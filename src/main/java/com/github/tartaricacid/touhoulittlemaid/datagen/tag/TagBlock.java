package com.github.tartaricacid.touhoulittlemaid.datagen.tag;

import net.minecraft.resources.Identifier;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * Little Maid Continued: key constants used at runtime. The data generator for these lives in
 * src/datagen_disabled until it is ported to MC 26.x.
 */
public final class TagBlock {
    public static final TagKey<Block> MAID_JUMP_FORBIDDEN_BLOCK = createTagKey("maid_jump_forbidden_block");
    public static final TagKey<Block> MAID_AVOID_BLOCK = createTagKey("maid_avoid_block");
    public static final TagKey<Block> ALTAR_TORII = createTagKey("altar_torii");
    public static final TagKey<Block> ALTAR_PILLAR = createTagKey("altar_pillar");
    public static final TagKey<Block> MAID_SNACK_STAND_BLOCK = createTagKey("maid_snack_stand_block");
    public static final TagKey<Block> SNACK_CABINET_FULL = createTagKey("snack_cabinet_full");
    public static final TagKey<Block> SNACK_CABINET_HALF = createTagKey("snack_cabinet_half");
    public static final TagKey<Block> CARRYON_BLOCK_BLACKLIST = createTagKey(Identifier.parse("carryon:block_blacklist"));

    private TagBlock() {
    }

    public static TagKey<Block> createTagKey(String name) {
        return createTagKey(Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, name));
    }

    public static TagKey<Block> createTagKey(Identifier id) {
        return TagKey.create(Registries.BLOCK, id);
    }
}
