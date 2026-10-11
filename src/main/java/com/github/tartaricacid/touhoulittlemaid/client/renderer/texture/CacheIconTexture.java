package com.github.tartaricacid.touhoulittlemaid.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/**
 * Model icon rendered from the cache screen; the image is already in memory.
 */
public class CacheIconTexture extends DynamicTexture {
    private final Identifier modelId;

    public CacheIconTexture(Identifier modelId, NativeImage imageIn) {
        super(modelId::toString, imageIn);
        this.modelId = modelId;
    }

    public Identifier getModelId() {
        return modelId;
    }
}
