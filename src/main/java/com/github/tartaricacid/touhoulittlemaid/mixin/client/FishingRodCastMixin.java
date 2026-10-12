package com.github.tartaricacid.touhoulittlemaid.mixin.client;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.FishingRodCast;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Shows the cast fishing rod model while a maid is fishing (vanilla only checks players).
 */
@Mixin(FishingRodCast.class)
public class FishingRodCastMixin {
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void tlm$maidFishing(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed,
                                 ItemDisplayContext displayContext, CallbackInfoReturnable<Boolean> cir) {
        if (owner instanceof EntityMaid maid && maid.getMainHandItem() == stack) {
            cir.setReturnValue(maid.hasFishingHook());
        }
    }
}
