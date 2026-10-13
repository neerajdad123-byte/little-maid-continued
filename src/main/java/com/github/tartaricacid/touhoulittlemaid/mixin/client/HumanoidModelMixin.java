package com.github.tartaricacid.touhoulittlemaid.mixin.client;

import com.github.tartaricacid.touhoulittlemaid.client.init.ClientSetupEvent;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Raises the arms of a player carrying a maid. MC 26.x models only see the render state, which
 * {@link ClientSetupEvent#CARRYING_MAID} marks.
 */
@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {
    @Shadow
    @Final
    public ModelPart leftArm;
    @Shadow
    @Final
    public ModelPart rightArm;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At(value = "TAIL"))
    private void setRotationAnglesHead(HumanoidRenderState state, CallbackInfo ci) {
        if (Boolean.TRUE.equals(state.getRenderData(ClientSetupEvent.CARRYING_MAID))) {
            leftArm.xRot = (float) Math.toRadians(-65);
            leftArm.yRot = (float) Math.toRadians(10);
            rightArm.xRot = (float) Math.toRadians(-65);
            rightArm.yRot = (float) Math.toRadians(-10);
        }
    }
}
