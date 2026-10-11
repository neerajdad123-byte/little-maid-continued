package com.github.tartaricacid.touhoulittlemaid.client.renderer.entity;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.LegacyDraw;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.SimpleBedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.resource.BedrockModelLoader;
import com.github.tartaricacid.touhoulittlemaid.config.subconfig.VanillaConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy.LegacyMobRenderer;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.cubemob.Slime;

import java.util.Objects;

import static com.github.tartaricacid.touhoulittlemaid.client.resource.BedrockModelLoader.REIMU_YUKKURI;

public class EntityYukkuriSlimeRender extends LegacyMobRenderer<Slime, SimpleBedrockModel<Slime>> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "textures/bedrock/entity/reimu_yukkuri.png");
    private final SlimeRenderer vanillaRender;

    public EntityYukkuriSlimeRender(EntityRendererProvider.Context context) {
        super(context, Objects.requireNonNull(BedrockModelLoader.getModel(REIMU_YUKKURI)), 0.25F);
        this.vanillaRender = new SlimeRenderer(context);
    }

    @Override
    public void render(Slime slime, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (VanillaConfig.REPLACE_SLIME_MODEL.get()) {
            renderYukkuri(slime, yaw, partialTicks, poseStack, buffer, packedLight);
        } else {
            LegacyDraw.entity(vanillaRender, slime, partialTicks, poseStack, buffer);
        }
    }

    private void renderYukkuri(Slime slime, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.shadowRadius = 0.25F * (float) slime.getSize();
        super.render(slime, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected void scale(Slime slime, PoseStack poseStack, float partialTicks) {
        poseStack.scale(0.999F, 0.999F, 0.999F);
        poseStack.translate(0.0F, 0.001F, 0.0F);
        float slimeSize = (float) slime.getSize();
        float tmp = Mth.lerp(partialTicks, slime.oSquish, slime.squish) / (slimeSize * 0.5F + 1.0F);
        float scale = 1.0F / (tmp + 1.0F);
        poseStack.scale(scale * slimeSize, 1.0F / scale * slimeSize, scale * slimeSize);
    }

    @Override
    public Identifier getTextureLocation(Slime slime) {
        return TEXTURE;
    }
}
