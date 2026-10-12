package com.github.tartaricacid.touhoulittlemaid.item;

import java.util.ArrayList;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.List;

public class ItemChairShow extends Item {
    public ItemChairShow() {
        super((new Properties()).stacksTo(1));
    }


    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pLevel, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag pIsAdvanced) {
        List<Component> tooltip = new ArrayList<>();
        try {
            tooltip.add((Component.translatable("tooltips.touhou_little_maid.chair_show.desc")).withStyle(ChatFormatting.GRAY));
        } finally {
            tooltip.forEach(tooltipAdder);
        }
    }
}