package com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

/**
 * Little Maid Continued: the MC 1.21.1 {@code RenderLayerParent} API.
 */
public interface LegacyRenderLayerParent<T extends Entity, M extends LegacyEntityModel<T>> {
    M getModel();

    Identifier getTextureLocation(T entity);
}
