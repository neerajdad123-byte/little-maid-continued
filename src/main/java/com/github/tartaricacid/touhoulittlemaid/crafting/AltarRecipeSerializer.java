package com.github.tartaricacid.touhoulittlemaid.crafting;

import net.minecraft.world.item.ItemStackTemplate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class AltarRecipeSerializer {
    public static final MapCodec<AltarRecipe> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.STRING.optionalFieldOf("group", StringUtils.EMPTY).forGetter(AltarRecipe::group),
                    CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(AltarRecipe::getCategory),
                    Ingredient.CODEC.listOf().fieldOf("ingredients").flatXmap(AltarRecipeSerializer::checkIngredients, DataResult::success).forGetter(AltarRecipe::getIngredients),
                    Codec.FLOAT.fieldOf("power").forGetter(AltarRecipe::getPower),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(AltarRecipe::getResultTemplate),
                    Identifier.CODEC.fieldOf("entity").forGetter(AltarRecipe::getEntityType),
                    Codec.STRING.optionalFieldOf("lang", StringUtils.EMPTY).forGetter(AltarRecipe::getLangKey)
            ).apply(instance, AltarRecipe::new)
    );

    @NotNull
    private static DataResult<List<Ingredient>> checkIngredients(List<Ingredient> ingredientList) {
        Ingredient[] aingredient = ingredientList.toArray(Ingredient[]::new);
        if (aingredient.length == 0) {
            return DataResult.error(() -> "No ingredients for shapeless recipe");
        } else {
            if (aingredient.length > 6) {
                return DataResult.error(() -> "Too many ingredients for shapeless recipe. The maximum is: 6");
            }
            return DataResult.success(List.of(aingredient));
        }
    }

    private static AltarRecipe fromNetwork(RegistryFriendlyByteBuf byteBuf) {
        String group = byteBuf.readUtf();
        CraftingBookCategory category = byteBuf.readEnum(CraftingBookCategory.class);
        int size = byteBuf.readVarInt();
        List<Ingredient> ingredients = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(byteBuf));
        }
        float power = byteBuf.readFloat();
        ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(byteBuf);
        Identifier entityType = byteBuf.readIdentifier();
        String langKey = byteBuf.readUtf();
        return new AltarRecipe(group, category, ingredients, power, result, entityType, langKey);
    }

    private static void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, AltarRecipe altarRecipe) {
        friendlyByteBuf.writeUtf(altarRecipe.group());
        friendlyByteBuf.writeEnum(altarRecipe.getCategory());
        friendlyByteBuf.writeVarInt(altarRecipe.getIngredients().size());
        for (Ingredient ingredient : altarRecipe.getIngredients()) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(friendlyByteBuf, ingredient);
        }
        friendlyByteBuf.writeFloat(altarRecipe.getPower());
        ItemStackTemplate.STREAM_CODEC.encode(friendlyByteBuf, altarRecipe.getResultTemplate());
        friendlyByteBuf.writeIdentifier(altarRecipe.getEntityType());
        friendlyByteBuf.writeUtf(altarRecipe.getLangKey());
    }

    public static final RecipeSerializer<AltarRecipe> SERIALIZER = new RecipeSerializer<>(CODEC,
            StreamCodec.of(AltarRecipeSerializer::toNetwork, AltarRecipeSerializer::fromNetwork));

    private AltarRecipeSerializer() {
    }
}
