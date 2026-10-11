package com.github.tartaricacid.touhoulittlemaid.compat.slashblade;

import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.BedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.MultiBufferSource;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.ILocationModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

// Little Maid Continued: no-op stub. The real integration lives in src/compat_disabled and is
// re-enabled once the mod has a build for this Minecraft version.
public class SlashBladeRender {
    public static void renderSlashBlade(PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, ItemStack stack) {
    }

    public static void renderMaidBackSlashBlade(PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, ItemStack stack) {
    }

    public static void renderGeckoMaidBackSlashBlade(PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, ItemStack stack) {
    }

    public static void renderMaidMainhandSlashBlade(Mob maid, BedrockModel<Mob> model, PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, ItemStack stack, float partialTicks) {
    }

    public static void renderMaidMainhandSlashBlade(LivingEntity maid, ILocationModel model, PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, ItemStack stack, float partialTicks) {
    }

    public static void renderMaidOffhandSlashBlade(BedrockModel<?> model, PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, ItemStack stack) {
    }

    public static void renderMaidOffhandSlashBlade(ILocationModel model, PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, ItemStack stack) {
    }
}
