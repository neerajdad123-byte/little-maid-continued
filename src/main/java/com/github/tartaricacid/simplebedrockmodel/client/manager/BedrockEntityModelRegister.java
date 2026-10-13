package com.github.tartaricacid.simplebedrockmodel.client.manager;

import net.neoforged.neoforge.client.resources.VanillaClientListeners;
import com.github.tartaricacid.simplebedrockmodel.SimpleBedrockModel;
import com.github.tartaricacid.simplebedrockmodel.client.bedrock.AbstractBedrockEntityModel;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

import java.util.Set;

@SuppressWarnings({"unchecked", "rawtypes"})
@EventBusSubscriber(modid = TouhouLittleMaid.MOD_ID, value = Dist.CLIENT)
public class BedrockEntityModelRegister<T extends AbstractBedrockEntityModel<? extends Entity>> {
    public static BedrockEntityModelRegister INSTANCE = null;
    private final BedrockEntityModelSet<T> modelSet;

    private BedrockEntityModelRegister(BedrockEntityModelSet<T> modelSet) {
        this.modelSet = modelSet;
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListenersEvent(AddClientReloadListenersEvent event) {
        INSTANCE = new BedrockEntityModelRegister<>(new BedrockEntityModelSet<>());
        ModLoader.postEvent(new BedrockEntityModelRegisterEvent(INSTANCE.modelSet));
        // 将注册冻结
        INSTANCE.modelSet.immutableKnowLocations();
        // entity and block entity renderers read these models when they are created
        Identifier key = Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "bedrock_models");
        event.addListener(key, INSTANCE.modelSet);
        event.addDependency(key, VanillaClientListeners.ENTITY_RENDERER);
        event.addDependency(key, VanillaClientListeners.BLOCK_ENTITY_RENDERER);
    }

    public AbstractBedrockEntityModel<? extends Entity> getModel(Identifier location) {
        return modelSet.getModels().get(location);
    }

    public Set<Identifier> getAllModelKeys() {
        return modelSet.getModels().keySet();
    }
}
