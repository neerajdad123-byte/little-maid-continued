package com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

/**
 * Render state for {@link LegacyBlockEntityRenderer}; keeps the block entity for the legacy render code.
 */
public class LegacyBlockEntityRenderState<T extends BlockEntity> extends BlockEntityRenderState {
    public @Nullable T blockEntity;
    public float partialTick;
}
