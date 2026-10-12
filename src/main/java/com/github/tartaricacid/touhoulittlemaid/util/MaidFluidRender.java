package com.github.tartaricacid.touhoulittlemaid.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * From JEI: <a href="https://github.com/mezz/JustEnoughItems/blob/1.20/Forge/src/main/java/mezz/jei/forge/platform/FluidHelper.java">...</a>
 * <p>
 * Little Maid Continued: MC 26.x takes fluid sprites and tints from the fluid model; partial tiles are clipped with a scissor.
 */
@OnlyIn(Dist.CLIENT)
public final class MaidFluidRender {
    private static final int TEXTURE_SIZE = 16;

    public static Component getFluidName(String fluidId, int amount) {
        Fluid fluid = BuiltInRegistries.FLUID.getValue(Identifier.parse(fluidId));
        if (amount <= 0 || fluid == null || fluid.isSame(Fluids.EMPTY)) {
            return Component.translatable("tooltips.touhou_little_maid.tank_backpack.empty_fluid");
        }
        return fluid.getFluidType().getDescription();
    }

    public static void drawFluid(GuiGraphicsExtractor graphics, int x, int y, int width, int height, String fluidId, int amount, int capacity) {
        Fluid fluid = BuiltInRegistries.FLUID.getValue(Identifier.parse(fluidId));
        if (amount <= 0 || fluid == null || fluid.isSame(Fluids.EMPTY)) {
            return;
        }
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
        TextureAtlasSprite sprite = model.stillMaterial().sprite();
        int color = model.fluidTintSource() == null ? -1 : ARGB.opaque(model.fluidTintSource().colorAsStack(new FluidStack(fluid, amount)));
        // at least one row of pixels, so the player can see there is something inside
        int scaledAmount = (int) Math.clamp((long) amount * height / capacity, 1, height);

        int top = y + height - scaledAmount;
        graphics.enableScissor(x, top, x + width, y + height);
        for (int tileX = x; tileX < x + width; tileX += TEXTURE_SIZE) {
            for (int tileY = y + height - TEXTURE_SIZE; tileY > top - TEXTURE_SIZE; tileY -= TEXTURE_SIZE) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, tileX, tileY, TEXTURE_SIZE, TEXTURE_SIZE, color);
            }
        }
        graphics.disableScissor();
    }
}
