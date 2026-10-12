package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.entity.monster.EntityFairy;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

/**
 * Little Maid Continued: MC 26.x spawn eggs take their entity from {@code Item.Properties#spawnEgg}.
 * TODO: the old "egg on a fairy spawns a baby fairy" hook is static in 26.x and was dropped.
 */
public class ItemFairySpawnEgg extends SpawnEggItem {
    public ItemFairySpawnEgg() {
        super(new Item.Properties().spawnEgg(EntityFairy.TYPE));
    }
}
