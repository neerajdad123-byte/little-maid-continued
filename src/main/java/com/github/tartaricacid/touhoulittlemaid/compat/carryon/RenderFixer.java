package com.github.tartaricacid.touhoulittlemaid.compat.carryon;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.MultiBufferSource;
import net.minecraft.world.item.ItemStack;

// Little Maid Continued: no-op stub. The real integration lives in src/compat_disabled and is
// re-enabled once the mod has a build for this Minecraft version.
public class RenderFixer {
    public static boolean isCarryOnRender(ItemStack stack, MultiBufferSource bufferSource) {
        return false;
    }
}
