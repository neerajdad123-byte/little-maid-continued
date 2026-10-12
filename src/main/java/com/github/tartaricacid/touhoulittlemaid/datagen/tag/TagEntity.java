package com.github.tartaricacid.touhoulittlemaid.datagen.tag;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/**
 * Little Maid Continued: key constants used at runtime. The data generator for these lives in
 * src/datagen_disabled until it is ported to MC 26.x.
 */
public final class TagEntity {
    /**
     * 女仆妖精的攻击目标，默认仅攻击铁傀儡和玩家
     */
    public static final TagKey<EntityType<?>> MAID_FAIRY_ATTACK_GOAL = createTagKey("maid_fairy_attack_goal");
    /**
     * 女仆在骑乘时会强制同步朝向；部分模组（如机械动力）会因此异常旋转，故添加此标签
     */
    public static final TagKey<EntityType<?>> MAID_VEHICLE_ROTATE_BLOCKLIST = createTagKey("maid_vehicle_rotate_blocklist");
    public static final TagKey<EntityType<?>> CARRYON_ENTITY_BLACKLIST = createTagKey(Identifier.parse("carryon:entity_blacklist"));
    /**
     * 冰与火的石化效果免疫标签
     */
    public static final TagKey<EntityType<?>> IMMUNE_TO_GORGON_STONE = createTagKey(Identifier.parse("iceandfire:immune_to_gorgon_stone"));

    private TagEntity() {
    }

    private static TagKey<EntityType<?>> createTagKey(String name) {
        return createTagKey(Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, name));
    }

    private static TagKey<EntityType<?>> createTagKey(Identifier id) {
        return TagKey.create(Registries.ENTITY_TYPE, id);
    }
}
