package com.github.tartaricacid.touhoulittlemaid.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Little Maid Continued: since MC 1.21.2 every {@code Item.Properties} / {@code BlockBehaviour.Properties}
 * needs its registry id before the item or block is constructed. The mod's items build their properties
 * internally, so registration runs inside {@link #item} / {@link #block} and a mixin on the properties
 * constructors applies the pending id.
 */
public final class PendingIds {
    private static final ThreadLocal<@Nullable ResourceKey<Item>> ITEM = new ThreadLocal<>();
    private static final ThreadLocal<@Nullable ResourceKey<Block>> BLOCK = new ThreadLocal<>();

    private PendingIds() {
    }

    public static <T> T item(Identifier id, Supplier<T> factory) {
        ResourceKey<Item> previous = ITEM.get();
        ITEM.set(ResourceKey.create(Registries.ITEM, id));
        try {
            return factory.get();
        } finally {
            ITEM.set(previous);
        }
    }

    public static <T> T block(Identifier id, Supplier<T> factory) {
        ResourceKey<Block> previous = BLOCK.get();
        BLOCK.set(ResourceKey.create(Registries.BLOCK, id));
        try {
            return factory.get();
        } finally {
            BLOCK.set(previous);
        }
    }

    public static @Nullable ResourceKey<Item> currentItem() {
        return ITEM.get();
    }

    public static @Nullable ResourceKey<Block> currentBlock() {
        return BLOCK.get();
    }
}
