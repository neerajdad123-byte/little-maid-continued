package com.github.tartaricacid.touhoulittlemaid.mixin;

import com.github.tartaricacid.touhoulittlemaid.util.PendingIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Little Maid Continued: gives properties created during a {@link PendingIds#item} registration their item id.
 */
@Mixin(Item.Properties.class)
public abstract class ItemPropertiesIdMixin {
    @Shadow
    private @Nullable ResourceKey<Item> id;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void tlm$applyPendingId(CallbackInfo ci) {
        ResourceKey<Item> pending = PendingIds.currentItem();
        if (pending != null && this.id == null) {
            this.id = pending;
        }
    }
}
