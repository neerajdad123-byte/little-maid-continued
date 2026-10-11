package com.github.tartaricacid.touhoulittlemaid.compat.gun.swarfare;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

// Little Maid Continued: no-op stub. The real integration lives in src/compat_disabled and is
// re-enabled once the mod has a build for this Minecraft version.
public class SWarfareCompat {
    public static boolean isInstalled() {
        return false;
    }

    public static boolean shouldHideLivingRender(LivingEntity entity) {
        return false;
    }

    public static boolean isVehicle(Entity entity) {
        return false;
    }

    public static boolean isGrenade(ItemStack stack) {
        return false;
    }

    public static Optional<Boolean> canVehicleSee(EntityMaid maid, LivingEntity target) {
        return Optional.empty();
    }
}
