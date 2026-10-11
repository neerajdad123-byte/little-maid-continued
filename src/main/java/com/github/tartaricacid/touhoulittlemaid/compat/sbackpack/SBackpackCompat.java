package com.github.tartaricacid.touhoulittlemaid.compat.sbackpack;

import net.minecraft.world.item.ItemStack;

// Little Maid Continued: no-op stub. The real integration lives in src/compat_disabled and is
// re-enabled once the mod has a build for this Minecraft version.
public class SBackpackCompat {
    public static void init() {
    }

    public static boolean isLoaded() {
        return false;
    }

    public static boolean isBackpack(ItemStack stack) {
        return false;
    }
}
