package com.github.tartaricacid.touhoulittlemaid.init.registry;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.datapack.resources.BoardStateDataReloadListener;
import com.github.tartaricacid.touhoulittlemaid.datapack.resources.KaomojiDataReloadListener;
import com.github.tartaricacid.touhoulittlemaid.datapack.resources.SkillsDataReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = TouhouLittleMaid.MOD_ID)
public class DatapackRegistry {
    @SubscribeEvent
    public static void onAddReloadListenerEvent(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "kaomoji"), new KaomojiDataReloadListener());
        event.addListener(Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "board_state"), new BoardStateDataReloadListener());
        event.addListener(Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "skills"), new SkillsDataReloadListener());
    }
}
