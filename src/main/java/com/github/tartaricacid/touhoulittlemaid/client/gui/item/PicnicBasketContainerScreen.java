package com.github.tartaricacid.touhoulittlemaid.client.gui.item;

import net.minecraft.client.renderer.RenderPipelines;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.other.PicnicBasketContainer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class PicnicBasketContainerScreen extends AbstractContainerScreen<PicnicBasketContainer> {
    private static final Identifier CONTAINER_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");

    public PicnicBasketContainerScreen(PicnicBasketContainer container, Inventory inv, Component titleIn) {
        super(container, inv, titleIn, 176, 132);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        this.extractTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        int middleX = (this.width - this.imageWidth) / 2;
        int middleY = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, middleX, middleY, 0, 0, this.imageWidth, 35, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, middleX, middleY + 35, 0, 126, this.imageWidth, 96, 256, 256);
    }
}
