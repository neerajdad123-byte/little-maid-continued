package com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.MultiBufferSource;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.SubmitBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

/**
 * Little Maid Continued: runs MC 1.21.1 style {@code render(entity, yaw, partialTicks, poseStack, buffer, light)}
 * code inside the MC 26.x submit pipeline.
 */
public abstract class LegacyEntityRenderer<T extends Entity> extends EntityRenderer<T, LegacyRenderState<T>> {
    protected final EntityRendererProvider.Context context;

    protected LegacyEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public LegacyRenderState<T> createRenderState() {
        return new LegacyRenderState<>();
    }

    @Override
    public void extractRenderState(T entity, LegacyRenderState<T> state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.entity = entity;
        state.yaw = entity.getYRot(partialTicks);
    }

    @Override
    public void submit(LegacyRenderState<T> state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.entity != null) {
            try (SubmitBufferSource buffers = new SubmitBufferSource(collector, camera)) {
                this.render(state.entity, state.yaw, state.partialTick, poseStack, buffers, state.lightCoords);
            }
        }
        super.submit(state, poseStack, collector, camera);
    }

    /**
     * MC 1.21.1 style rendering. Name tags and leashes are submitted by the base class afterwards.
     */
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    }

    public Identifier getTextureLocation(T entity) {
        return MissingTextureAtlasSprite.getLocation();
    }

    public EntityRenderDispatcher getDispatcher() {
        return this.entityRenderDispatcher;
    }
}
