package com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.LegacyDraw;
import net.minecraft.core.component.DataComponents;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.model.EntityPlaceholderModel;
import com.github.tartaricacid.touhoulittlemaid.item.ItemEntityPlaceholder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy.LegacyItemStackRenderer;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

public class TileEntityEntityPlaceholderRenderer extends LegacyItemStackRenderer {
    private static final EntityPlaceholderModel BASE_MODEL = new EntityPlaceholderModel();
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TouhouLittleMaid.MOD_ID, "textures/item/entity_placeholder.png");

    // Little Maid Continued: MC 26.x renders these through item definitions (assets/<ns>/items/<name>.json)
    private final Function<Identifier, Identifier> recipeToModel = Util.memoize(recipeId -> {
        Path path = Paths.get(recipeId.getPath());
        return Identifier.fromNamespaceAndPath(recipeId.getNamespace(), path.getFileName().toString());
    });

    private final Function<Identifier, Identifier> recipeToTexture = Util.memoize(recipeId -> {
        Path path = Paths.get(recipeId.getPath());
        String namespace = recipeId.getNamespace();
        return Identifier.fromNamespaceAndPath(namespace, String.format("textures/item/%s.png", path.getFileName().toString()));
    });


    public TileEntityEntityPlaceholderRenderer() {
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLight, int combinedOverlay) {
        Identifier recipeId = ItemEntityPlaceholder.getRecipeId(stack);
        Identifier itemModel = getItemModel(recipeId);
        poseStack.pushPose();
        if (itemModel != null) {
            // 先尝试获取物品材质进行渲染
            ItemStack display = stack.copyWithCount(1);
            display.set(DataComponents.ITEM_MODEL, itemModel);
            // item models are centred on the origin, the old baked quads were in 0..1 block space
            poseStack.translate(0.5, 0.5, 0.5);
            LegacyDraw.item(display, ItemDisplayContext.NONE, combinedLight, combinedOverlay, poseStack, bufferIn, null, 0);
        } else {
            // 否则渲染贴图
            Identifier texture = getTexture(recipeId);
            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.mulPose(Axis.ZN.rotationDegrees(180));
            VertexConsumer buffer = bufferIn.getBuffer(RenderTypes.entityCutout(texture));
            BASE_MODEL.renderToBuffer(poseStack, buffer, combinedLight, combinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        }
        poseStack.popPose();
    }

    @Nullable
    private Identifier getItemModel(@Nullable Identifier recipeId) {
        if (recipeId == null) {
            return null;
        }
        Identifier modelId = recipeToModel.apply(recipeId);
        Identifier definition = modelId.withPath(path -> "items/" + path + ".json");
        return Minecraft.getInstance().getResourceManager().getResource(definition).isPresent() ? modelId : null;
    }

    private Identifier getTexture(@Nullable Identifier recipeId) {
        if (recipeId == null) {
            return TEXTURE;
        }
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        Identifier texture = recipeToTexture.apply(recipeId);
        if (manager.getResource(texture).isPresent()) {
            return texture;
        }
        return TEXTURE;
    }
}
