package com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.SubmitBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Little Maid Continued: runs a {@link LegacyItemStackRenderer} as a MC 26.x special item model.
 */
public final class LegacySpecialModelRenderer implements SpecialModelRenderer<ItemStack> {
    private final LegacyItemStackRenderer renderer;

    public LegacySpecialModelRenderer(LegacyItemStackRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void submit(@Nullable ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords,
                       boolean hasFoil, int outlineColor) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        try (SubmitBufferSource buffers = new SubmitBufferSource(collector)) {
            // The display context transform is already applied by the item model; the renderer only needs the stack
            this.renderer.renderByItem(stack, ItemDisplayContext.NONE, poseStack, buffers, lightCoords, overlayCoords);
        }
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        // A full block: used by the game for GUI framing
        output.accept(new Vector3f(0, 0, 0));
        output.accept(new Vector3f(1, 1, 1));
    }

    @Override
    public ItemStack extractArgument(ItemStack stack) {
        return stack.copyWithCount(1);
    }

    /**
     * Unbaked form registered with {@code RegisterSpecialModelRendererEvent}; holds no JSON fields.
     */
    public static final class Unbaked implements SpecialModelRenderer.Unbaked<ItemStack> {
        private final Supplier<LegacyItemStackRenderer> factory;
        private final MapCodec<Unbaked> codec;

        public Unbaked(Supplier<LegacyItemStackRenderer> factory) {
            this.factory = factory;
            this.codec = MapCodec.unit(this);
        }

        public MapCodec<Unbaked> codec() {
            return this.codec;
        }

        @Override
        public SpecialModelRenderer<ItemStack> bake(SpecialModelRenderer.BakingContext context) {
            return new LegacySpecialModelRenderer(this.factory.get());
        }

        @Override
        public MapCodec<Unbaked> type() {
            return this.codec;
        }
    }
}
