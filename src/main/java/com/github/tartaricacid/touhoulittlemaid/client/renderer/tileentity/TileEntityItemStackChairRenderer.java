package com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity;

import net.minecraft.world.entity.EntitySpawnReason;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.LegacyDraw;
import com.github.tartaricacid.touhoulittlemaid.util.PortUtil;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.EntityChairRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityChair;
import com.github.tartaricacid.touhoulittlemaid.item.ItemChair;
import com.github.tartaricacid.touhoulittlemaid.util.EntityCacheUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy.LegacyItemStackRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class TileEntityItemStackChairRenderer extends LegacyItemStackRenderer {
    public TileEntityItemStackChairRenderer() {
    }

    @Override
    public void renderByItem(ItemStack itemStackIn, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }
        EntityChair entityChair;
        ItemChair.Data data = ItemChair.getData(itemStackIn);
        float renderItemScale = CustomPackLoader.CHAIR_MODELS.getModelRenderItemScale(data.modelId());
        try {
            entityChair = (EntityChair) EntityCacheUtil.ENTITY_CACHE.get(EntityChair.TYPE, () -> {
                Entity e = EntityChair.TYPE.create(world, EntitySpawnReason.LOAD);
                return Objects.requireNonNullElseGet(e, () -> new EntityChair(world));
            });
        } catch (ExecutionException | ClassCastException e) {
            TouhouLittleMaid.LOGGER.error("Failed to render chair item preview", e);
            return;
        }
        entityChair.setModelId(data.modelId());
        poseStack.pushPose();
        poseStack.scale(renderItemScale, renderItemScale, renderItemScale);
        EntityChairRenderer.renderHitBox = false;
        LegacyDraw.entity(entityChair,
                1 / renderItemScale - 0.125, 0.25, 0.75, 0, 0,
                poseStack, buffer, combinedLight);
        EntityChairRenderer.renderHitBox = true;
        poseStack.popPose();
    }
}
