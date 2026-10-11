package com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.MultiBufferSource;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.SubmitBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Little Maid Continued: runs MC 1.21.1 style block entity rendering
 * ({@code render(blockEntity, partialTick, poseStack, buffer, light, overlay)}) inside the MC 26.x submit pipeline.
 */
public interface LegacyBlockEntityRenderer<T extends BlockEntity> extends BlockEntityRenderer<T, LegacyBlockEntityRenderState<T>> {
    @Override
    default LegacyBlockEntityRenderState<T> createRenderState() {
        return new LegacyBlockEntityRenderState<>();
    }

    @Override
    default void extractRenderState(T blockEntity, LegacyBlockEntityRenderState<T> state, float partialTicks, Vec3 cameraPosition,
                                    ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.blockEntity = blockEntity;
        state.partialTick = partialTicks;
    }

    @Override
    default void submit(LegacyBlockEntityRenderState<T> state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.blockEntity == null) {
            return;
        }
        try (SubmitBufferSource buffers = new SubmitBufferSource(collector, camera)) {
            this.render(state.blockEntity, state.partialTick, poseStack, buffers, state.lightCoords, OverlayTexture.NO_OVERLAY);
        }
    }

    void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay);
}
