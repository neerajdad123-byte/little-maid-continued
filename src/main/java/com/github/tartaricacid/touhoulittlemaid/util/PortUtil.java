package com.github.tartaricacid.touhoulittlemaid.util;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ARGB;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRule;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Little Maid Continued: small helpers that replace APIs removed between MC 1.21.1 and 26.x.
 */
public final class PortUtil {
    /**
     * Reads or writes the whole entity / block entity record as one {@link CompoundTag}.
     */
    private static final MapCodec<CompoundTag> ROOT = MapCodec.assumeMapUnsafe(CompoundTag.CODEC);

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
     * Replacement for {@code item instanceof TieredItem} (tools and swords became data components in 1.21.5).
     */
    public static boolean isTieredTool(ItemStack stack) {
        return stack.has(DataComponents.TOOL) || stack.has(DataComponents.WEAPON) || stack.is(ItemTags.SWORDS);
    }

    /**
     * Replacement for the removed {@code NbtUtils#writeBlockPos(BlockPos)}.
     */
    public static Tag writeBlockPos(BlockPos pos) {
        return BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).getOrThrow();
    }

    /**
     * Bridges {@link ValueInput} back to the CompoundTag based load code of 1.21.1.
     */
    public static CompoundTag readRoot(ValueInput input) {
        return input.read(ROOT).orElseGet(CompoundTag::new);
    }

    /**
     * Bridges CompoundTag based save code of 1.21.1 to {@link ValueOutput}; keys are merged into the record.
     */
    public static void writeRoot(ValueOutput output, CompoundTag tag) {
        if (!tag.isEmpty()) {
            output.store(ROOT, tag);
        }
    }

    /**
     * Replacement for the removed {@code ItemStack#getDrinkingSound()/getEatingSound()}.
     */
    public static SoundEvent consumeSound(ItemStack stack) {
        Consumable consumable = stack.get(DataComponents.CONSUMABLE);
        return consumable == null ? SoundEvents.GENERIC_EAT.value() : consumable.sound().value();
    }

    /**
     * Replacement for the removed {@code FoodProperties#effects()}: status effects applied when the item is consumed.
     */
    public static List<MobEffectInstance> foodEffects(ItemStack stack) {
        Consumable consumable = stack.get(DataComponents.CONSUMABLE);
        if (consumable == null) {
            return List.of();
        }
        List<MobEffectInstance> effects = new ArrayList<>();
        for (ConsumeEffect effect : consumable.onConsumeEffects()) {
            if (effect instanceof ApplyStatusEffectsConsumeEffect apply) {
                effects.addAll(apply.effects());
            }
        }
        return effects;
    }

    /**
     * Replacement for the removed {@code NbtUtils#loadUUID(Tag)} (int array form).
     */
    public static UUID loadUUID(Tag tag) {
        return UUIDUtil.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
    }

    /**
     * Replacement for the removed {@code EntityType#byString(String)}.
     */
    public static Optional<EntityType<?>> entityTypeByString(String id) {
        Identifier key = Identifier.tryParse(id);
        return key == null ? Optional.empty() : BuiltInRegistries.ENTITY_TYPE.getOptional(key);
    }

    /**
     * Replacement for MC 1.21.1 {@code Entity#load(CompoundTag)}.
     */
    public static void loadEntity(Entity entity, CompoundTag tag) {
        entity.load(TagValueInput.create(ProblemReporter.DISCARDING, entity.registryAccess(), tag));
    }

    /**
     * Replacement for MC 1.21.1 {@code Entity#saveWithoutId(CompoundTag)}.
     */
    public static CompoundTag saveEntity(Entity entity) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());
        entity.saveWithoutId(output);
        return output.buildResult();
    }

    /**
     * Replacement for MC 1.21.1 {@code ItemStackHandler#serializeNBT(Provider)}; same NBT layout ("Items", "Size").
     */
    public static CompoundTag serializeItems(ItemStackHandler handler, HolderLookup.Provider provider) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        handler.serialize(output);
        return output.buildResult();
    }

    /**
     * Replacement for MC 1.21.1 {@code ItemStackHandler#deserializeNBT(Provider, CompoundTag)}.
     */
    public static void deserializeItems(ItemStackHandler handler, HolderLookup.Provider provider, CompoundTag tag) {
        handler.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, provider, tag));
    }

    /**
     * Replacement for the removed {@code Component.Serializer#toJson(Component, Provider)}.
     */
    public static String componentToJson(Component component, HolderLookup.Provider provider) {
        return ComponentSerialization.CODEC.encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), component).getOrThrow().toString();
    }

    /**
     * Replacement for the removed {@code Component.Serializer#fromJson(String, Provider)}; null when unparsable.
     */
    @Nullable
    public static MutableComponent componentFromJson(String json, HolderLookup.Provider provider) {
        try {
            return ComponentSerialization.CODEC.parse(provider.createSerializationContext(JsonOps.INSTANCE), JsonParser.parseString(json))
                    .result().map(Component::copy).orElse(null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Game rules only exist on the server in MC 26.x; returns false on the client.
     */
    public static boolean gameRule(Level level, GameRule<Boolean> rule) {
        return level instanceof ServerLevel serverLevel && serverLevel.getGameRules().get(rule);
    }

    /**
     * Registry access for save code that has no lookup of its own.
     */
    public static HolderLookup.Provider registries() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.registryAccess();
        }
        if (FMLEnvironment.getDist().isClient()) {
            HolderLookup.Provider client = ClientPortUtil.registries();
            if (client != null) {
                return client;
            }
        }
        return RegistryAccess.EMPTY;
    }
}
