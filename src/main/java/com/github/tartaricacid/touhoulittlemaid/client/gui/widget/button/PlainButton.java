package com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Little Maid Continued: MC 26.x made {@link Button} abstract; this restores the plain vanilla look
 * (sprite + label) as a base for the mod's buttons.
 */
public class PlainButton extends Button {
    public PlainButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public PlainButton(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message, onPress, createNarration);
    }

    public PlainButton(Builder builder) {
        super(builder);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        this.extractDefaultSprite(graphics);
        this.renderString(graphics, Minecraft.getInstance().font, this.getFGColor());
    }

    /**
     * MC 1.21.1 label hook, kept so subclasses can still draw their own label.
     */
    public void renderString(GuiGraphicsExtractor graphics, Font font, int color) {
        this.extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
    }
}
