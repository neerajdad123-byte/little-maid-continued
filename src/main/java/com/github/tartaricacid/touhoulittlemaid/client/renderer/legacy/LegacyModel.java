package com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

/**
 * Little Maid Continued: the MC 1.21.1 {@code net.minecraft.client.model.Model} API.
 * <p>
 * MC 26.x models are bound to render states and vanilla {@code ModelPart} trees. The mod's bedrock models
 * build their own geometry, so they keep this immediate-mode API and are drawn through
 * {@link com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.SubmitBufferSource}.
 */
public abstract class LegacyModel {
    protected final Function<Identifier, RenderType> renderType;

    public LegacyModel(Function<Identifier, RenderType> renderType) {
        this.renderType = renderType;
    }

    public LegacyModel() {
        this(RenderTypes::entityCutout);
    }

    public final RenderType renderType(Identifier texture) {
        return this.renderType.apply(texture);
    }

    public abstract void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color);

    public final void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        this.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, -1);
    }
}
