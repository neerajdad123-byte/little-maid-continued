package com.github.tartaricacid.touhoulittlemaid.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Little Maid Continued: MC 26.x removed {@code Block#onRemove}. Blocks that dropped their block entity's
 * contents there implement this, and the block entity calls it from {@code preRemoveSideEffects},
 * which runs right before the block entity is removed (only when the block itself changes).
 */
public interface LegacyRemovalBlock {
    void onRemoveLegacy(BlockState state, Level worldIn, BlockPos pos);

    static void notify(Level level, BlockPos pos, BlockState state) {
        if (level != null && state.getBlock() instanceof LegacyRemovalBlock block) {
            block.onRemoveLegacy(state, level, pos);
        }
    }
}
