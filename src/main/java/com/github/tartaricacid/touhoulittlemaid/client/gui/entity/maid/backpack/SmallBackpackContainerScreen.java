package com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.backpack;

import net.minecraft.client.renderer.RenderPipelines;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.AbstractMaidContainerGui;
import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.BaubleButton;
import com.github.tartaricacid.touhoulittlemaid.compat.curios.CuriosCompat;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.backpack.SmallBackpackContainer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
public class SmallBackpackContainerScreen extends AbstractMaidContainerGui<SmallBackpackContainer> implements IBackpackContainerScreen {
    private static final Identifier BACKPACK = Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "textures/gui/maid_gui_backpack.png");
    private final EntityMaid maid;

    public SmallBackpackContainerScreen(SmallBackpackContainer container, Inventory inv, Component titleIn) {
        super(container, inv, titleIn);
        this.maid = menu.getMaid();
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
        graphics.fill(leftPos + 142, topPos + 81, leftPos + 250, topPos + 117, 0xaa222222);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKPACK, leftPos + 190, topPos + 92, 165, 0, 11, 11, 256, 256);
        graphics.fill(leftPos + 142, topPos + 122, leftPos + 250, topPos + 158, 0xaa222222);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKPACK, leftPos + 190, topPos + 133, 165, 0, 11, 11, 256, 256);
    }
}
