package com.github.tartaricacid.touhoulittlemaid.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;

/**
 * Little Maid Continued: small helpers that replace APIs removed between MC 1.21.1 and 26.x.
 */
public final class PortUtil {
    private PortUtil() {
    }

    /**
     * Replacement for the removed {@code ChatFormatting#getColor()}; returns an opaque ARGB color.
     */
    public static int color(ChatFormatting formatting) {
        TextColor color = TextColor.fromLegacyFormat(formatting);
        return color == null ? 0xFFFFFFFF : ARGB.opaque(color.getValue());
    }

    /**
     * Replacement for the removed {@code NbtUtils#writeBlockPos(BlockPos)}.
     */
    public static Tag writeBlockPos(BlockPos pos) {
        return BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).getOrThrow();
    }
}
