package com.github.tartaricacid.touhoulittlemaid.util;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import java.util.Optional;
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
import net.minecraft.util.ProblemReporter;
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
