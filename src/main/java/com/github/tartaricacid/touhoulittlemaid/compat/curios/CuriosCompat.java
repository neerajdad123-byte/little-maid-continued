package com.github.tartaricacid.touhoulittlemaid.compat.curios;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.MenuProvider;

// Little Maid Continued: no-op stub. The real integration lives in src/compat_disabled and is
// re-enabled once the mod has a build for this Minecraft version.
public class CuriosCompat {
    public static void init() {
    }

    public static boolean isLoaded() {
        return false;
    }

    public static boolean isLoadedOrEnable() {
        return false;
    }

    public static MenuProvider create(EntityMaid maid) {
        return maid.getMaidBackpackType().getGuiProvider(maid.getId());
    }

    public static void clientUpdatePage(int page) {
    }

    public static void clientResetPage() {
    }
}
