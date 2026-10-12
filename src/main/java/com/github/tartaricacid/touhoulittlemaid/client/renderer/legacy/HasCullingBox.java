package com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy;

import net.minecraft.world.phys.AABB;

/**
 * Little Maid Continued: MC 26.x moved {@code getBoundingBoxForCulling} from the entity to its renderer;
 * entities with model-sized culling boxes expose them through this and {@link LegacyEntityRenderer} uses it.
 */
public interface HasCullingBox {
    AABB getBoundingBoxForCulling();
}
