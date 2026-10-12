package com.github.tartaricacid.touhoulittlemaid.mixin;

import com.github.tartaricacid.touhoulittlemaid.util.PendingIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Little Maid Continued: gives properties created during a {@link PendingIds#block} registration their block id.
 */
@Mixin(BlockBehaviour.Properties.class)
public abstract class BlockPropertiesIdMixin {
    @Shadow
    private @Nullable ResourceKey<Block> id;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void tlm$applyPendingId(CallbackInfo ci) {
        ResourceKey<Block> pending = PendingIds.currentBlock();
        if (pending != null && this.id == null) {
            this.id = pending;
        }
    }
}
