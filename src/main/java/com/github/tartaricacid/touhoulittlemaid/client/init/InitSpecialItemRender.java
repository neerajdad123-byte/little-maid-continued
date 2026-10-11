package com.github.tartaricacid.touhoulittlemaid.client.init;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.VanillaConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import org.jspecify.annotations.Nullable;

import java.util.function.BooleanSupplier;

/**
 * Little Maid Continued: config-driven item texture swaps (totem of undying -> life point,
 * bottle o' enchanting -> point item).
 * <p>
 * MC 1.21.1 swapped baked models at runtime. MC 26.x selects models in item definitions, so the
 * definitions in {@code assets/minecraft/items/} use a {@code minecraft:condition} model with the
 * {@code touhou_little_maid:config} property registered here.
 */
@EventBusSubscriber(value = Dist.CLIENT)
public final class InitSpecialItemRender {
    private InitSpecialItemRender() {
    }

    @SubscribeEvent
    public static void onRegisterConditional(RegisterConditionalItemModelPropertyEvent event) {
        event.register(Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "config"), ConfigSwitch.MAP_CODEC);
    }

    public enum Option implements StringRepresentable {
        REPLACE_TOTEM("replace_totem", () -> VanillaConfig.REPLACE_TOTEM_TEXTURE.get()),
        REPLACE_XP_BOTTLE("replace_xp_bottle", () -> VanillaConfig.REPLACE_XP_BOTTLE_TEXTURE.get());

        public static final Codec<Option> CODEC = StringRepresentable.fromEnum(Option::values);
        private final String name;
        private final BooleanSupplier value;

        Option(String name, BooleanSupplier value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public record ConfigSwitch(Option option) implements ConditionalItemModelProperty {
        public static final MapCodec<ConfigSwitch> MAP_CODEC = Option.CODEC.fieldOf("option").xmap(ConfigSwitch::new, ConfigSwitch::option);

        @Override
        public boolean get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
            return this.option.value.getAsBoolean();
        }

        @Override
        public MapCodec<ConfigSwitch> type() {
            return MAP_CODEC;
        }
    }
}
