package com.github.tartaricacid.touhoulittlemaid.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.entity.FuelValues;
import org.jspecify.annotations.Nullable;

/**
 * Client-only half of {@link PortUtil}; only loaded on the physical client.
 */
final class ClientPortUtil {
    private ClientPortUtil() {
    }

    static FuelValues fuelValues() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level == null
                ? FuelValues.vanillaBurnTimes(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), FeatureFlags.VANILLA_SET)
                : mc.level.fuelValues();
    }

    @Nullable
    static HolderLookup.Provider registries() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level == null ? null : mc.level.registryAccess();
    }
}
