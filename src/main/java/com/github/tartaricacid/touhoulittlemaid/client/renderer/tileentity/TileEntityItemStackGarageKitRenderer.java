package com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity;

import net.minecraft.world.entity.EntitySpawnReason;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.LegacyDraw;
import com.github.tartaricacid.touhoulittlemaid.util.PortUtil;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.client.render.MaidRenderState;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.SimpleBedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.resource.BedrockModelLoader;
import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.tartaricacid.touhoulittlemaid.item.ItemGarageKit;
import com.github.tartaricacid.touhoulittlemaid.util.EntityCacheUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy.LegacyItemStackRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.github.tartaricacid.touhoulittlemaid.init.InitDataComponent.ENTITY_ID_TAG_NAME;
import static com.github.tartaricacid.touhoulittlemaid.util.EntityCacheUtil.clearMaidDataResidue;

public class TileEntityItemStackGarageKitRenderer extends LegacyItemStackRenderer {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "textures/bedrock/block/statue_base.png");
    private static SimpleBedrockModel<Entity> BASE_MODEL;

    public TileEntityItemStackGarageKitRenderer() {
        BASE_MODEL = BedrockModelLoader.getModel(BedrockModelLoader.STATUE_BASE);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (BASE_MODEL == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.translate(1, 1.5, 1);
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));
        VertexConsumer buffer = bufferIn.getBuffer(RenderTypes.entityCutout(TEXTURE));
        BASE_MODEL.renderToBuffer(poseStack, buffer, combinedLightIn, combinedOverlayIn);
        poseStack.popPose();

        CustomData data = ItemGarageKit.getMaidData(stack);
        Level world = Minecraft.getInstance().level;
        if (data.isEmpty() || world == null) {
            return;
        }

        PortUtil.entityTypeByString(data.copyTag().getStringOr(ENTITY_ID_TAG_NAME, "")).ifPresent(type -> {
                    try {
                        renderEntity(stack, poseStack, bufferIn, combinedLightIn, data.copyTag(), world, type);
                    } catch (ExecutionException e) {
                        TouhouLittleMaid.LOGGER.error("Failed to render garage kit item entity", e);
                    }
                }
        );
    }

    private void renderEntity(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, CompoundTag data, Level world, EntityType<?> type) throws ExecutionException {
        Entity entity;
        if (type.equals(InitEntities.MAID.get())) {
            entity = EntityCacheUtil.GARAGE_KIT_CACHE.get(stack, () -> new EntityMaid(world));
        } else {
            entity = EntityCacheUtil.ENTITY_CACHE.get(type, () -> {
                Entity e = type.create(world, EntitySpawnReason.LOAD);
                return Objects.requireNonNullElseGet(e, () -> new EntityMaid(world));
            });
        }

        float renderItemScale = 1;
        PortUtil.loadEntity(entity, data);
        if (entity instanceof EntityMaid maid) {
            clearMaidDataResidue(maid, true);
            if (data.contains(EntityMaid.MODEL_ID_TAG)) {
                String modelId = data.getStringOr(EntityMaid.MODEL_ID_TAG, "");
                renderItemScale = CustomPackLoader.MAID_MODELS.getModelRenderItemScale(modelId);
            }
            maid.renderState = MaidRenderState.GARAGE_KIT;
        }

        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.scale(renderItemScale, renderItemScale, renderItemScale);
        poseStack.translate(1, 0.21328125, 1);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        LegacyDraw.entity(entity, 0, 0, 0, 0, 0,
                    poseStack, bufferIn, combinedLightIn);
        poseStack.popPose();
    }
}
