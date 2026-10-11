package com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.LegacyDraw;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.block.BlockGomoku;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.SimpleBedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.resource.BedrockModelLoader;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityShrine;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy.LegacyBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TileEntityShrineRenderer implements LegacyBlockEntityRenderer<TileEntityShrine> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "textures/bedrock/block/shrine.png");
    private final SimpleBedrockModel<Entity> model;

    public TileEntityShrineRenderer(BlockEntityRendererProvider.Context context) {
        model = BedrockModelLoader.getModel(BedrockModelLoader.SHRINE);
    }

    @Override
    public void render(TileEntityShrine shrine, float partialTick, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        Direction facing = shrine.getBlockState().getValue(BlockGomoku.FACING);
        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));
        poseStack.mulPose(Axis.YN.rotationDegrees(180 - facing.get2DDataValue() * 90));
        VertexConsumer buffer = bufferIn.getBuffer(RenderTypes.entityCutout(TEXTURE));
        model.renderToBuffer(poseStack, buffer, combinedLightIn, combinedOverlayIn);
        poseStack.popPose();

        Level level = shrine.getLevel();
        if (level == null) {
            return;
        }
        ItemStack stack = shrine.getStorageItem();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.85, 0.5);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        float deg = (level.getGameTime() + partialTick) % 360;
        poseStack.mulPose(Axis.YN.rotationDegrees(deg));
        LegacyDraw.item(stack, ItemDisplayContext.GROUND, combinedLightIn, combinedOverlayIn, poseStack, bufferIn, level, 0);
        poseStack.popPose();
    }
}
