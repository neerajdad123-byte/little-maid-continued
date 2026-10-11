package com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button;

import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.PlainButton;
import net.minecraft.client.renderer.RenderPipelines;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.client.gui.ITooltipButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * 女仆界面侧边栏按钮
 */
public class MaidSideTabButton extends PlainButton implements ITooltipButton {
    private static final Identifier SIDE = Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "textures/gui/maid_gui_side.png");
    private static final int V_OFFSET = 107;
    private final List<Component> tooltips;
    private final int top;

    public MaidSideTabButton(int x, int y, int top, OnPress onPressIn, List<Component> tooltips) {
        super(Button.builder(Component.empty(), onPressIn).pos(x, y).size(26, 24));
        this.top = V_OFFSET + top;
        this.tooltips = tooltips;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if (!this.active) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, SIDE, this.getX() + 2, this.getY(), 209, top, this.width, this.height, 256, 256);
        }
        // 193, 111
        graphics.blit(RenderPipelines.GUI_TEXTURED, SIDE, this.getX() + 6, this.getY() + 4, 193, top + 4, 16, 16, 256, 256);
    }

    @Override
    public boolean isTooltipHovered() {
        return this.isHovered();
    }

    @Override
    public void renderTooltip(GuiGraphicsExtractor graphics, Minecraft mc, int mouseX, int mouseY) {
        graphics.setComponentTooltipForNextFrame(mc.font, this.tooltips, mouseX, mouseY);
    }
}