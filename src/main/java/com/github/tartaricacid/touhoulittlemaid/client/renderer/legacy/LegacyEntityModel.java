package com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

/**
 * Little Maid Continued: the MC 1.21.1 {@code net.minecraft.client.model.EntityModel} API.
 */
public abstract class LegacyEntityModel<T extends Entity> extends LegacyModel {
    public float attackTime;
    public boolean riding;
    public boolean young;

    protected LegacyEntityModel() {
        this(RenderTypes::entityCutout);
    }

    protected LegacyEntityModel(Function<Identifier, RenderType> renderType) {
        super(renderType);
    }

    public abstract void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch);

    public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTick) {
    }

    public void copyPropertiesTo(LegacyEntityModel<T> otherModel) {
        otherModel.attackTime = this.attackTime;
        otherModel.riding = this.riding;
        otherModel.young = this.young;
    }
}
