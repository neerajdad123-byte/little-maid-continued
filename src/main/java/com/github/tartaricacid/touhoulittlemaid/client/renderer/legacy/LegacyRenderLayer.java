package com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Little Maid Continued: the MC 1.21.1 {@code RenderLayer} API.
 */
public abstract class LegacyRenderLayer<T extends Entity, M extends LegacyEntityModel<T>> {
    private final LegacyRenderLayerParent<T, M> renderer;

    public LegacyRenderLayer(LegacyRenderLayerParent<T, M> renderer) {
        this.renderer = renderer;
    }

    protected static <T extends LivingEntity> void renderColoredCutoutModel(LegacyEntityModel<T> model, Identifier texture, PoseStack poseStack,
                                                                            MultiBufferSource buffer, int packedLight, T entity, int color) {
        VertexConsumer consumer = buffer.getBuffer(RenderTypes.entityCutout(texture));
        model.renderToBuffer(poseStack, consumer, packedLight, LegacyLivingEntityRenderer.getOverlayCoords(entity, 0.0F), color);
    }

    public M getParentModel() {
        return this.renderer.getModel();
    }

    protected Identifier getTextureLocation(T entity) {
        return this.renderer.getTextureLocation(entity);
    }

    public abstract void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                                float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch);
}
