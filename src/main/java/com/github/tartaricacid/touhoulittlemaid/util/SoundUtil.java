package com.github.tartaricacid.touhoulittlemaid.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public final class SoundUtil {
    private static final long MORNING_START = 0;
    private static final long MORNING_END = 3000;
    private static final long EVENING_START = 12000;
    private static final long EVENING_END = 15000;

    public static SoundEvent environmentSound(EntityMaid maid, SoundEvent fallback, float probability) {
        Level world = maid.level();
        RandomSource rand = maid.getRandom();
        BlockPos pos = maid.blockPosition();
        long dayTime = world.getOverworldClockTime();
        Biome biome = world.getBiome(pos).value();

        // 差不多早上 6:00 - 9:00
        if (rand.nextFloat() < probability && MORNING_START < dayTime && dayTime < MORNING_END) {
            return InitSounds.MAID_MORNING.get();
        }
        // 差不多下午 6:00 - 9:00
        if (rand.nextFloat() < probability && EVENING_START < dayTime && dayTime < EVENING_END) {
            return InitSounds.MAID_NIGHT.get();
        }
        if (rand.nextFloat() < probability && world.isRaining() && isRainBiome(biome, pos)) {
            return InitSounds.MAID_RAIN.get();
        }
        if (rand.nextFloat() < probability && world.isRaining() && isSnowyBiome(biome, pos)) {
            return InitSounds.MAID_SNOW.get();
        }
        if (rand.nextFloat() < probability && biome.coldEnoughToSnow(pos, maid.level().getSeaLevel())) {
            return InitSounds.MAID_COLD.get();
        }
        if (rand.nextFloat() < probability && shouldSnowGolemBurn(biome, pos)) {
            return InitSounds.MAID_HOT.get();
        }
        return fallback;
    }

    public static SoundEvent attackSound(EntityMaid maid, SoundEvent fallback, float probability) {
        RandomSource rand = maid.getRandom();
        if (rand.nextFloat() < probability) {
            return InitSounds.MAID_FIND_TARGET.get();
        }
        return fallback;
    }

    // shortcut: overworld sea level for the height-adjusted temperature; pass the level's sea level if other dimensions sound wrong
    private static final int SEA_LEVEL = 63;

    public static boolean isRainBiome(Biome biome, BlockPos pos) {
        return biome.getPrecipitationAt(pos, SEA_LEVEL) == Biome.Precipitation.RAIN && !shouldSnowGolemBurn(biome, pos);
    }

    public static boolean isSnowyBiome(Biome biome, BlockPos pos) {
        return biome.getPrecipitationAt(pos, SEA_LEVEL) == Biome.Precipitation.SNOW;
    }

    private static boolean shouldSnowGolemBurn(Biome biome, BlockPos pos) {
        return biome.getBaseTemperature() > 1.0F;
    }
}
