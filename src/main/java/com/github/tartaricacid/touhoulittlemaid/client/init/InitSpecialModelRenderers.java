package com.github.tartaricacid.touhoulittlemaid.client.init;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy.LegacyItemStackRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy.LegacySpecialModelRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity.PicnicBasketRender;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity.TileEntityEntityPlaceholderRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity.TileEntityItemStackChairRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity.TileEntityItemStackGarageKitRenderer;
import com.google.common.base.Suppliers;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

import java.util.function.Supplier;

/**
 * Little Maid Continued: 3D item renderers (MC 1.21.1 BlockEntityWithoutLevelRenderer) registered as
 * MC 26.x special item models. Item definitions in {@code assets/touhou_little_maid/items/} refer to these ids.
 */
@EventBusSubscriber(value = Dist.CLIENT)
public final class InitSpecialModelRenderers {
    private InitSpecialModelRenderers() {
    }

    @SubscribeEvent
    public static void onRegister(RegisterSpecialModelRendererEvent event) {
        register(event, "chair", TileEntityItemStackChairRenderer::new);
        register(event, "garage_kit", TileEntityItemStackGarageKitRenderer::new);
        register(event, "picnic_basket", PicnicBasketRender::new);
        register(event, "entity_placeholder", TileEntityEntityPlaceholderRenderer::new);
    }

    private static void register(RegisterSpecialModelRendererEvent event, String name, Supplier<LegacyItemStackRenderer> factory) {
        // One renderer instance is shared by every baked model of the item
        Supplier<LegacyItemStackRenderer> shared = Suppliers.memoize(factory::get);
        Identifier id = Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, name);
        event.register(id, new LegacySpecialModelRenderer.Unbaked(shared).codec());
    }
}
