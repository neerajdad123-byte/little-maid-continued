package com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

/**
 * Render state for {@link LegacyEntityRenderer}: keeps the entity itself, since the mod's model and
 * animation code reads entity data directly while rendering.
 */
public class LegacyRenderState<T extends Entity> extends EntityRenderState {
    public @Nullable T entity;
    public float yaw;
}
