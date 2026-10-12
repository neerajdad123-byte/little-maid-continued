package com.github.tartaricacid.touhoulittlemaid.crafting;

import com.github.tartaricacid.touhoulittlemaid.init.InitRecipes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

/**
 * Little Maid Continued: MC 26.x no longer sends recipes to clients; the creative tab and JEI need the altar ones.
 */
@EventBusSubscriber
public final class AltarRecipeSync {
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(InitRecipes.ALTAR_CRAFTING.get());
    }
}
