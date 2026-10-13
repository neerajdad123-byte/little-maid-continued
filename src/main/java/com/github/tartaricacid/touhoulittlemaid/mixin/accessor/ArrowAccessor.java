package com.github.tartaricacid.touhoulittlemaid.mixin.accessor;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = AbstractArrow.class)
public interface ArrowAccessor {
    @Invoker("isInGround")
    boolean tlmInGround();

    // MC 26.x removed the public getter
    @Accessor("baseDamage")
    double tlmGetBaseDamage();

    @Invoker("getPickupItem")
    ItemStack getTlmPickupItem();
}
