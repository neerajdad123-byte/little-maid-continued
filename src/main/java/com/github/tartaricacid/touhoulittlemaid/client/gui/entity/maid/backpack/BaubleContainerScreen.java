package com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.backpack;

import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.AbstractMaidContainerGui;
import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.BaubleButton;
import com.github.tartaricacid.touhoulittlemaid.compat.curios.CuriosCompat;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.TabIndex;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.backpack.BaubleContainer;
import com.github.tartaricacid.touhoulittlemaid.network.message.OpenMaidGuiPackage;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
public class BaubleContainerScreen extends AbstractMaidContainerGui<BaubleContainer> implements IBackpackContainerScreen {
    private static final Identifier BAUBLE_BG = Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "textures/gui/maid_gui_bauble.png");
    private final EntityMaid maid;
    private final int favorabilityLevel;

    public BaubleContainerScreen(BaubleContainer container, Inventory inv, Component titleIn) {
        super(container, inv, titleIn);
        this.maid = menu.getMaid();
        this.favorabilityLevel = this.maid.getFavorabilityManager().getLevel();
    }

    @Override
    protected void initAdditionWidgets() {
        BaubleButton baubleButton = new BaubleButton(leftPos, topPos, true, btn -> {
            OpenMaidGuiPackage message = new OpenMaidGuiPackage(maid.getId(), TabIndex.MAIN);
            ClientPacketDistributor.sendToServer(message);
        });
        this.addRenderableWidget(baubleButton);

        // 添加 curios 兼容按钮
        if (CuriosCompat.isLoadedOrEnable()) {
            this.addRenderableWidget(this.getCuriosButton(maid, leftPos, topPos));
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int x, int y, float partialTicks) {
        super.extractBackground(graphics, x, y, partialTicks);
        super.extractBackground(graphics, x, y, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BAUBLE_BG, leftPos + 85, topPos + 36, 0, 0, 165, 128, 256, 256);

        // 0 级和 1 级：只有前两层
        // 2 级，前四层
        // 3 级及以上，全部开放
        if (favorabilityLevel < 2) {
            graphics.fill(leftPos + 152, topPos + 81, leftPos + 240, topPos + 115, 0xaa222222);
            graphics.blit(RenderPipelines.GUI_TEXTURED, BAUBLE_BG, leftPos + 190, topPos + 92, 165, 0, 11, 11, 256, 256);
        }
        if (favorabilityLevel < 3) {
            graphics.fill(leftPos + 152, topPos + 117, leftPos + 240, topPos + 151, 0xaa222222);
            graphics.blit(RenderPipelines.GUI_TEXTURED, BAUBLE_BG, leftPos + 190, topPos + 127, 165, 0, 11, 11, 256, 256);
        }
    }

    @Override
    protected void renderAdditionTransTooltip(GuiGraphicsExtractor graphics, int x, int y) {
        if (favorabilityLevel < 2) {
            if (leftPos + 152 <= x && x < leftPos + 240 && topPos + 81 <= y && y < topPos + 115) {
                graphics.setTooltipForNextFrame(font, Component.translatable("gui.touhou_little_maid.bauble_button.need_favorability_level", 2), x, y);
            }
        }
        if (favorabilityLevel < 3) {
            if (leftPos + 152 <= x && x < leftPos + 240 && topPos + 117 <= y && y < topPos + 151) {
                graphics.setTooltipForNextFrame(font, Component.translatable("gui.touhou_little_maid.bauble_button.need_favorability_level", 3), x, y);
            }
        }
    }
}
