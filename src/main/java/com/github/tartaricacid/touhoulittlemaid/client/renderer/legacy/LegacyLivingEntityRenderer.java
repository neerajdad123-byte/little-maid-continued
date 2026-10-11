package com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.MultiBufferSource;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.scores.Team;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Little Maid Continued: port of the MC 1.21.1 {@code LivingEntityRenderer#render} flow
 * (rotations, scale, model animation, body, layers) on top of {@link LegacyEntityRenderer}.
 */
public abstract class LegacyLivingEntityRenderer<T extends LivingEntity, M extends LegacyEntityModel<T>>
        extends LegacyEntityRenderer<T> implements LegacyRenderLayerParent<T, M> {
    protected M model;
    protected final List<LegacyRenderLayer<T, M>> layers = Lists.newArrayList();

    protected LegacyLivingEntityRenderer(EntityRendererProvider.Context context, M model, float shadowRadius) {
        super(context);
        this.model = model;
        this.shadowRadius = shadowRadius;
    }

    public final boolean addLayer(LegacyRenderLayer<T, M> layer) {
        return this.layers.add(layer);
    }

    @Override
    public M getModel() {
        return this.model;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        this.model.attackTime = this.getAttackAnim(entity, partialTicks);
        boolean shouldSit = entity.isPassenger() && entity.getVehicle() != null && entity.getVehicle().shouldRiderSit();
        this.model.riding = shouldSit;
        this.model.young = entity.isBaby();
        float bodyRot = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float headRot = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float netHeadYaw = headRot - bodyRot;
        if (shouldSit && entity.getVehicle() instanceof LivingEntity vehicle) {
            bodyRot = Mth.rotLerp(partialTicks, vehicle.yBodyRotO, vehicle.yBodyRot);
            netHeadYaw = headRot - bodyRot;
            float clamped = Mth.clamp(Mth.wrapDegrees(netHeadYaw), -85.0F, 85.0F);
            bodyRot = headRot - clamped;
            if (clamped * clamped > 2500.0F) {
                bodyRot += clamped * 0.2F;
            }
            netHeadYaw = headRot - bodyRot;
        }

        float headPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        if (this.isEntityUpsideDown(entity)) {
            headPitch *= -1.0F;
            netHeadYaw *= -1.0F;
        }
        netHeadYaw = Mth.wrapDegrees(netHeadYaw);

        if (entity.hasPose(Pose.SLEEPING)) {
            Direction direction = entity.getBedOrientation();
            if (direction != null) {
                float offset = entity.getEyeHeight(Pose.STANDING) - 0.1F;
                poseStack.translate(-direction.getStepX() * offset, 0.0F, -direction.getStepZ() * offset);
            }
        }

        float entityScale = entity.getScale();
        poseStack.scale(entityScale, entityScale, entityScale);
        float ageInTicks = this.getBob(entity, partialTicks);
        this.setupRotations(entity, poseStack, ageInTicks, bodyRot, partialTicks, entityScale);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(entity, poseStack, partialTicks);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        float limbSwingAmount = 0.0F;
        float limbSwing = 0.0F;
        if (!shouldSit && entity.isAlive()) {
            limbSwingAmount = Math.min(entity.walkAnimation.speed(partialTicks), 1.0F);
            limbSwing = entity.walkAnimation.position(partialTicks);
            if (entity.isBaby()) {
                limbSwing *= 3.0F;
            }
        }

        this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        Minecraft minecraft = Minecraft.getInstance();
        boolean bodyVisible = this.isBodyVisible(entity);
        boolean translucent = !bodyVisible && minecraft.player != null && !entity.isInvisibleTo(minecraft.player);
        boolean glowing = minecraft.shouldEntityAppearGlowing(entity);
        RenderType renderType = this.getRenderType(entity, bodyVisible, translucent, glowing);
        if (renderType != null) {
            VertexConsumer consumer = buffer.getBuffer(renderType);
            int overlay = getOverlayCoords(entity, this.getWhiteOverlayProgress(entity, partialTicks));
            this.model.renderToBuffer(poseStack, consumer, packedLight, overlay, translucent ? 0x26FFFFFF : -1);
        }

        if (!entity.isSpectator()) {
            for (LegacyRenderLayer<T, M> layer : this.layers) {
                layer.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }
        }
        poseStack.popPose();
    }

    protected @Nullable RenderType getRenderType(T entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        Identifier texture = this.getTextureLocation(entity);
        if (translucent) {
            return RenderTypes.entityTranslucentCullItemTarget(texture);
        } else if (bodyVisible) {
            return this.model.renderType(texture);
        } else {
            return glowing ? RenderTypes.outline(texture) : null;
        }
    }

    public static int getOverlayCoords(LivingEntity entity, float whiteOverlayProgress) {
        return OverlayTexture.pack(OverlayTexture.u(whiteOverlayProgress), OverlayTexture.v(entity.hurtTime > 0 || entity.deathTime > 0));
    }

    protected boolean isBodyVisible(T entity) {
        return !entity.isInvisible();
    }

    private static float sleepDirectionToRotation(Direction facing) {
        return switch (facing) {
            case SOUTH -> 90.0F;
            case WEST -> 0.0F;
            case NORTH -> 270.0F;
            case EAST -> 180.0F;
            default -> 0.0F;
        };
    }

    protected boolean isShaking(T entity) {
        return entity.isFullyFrozen();
    }

    protected void setupRotations(T entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        if (this.isShaking(entity)) {
            yBodyRot += (float) (Math.cos(entity.tickCount * 3.25) * Math.PI * 0.4F);
        }
        if (!entity.hasPose(Pose.SLEEPING)) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yBodyRot));
        }
        if (entity.deathTime > 0) {
            float progress = Math.min(Mth.sqrt((entity.deathTime + partialTick - 1.0F) / 20.0F * 1.6F), 1.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(progress * this.getFlipDegrees(entity)));
        } else if (entity.isAutoSpinAttack()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F - entity.getXRot()));
            poseStack.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTick) * -75.0F));
        } else if (entity.hasPose(Pose.SLEEPING)) {
            Direction direction = entity.getBedOrientation();
            float rotation = direction != null ? sleepDirectionToRotation(direction) : yBodyRot;
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.mulPose(Axis.ZP.rotationDegrees(this.getFlipDegrees(entity)));
            poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
        } else if (this.isEntityUpsideDown(entity)) {
            poseStack.translate(0.0F, (entity.getBbHeight() + 0.1F) / scale, 0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        }
    }

    @Override
    protected boolean shouldShowName(T entity, double distanceToCameraSq) {
        if (entity.isDiscrete() && distanceToCameraSq >= 1024.0) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return false;
        }
        boolean visible = !entity.isInvisibleTo(player);
        if (entity != player) {
            Team team = entity.getTeam();
            Team myTeam = player.getTeam();
            if (team != null) {
                return switch (team.getNameTagVisibility()) {
                    case ALWAYS -> visible;
                    case NEVER -> false;
                    case HIDE_FOR_OTHER_TEAMS -> myTeam == null ? visible : team.isAlliedTo(myTeam) && (team.canSeeFriendlyInvisibles() || visible);
                    case HIDE_FOR_OWN_TEAM -> myTeam == null ? visible : !team.isAlliedTo(myTeam) && visible;
                };
            }
        }
        return !minecraft.gui.hud.isHidden() && entity != minecraft.getCameraEntity() && visible && !entity.isVehicle();
    }

    protected boolean isEntityUpsideDown(T entity) {
        return false;
    }

    protected float getAttackAnim(T entity, float partialTick) {
        return entity.getAttackAnim(partialTick);
    }

    protected float getBob(T entity, float partialTick) {
        return entity.tickCount + partialTick;
    }

    protected float getFlipDegrees(T entity) {
        return 90.0F;
    }

    protected float getWhiteOverlayProgress(T entity, float partialTick) {
        return 0.0F;
    }

    protected void scale(T entity, PoseStack poseStack, float partialTick) {
    }
}
