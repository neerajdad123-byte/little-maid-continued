package com.github.tartaricacid.touhoulittlemaid.item;

import java.util.ArrayList;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

@Deprecated(since = "1.1.13")
public class ItemMonsterList extends Item {
    public ItemMonsterList() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flagIn) {
        List<Component> tooltip = new ArrayList<>();
        try {
            tooltip.add(Component.translatable("tooltips.touhou_little_maid.monster_list.desc").withStyle(ChatFormatting.DARK_RED));
        } finally {
            tooltip.forEach(tooltipAdder);
        }
    }
}