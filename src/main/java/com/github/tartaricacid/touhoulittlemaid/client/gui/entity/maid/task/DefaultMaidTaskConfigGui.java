package com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.task;

import com.github.tartaricacid.touhoulittlemaid.util.PortUtil;
import net.minecraft.client.renderer.RenderPipelines;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.task.TaskConfigContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
public class DefaultMaidTaskConfigGui extends MaidTaskConfigGui<TaskConfigContainer> {
    private static final Identifier BG = Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "textures/gui/default_task_config.png");

    public DefaultMaidTaskConfigGui(TaskConfigContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
        super.extractBackground(graphics, x, y, partialTicks);
        super.extractBackground(graphics, x, y, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BG, leftPos + 80, topPos + 28, 0, 0, imageWidth, 137, 256, 256);
    }

    @Override
    protected void renderAddition(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.textWithWordWrap(font, Component.translatable("gui.touhou_little_maid.default_task_config.title"), leftPos + 88, topPos + 38, 160, PortUtil.color(ChatFormatting.DARK_GRAY));
    }
}