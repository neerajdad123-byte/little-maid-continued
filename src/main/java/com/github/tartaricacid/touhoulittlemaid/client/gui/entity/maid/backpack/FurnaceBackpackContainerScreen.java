package com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.backpack;

import net.minecraft.client.renderer.RenderPipelines;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.AbstractMaidContainerGui;
import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.BaubleButton;
import com.github.tartaricacid.touhoulittlemaid.compat.curios.CuriosCompat;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.backpack.FurnaceBackpackContainer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
public class FurnaceBackpackContainerScreen extends AbstractMaidContainerGui<FurnaceBackpackContainer> implements IBackpackContainerScreen {
    private static final Identifier BACKPACK = Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "textures/gui/maid_furnace.png");

    public FurnaceBackpackContainerScreen(FurnaceBackpackContainer container, Inventory inv, Component titleIn) {
        super(container, inv, titleIn);
    }

    @Override
    protected void initAdditionWidgets() {
        BaubleButton button = this.getBaubleButton(maid, leftPos, topPos);
        this.addRenderableWidget(button);

        // 添加 curios 兼容按钮
        if (CuriosCompat.isLoadedOrEnable()) {
            this.addRenderableWidget(this.getCuriosButton(maid, leftPos, topPos));
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
        super.extractBackground(graphics, x, y, partialTicks);
        super.extractBackground(graphics, x, y, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKPACK, leftPos + 85, topPos + 36, 0, 0, 165, 128, 256, 256);
        if (this.menu.isLit()) {
            int litProgress = this.menu.getLitProgress();
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKPACK, leftPos + 161, topPos + 122 + 12 - litProgress, 165, 12 - litProgress, 14, litProgress + 1, 256, 256);
        }
        int burnProgress = this.menu.getBurnProgress();
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKPACK, leftPos + 184, topPos + 120, 165, 14, burnProgress + 1, 16, 256, 256);
    }
}
