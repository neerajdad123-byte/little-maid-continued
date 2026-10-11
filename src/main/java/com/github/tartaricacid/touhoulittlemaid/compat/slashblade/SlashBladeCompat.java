package com.github.tartaricacid.touhoulittlemaid.compat.slashblade;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.ItemStack;

// Little Maid Continued: no-op stub. The real integration lives in src/compat_disabled and is
// re-enabled once the mod has a build for this Minecraft version.
public class SlashBladeCompat {
    public static boolean isSlashBladeLoaded() {
        return false;
    }

    public static boolean isSlashBladeItem(ItemStack stack) {
        return false;
    }

    public static void swingSlashBlade(EntityMaid maid, ItemStack itemInHand) {
    }
}
