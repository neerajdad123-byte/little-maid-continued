package com.github.tartaricacid.touhoulittlemaid.client.renderer.entity;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.SimpleBedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.resource.BedrockModelLoader;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBox;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy.LegacyEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.stream.IntStream;

import static com.github.tartaricacid.touhoulittlemaid.client.resource.BedrockModelLoader.CAKE_BOX;

public class EntityBoxRender extends LegacyEntityRenderer<EntityBox> {
    private final List<Identifier> texturesGroup = Lists.newArrayList();
    private final SimpleBedrockModel<EntityBox> boxModel;

    public EntityBoxRender(EntityRendererProvider.Context manager) {
        super(manager);
        boxModel = BedrockModelLoader.getModel(CAKE_BOX);
        IntStream.range(0, EntityBox.MAX_TEXTURE_SIZE).forEach(this::addBoxTexture);
    }

    @Override
    public void render(EntityBox entityBox, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0, -1.501, 0.0);
        boxModel.setupAnim(entityBox, 0, 0, -0.1f, 0, 0);
        RenderType renderType = RenderTypes.entityCutout(getTextureLocation(entityBox));
        VertexConsumer buffer = bufferIn.getBuffer(renderType);
        boxModel.renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    @Override
    public Identifier getTextureLocation(EntityBox entity) {
        return texturesGroup.get(entity.getTextureIndex());
    }

    private void addBoxTexture(int index) {
        String fileName = String.format("textures/bedrock/entity/cake_box/cake_box_%s.png", index);
        texturesGroup.add(Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, fileName));
    }
}
