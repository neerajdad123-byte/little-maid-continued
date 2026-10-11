package com.github.tartaricacid.touhoulittlemaid.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.Nullable;

/**
 * Client-only half of {@link PortUtil}; only loaded on the physical client.
 */
final class ClientPortUtil {
    private ClientPortUtil() {
    }

    @Nullable
    static HolderLookup.Provider registries() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level == null ? null : mc.level.registryAccess();
    }
}
