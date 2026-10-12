package com.github.tartaricacid.touhoulittlemaid.crafting;

import net.minecraft.world.entity.EntityTypes;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBox;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitDataComponent;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.tartaricacid.touhoulittlemaid.init.InitRecipes;
import com.github.tartaricacid.touhoulittlemaid.item.ItemFilm;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import com.github.tartaricacid.touhoulittlemaid.util.PortUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class AltarRecipe implements Recipe<CraftingInput> {
    private final String group;
    private final List<Ingredient> ingredients;
    private @Nullable PlacementInfo placementInfo;
    private final CraftingBookCategory category;
    private final float power;
    private final ItemStack result;
    private final Identifier entityType;
    private final String langKey;

    public AltarRecipe(String group, CraftingBookCategory category, List<Ingredient> ingredients, float power, ItemStack result, Identifier entityType, String langKey) {
        this.ingredients = ingredients;
        this.group = group;
        this.category = category;
        this.power = power;
        this.result = result;
        this.entityType = entityType;
        this.langKey = langKey;
    }

    public Identifier getId() {
        return InitRecipes.ALTAR_CRAFTING.getId();
    }

    public String getRecipeString() {
        String recipeId = this.result.get(InitDataComponent.RECIPES_ID_TAG);
        return Objects.requireNonNullElse(recipeId, "spawn_box");
    }

    public boolean isItemCraft() {
        return entityType.equals(BuiltInRegistries.ENTITY_TYPE.getKey(EntityTypes.ITEM));
    }

    public void spawnOutputEntity(ServerLevel world, BlockPos pos, @Nullable List<ItemStack> list) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(entityType);

        if (type == EntityTypes.ITEM) {
            this.spawnItem(world, pos);
            return;
        }

        if (type == InitEntities.BOX.get()) {
            this.spawnBoxMaid(world, pos);
            return;
        }

        if (type == InitEntities.MAID.get()) {
            this.rebornMaid(world, pos, list);
            return;
        }

        // 生成类型为 EVENT 也许更合适
        type.spawn(world, pos, EntitySpawnReason.EVENT);
    }

    private void rebornMaid(ServerLevel world, BlockPos pos, @Nullable List<ItemStack> list) {
        ItemStack itemFilm = ItemStack.EMPTY;
        if (list != null) {
            itemFilm = list.stream().filter(stack -> stack.getItem() instanceof ItemFilm).findFirst().orElse(ItemStack.EMPTY);
        }
        EntityMaid maid = new EntityMaid(world);
        CustomData compoundData = itemFilm.get(InitDataComponent.MAID_INFO);
        if (compoundData != null) {
            CompoundTag maidCompound = compoundData.copyTag();
            // a copied film would otherwise spawn a second maid with the same UUID, which the level drops
            maidCompound.remove("UUID");
            PortUtil.loadEntity(maid, maidCompound);
        } else {
            maid.finalizeSpawn(world, world.getCurrentDifficultyAt(pos), EntitySpawnReason.SPAWN_ITEM_USE, null);
        }
        maid.setPos(pos.getX(), pos.getY(), pos.getZ());
        world.addFreshEntity(maid);
    }

    private void spawnBoxMaid(ServerLevel world, BlockPos pos) {
        EntityBox box = new EntityBox(world);
        box.setPos(pos.getX(), pos.getY(), pos.getZ());

        EntityMaid maid = new EntityMaid(world);
        maid.setPos(pos.getX(), pos.getY(), pos.getZ());
        maid.finalizeSpawn(world, world.getCurrentDifficultyAt(pos), EntitySpawnReason.SPAWN_ITEM_USE, null);
        maid.startRiding(box, true, true);

        world.tryAddFreshEntityWithPassengers(box);
    }

    private void spawnItem(ServerLevel world, BlockPos pos) {
        ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), this.result.copy());
        world.addFreshEntity(itemEntity);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != this.ingredients.size()) {
            return false;
        }
        List<ItemStack> nonEmptyItems = input.items().stream().filter(stack -> !stack.isEmpty()).toList();
        return RecipeMatcher.findMatches(nonEmptyItems, this.ingredients) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return this.result.copy();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return group;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(this.ingredients);
        }
        return this.placementInfo;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public @NotNull RecipeType<AltarRecipe> getType() {
        return InitRecipes.ALTAR_CRAFTING.get();
    }

    @Override
    public RecipeSerializer<AltarRecipe> getSerializer() {
        return InitRecipes.ALTAR_RECIPE_SERIALIZER.get();
    }

    public float getPower() {
        return power;
    }

    public CraftingBookCategory getCategory() {
        return category;
    }

    public ItemStack getResult() {
        return result;
    }

    public Identifier getEntityType() {
        return entityType;
    }

    public String getLangKey() {
        return langKey;
    }
}
