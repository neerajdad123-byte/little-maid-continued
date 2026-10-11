package com.github.tartaricacid.touhoulittlemaid.compat.simplehats;

import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.BedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.MultiBufferSource;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.processor.ILocationBone;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// Little Maid Continued: no-op stub. The real integration lives in src/compat_disabled and is
// re-enabled once the mod has a build for this Minecraft version.
public class SimpleHatsCompat {
    public static void init() {
    }

    public static void renderHat(PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, Mob mob, ItemStack stack, BedrockModel<Mob> model) {
    }

    public static void renderGeckoHat(PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, Mob mob, ItemStack stack, List<? extends ILocationBone> model) {
    }
}
