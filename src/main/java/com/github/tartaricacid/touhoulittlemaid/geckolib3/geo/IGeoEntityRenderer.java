package com.github.tartaricacid.touhoulittlemaid.geckolib3.geo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.MultiBufferSource;
import net.minecraft.world.entity.Entity;

public interface IGeoEntityRenderer<T extends Entity> {
    IGeoEntity getGeoEntity(T entity);

    void addGeoLayerRenderer(GeoLayerRenderer<?, ?> layerRenderer);

    void geoRender(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight);
}
