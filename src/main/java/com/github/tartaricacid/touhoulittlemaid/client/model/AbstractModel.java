package com.github.tartaricacid.touhoulittlemaid.client.model;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy.LegacyEntityModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public abstract class AbstractModel<E extends Entity> extends LegacyEntityModel<E> {
    public AbstractModel(Function<Identifier, RenderType> pRenderType) {
        super(pRenderType);
    }

    protected AbstractModel() {
        this(RenderTypes::entityCutout);
    }

    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer buffer, int light, int overlay, float r, float g, float b, float a) {
        this.renderToBuffer(poseStack, buffer, light, overlay);
    }
}
