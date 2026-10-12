package com.github.tartaricacid.touhoulittlemaid.geckolib3.geo;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.AnimatableEntity;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.event.predicate.AnimationEvent;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.molang.context.AnimationContext;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.util.Color;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.AnimatedGeoModel;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.model.provider.data.EntityModelData;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.util.EModelRenderCycle;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.util.IRenderCycle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.util.LightCoordsUtil;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy.LegacyEntityModel;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy.LegacyLivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class GeoReplacedEntityRenderer<T extends LivingEntity, E extends AnimatableEntity<T>> extends LegacyLivingEntityRenderer<T, LegacyEntityModel<T>> implements IGeoRenderer<T> {
    protected final List<GeoLayerRenderer> layerRenderers = new ObjectArrayList<>();
    protected AnimatableEntity<T> currentAnimatable;
    protected float widthScale = 1;
    protected float heightScale = 1;
    protected Matrix4f dispatchedMat = new Matrix4f();
    protected Matrix4f renderEarlyMat = new Matrix4f();
    protected MultiBufferSource rtb = null;
    private IRenderCycle currentModelRenderCycle = EModelRenderCycle.INITIAL;

    public GeoReplacedEntityRenderer(EntityRendererProvider.Context renderManager) {
        // the geo model draws itself; the base class model is only a placeholder
        super(renderManager, new LegacyEntityModel<>() {
            @Override
            public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            }

            @Override
            public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
            }
        }, 0.5f);
    }

    public static int getPackedOverlay(LivingEntity entity, float u) {
        return OverlayTexture.pack(OverlayTexture.u(u), OverlayTexture.v(entity.hurtTime > 0 || entity.deathTime > 0));
    }

    private static float getFacingAngle(Direction facingIn) {
        return switch (facingIn) {
            case SOUTH -> 90;
            case NORTH -> 270;
            case EAST -> 180;
            default -> 0;
        };
    }

    @Override
    @Nonnull
    public IRenderCycle getCurrentModelRenderCycle() {
        return this.currentModelRenderCycle;
    }

    @Override
    public void setCurrentModelRenderCycle(IRenderCycle currentModelRenderCycle) {
        this.currentModelRenderCycle = currentModelRenderCycle;
    }

    @Override
    public float getWidthScale(T animatable) {
        return this.widthScale;
    }

    @Override
    public float getHeightScale(T entity) {
        return this.heightScale;
    }

    @Override
    public void renderEarly(T animatable, PoseStack poseStack, float partialTick,
                            MultiBufferSource bufferSource, VertexConsumer buffer, int packedLight, int packedOverlayIn,
                            float red, float green, float blue, float alpha) {
        this.renderEarlyMat = new Matrix4f(poseStack.last().pose());
        IGeoRenderer.super.renderEarly(animatable, poseStack, partialTick, bufferSource, buffer, packedLight, packedOverlayIn, red, green, blue, alpha);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {

        render(entity, getAnimatableEntity(entity), entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    public void render(T entity, E animatable, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        this.currentAnimatable = animatable;
        this.dispatchedMat = new Matrix4f(poseStack.last().pose());
        boolean shouldSit = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());

        setCurrentModelRenderCycle(EModelRenderCycle.INITIAL);

        poseStack.pushPose();
        EntityModelData entityModelData = new EntityModelData();
        entityModelData.isSitting = shouldSit;
        entityModelData.isChild = entity.isBaby();

        float lerpBodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float lerpHeadRot = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        float netHeadYaw = lerpHeadRot - lerpBodyRot;

        if (shouldSit && entity.getVehicle() instanceof LivingEntity vehicle) {
            lerpBodyRot = Mth.rotLerp(partialTick, vehicle.yBodyRotO, vehicle.yBodyRot);
            netHeadYaw = lerpHeadRot - lerpBodyRot;
            float clampedHeadYaw = Mth.clamp(Mth.wrapDegrees(netHeadYaw), -85, 85);
            lerpBodyRot = lerpHeadRot - clampedHeadYaw;
            if (clampedHeadYaw * clampedHeadYaw > 2500f) {
                lerpBodyRot += clampedHeadYaw * 0.2f;
            }
            netHeadYaw = lerpHeadRot - lerpBodyRot;
        }

        if (entity.getPose() == Pose.SLEEPING) {
            Direction direction = entity.getBedOrientation();
            if (direction != null) {
                float eyeOffset = entity.getEyeHeight(Pose.STANDING) - 0.1f;
                poseStack.translate(-direction.getStepX() * eyeOffset, 0, -direction.getStepZ() * eyeOffset);
            }
        }

        float lerpedAge = entity.tickCount + partialTick;
        float limbSwingAmount = 0;
        float limbSwing = 0;

        setupRotations(entity, poseStack, lerpedAge, lerpBodyRot, partialTick, entity.getScale());
        preRenderCallback(entity, poseStack, partialTick);
        if (!shouldSit && entity.isAlive()) {
            limbSwingAmount = entity.walkAnimation.speed(partialTick);
            limbSwing = entity.walkAnimation.position(partialTick);
            if (entity.isBaby()) {
                limbSwing *= 3.0F;
            }
        }

        float headPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        entityModelData.headPitch = -headPitch;
        entityModelData.netHeadYaw = -Mth.clamp(Mth.wrapDegrees(netHeadYaw), -85, 85);
        AnimationEvent predicate = new AnimationEvent(animatable, limbSwing, limbSwingAmount, partialTick,
                (limbSwingAmount <= -getSwingMotionAnimThreshold() || limbSwingAmount <= getSwingMotionAnimThreshold()), Collections.singletonList(entityModelData));
        AnimationContext<?> ctx = new AnimationContext<>(entity, this.currentAnimatable, predicate, entityModelData);
        animatable.setCustomAnimations(ctx, predicate);
        AnimatedGeoModel model = animatable.getCurrentModel();
        if (model != null) {
            poseStack.translate(0, 0.01f, 0);

            Color renderColor = getRenderColor(entity, partialTick, poseStack, bufferSource, null, packedLight);
            RenderType renderType = getRenderType(entity, partialTick, poseStack, bufferSource, null, packedLight,
                    getTextureLocation(entity));

            if (Minecraft.getInstance().player != null && !entity.isInvisibleTo(Minecraft.getInstance().player)) {
                VertexConsumer translucentBuffer = bufferSource.getBuffer(RenderTypes.entityCutout(getTextureLocation(entity)));
                render(model, entity, partialTick, renderType, poseStack, bufferSource, translucentBuffer,
                        packedLight, getPackedOverlay(entity, getOverlayProgress(entity, partialTick)),
                        renderColor.getRed() / 255f, renderColor.getGreen() / 255f,
                        renderColor.getBlue() / 255f, renderColor.getAlpha() / 255f);
            }
        }
        if (!entity.isSpectator()) {
            for (GeoLayerRenderer layerRenderer : this.layerRenderers) {
                layerRenderer.render(poseStack, bufferSource, packedLight, entity, limbSwing, limbSwingAmount, partialTick,
                        lerpedAge, netHeadYaw, headPitch);
            }
        }
        poseStack.popPose();

    }

    protected float getOverlayProgress(LivingEntity entity, float partialTicks) {
        return 0.0F;
    }

    protected void preRenderCallback(LivingEntity entity, PoseStack poseStack, float partialTick) {
    }

    @Override
    @NotNull
    public Identifier getTextureLocation(T entity) {
        return this.getAnimatableEntity(entity).getTextureLocation();
    }

    @Override
    protected void setupRotations(T pEntityLiving, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks, float pScale) {
        super.setupRotations(pEntityLiving, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks, pScale);
        if (pEntityLiving.deathTime <= 0 && pEntityLiving.isAutoSpinAttack()) {
            pPoseStack.mulPose(Axis.YP.rotationDegrees(((float) pEntityLiving.tickCount + pPartialTicks) * 75.0F));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90.0F + pEntityLiving.getXRot()));
        }
    }

    protected boolean isVisible(LivingEntity entity) {
        return !entity.isInvisible();
    }

    protected float getDeathMaxRotation(LivingEntity entity) {
        return 90;
    }

    protected float getSwingProgress(LivingEntity entity, float partialTick) {
        return entity.getAttackAnim(partialTick);
    }

    protected float getSwingMotionAnimThreshold() {
        return 0.15f;
    }

    public final boolean addLayer(GeoLayerRenderer<T, ?> layer) {
        return this.layerRenderers.add(layer);
    }

    public abstract E getAnimatableEntity(T entity);

    @Override
    public MultiBufferSource getCurrentRTB() {
        return this.rtb;
    }

    @Override
    public void setCurrentRTB(MultiBufferSource bufferSource) {
        this.rtb = bufferSource;
    }

    public List<GeoLayerRenderer> getLayerRenderers() {
        return layerRenderers;
    }
}
