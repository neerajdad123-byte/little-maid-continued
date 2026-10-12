package com.github.tartaricacid.touhoulittlemaid.client.event;

import com.github.tartaricacid.touhoulittlemaid.crafting.AltarRecipe;
import com.github.tartaricacid.touhoulittlemaid.init.InitRecipes;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

import java.util.List;

/**
 * Little Maid Continued: altar recipes as last sent by the server (see {@code AltarRecipeSync}).
 */
@EventBusSubscriber(value = Dist.CLIENT)
public final class ClientAltarRecipes {
    private static List<RecipeHolder<AltarRecipe>> recipes = List.of();

    public static List<RecipeHolder<AltarRecipe>> get() {
        return recipes;
    }

    @SubscribeEvent
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        recipes = List.copyOf(event.getRecipeMap().byType(InitRecipes.ALTAR_CRAFTING.get()));
    }
}
