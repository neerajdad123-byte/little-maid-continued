package com.github.tartaricacid.touhoulittlemaid.datagen.tag;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Little Maid Continued: key constants used at runtime. The data generator for these lives in
 * src/datagen_disabled until it is ported to MC 26.x.
 */
public final class TagItem {
    public static final TagKey<Item> GOHEI_ENCHANTABLE = createTagKey("gohei_enchantable");
    public static final TagKey<Item> MAID_PLANTABLE_SEEDS = createTagKey("maid_plantable_seeds");
    public static final TagKey<Item> MAID_TAMED_ITEM = createTagKey("maid_tamed_item");
    public static final TagKey<Item> MAID_MENDING_BLOCKLIST_ITEM = createTagKey("maid_mending_blocklist_item");
    public static final TagKey<Item> MAID_VANISHING_BLOCKLIST_ITEM = createTagKey("maid_vanishing_blocklist_item");
    public static final TagKey<Item> MAID_EAT_BLOCKLIST_ITEM = createTagKey("maid_eat_blocklist_item");

    private TagItem() {
    }

    public static TagKey<Item> createTagKey(String name) {
        return createTagKey(Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, name));
    }

    public static TagKey<Item> createTagKey(Identifier id) {
        return TagKey.create(Registries.ITEM, id);
    }
}
