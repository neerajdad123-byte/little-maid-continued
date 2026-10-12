package com.github.tartaricacid.touhoulittlemaid.client.event;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.LegacyDraw;
import com.github.tartaricacid.touhoulittlemaid.util.RenderHelper;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.item.ItemWirelessIO;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = TouhouLittleMaid.MOD_ID, value = Dist.CLIENT)
public final class WirelessIORenderEvent {
    @SubscribeEvent
    public static void onRender(SubmitCustomGeometryEvent event) {
        RenderHelper.draw(event, () -> render(event));
    }

    private static void render(SubmitCustomGeometryEvent event) {
        if (true) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                return;
            }
            ItemStack mainStack = mc.player.getMainHandItem();
            if (mainStack.getItem() != InitItems.WIRELESS_IO.get()) {
                return;
            }
            BlockPos pos = ItemWirelessIO.getBindingPos(mainStack);
            if (pos == null) {
                return;
            }
            Vec3 position = event.getLevelRenderState().cameraRenderState.pos.reverse();
            AABB aabb = new AABB(pos).move(position);
            VertexConsumer buffer = RenderHelper.BUFFERS.getBuffer(RenderTypes.lines());
            LegacyDraw.lineBox(event.getPoseStack(), RenderHelper.BUFFERS, aabb, 1.0F, 0, 0, 1.0F);
        }
    }
}
