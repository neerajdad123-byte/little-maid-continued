package com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Little Maid Continued: replacement for MC 1.21.1 {@code BlockEntityWithoutLevelRenderer}.
 * <p>
 * Registered as a MC 26.x special item model through {@link LegacySpecialModelRenderer}; the item model
 * JSON in {@code assets/touhou_little_maid/items/} selects it with {@code "type": "minecraft:special"}.
 */
public abstract class LegacyItemStackRenderer {
    public abstract void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                                      MultiBufferSource buffer, int packedLight, int packedOverlay);
}
