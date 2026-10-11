package com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Little Maid Continued: replacements for MC 1.21.1 immediate draw calls (items, text) that write into a
 * {@link MultiBufferSource}. They submit to the collector behind a {@link SubmitBufferSource} instead.
 * Argument order matches the removed vanilla methods so call sites barely change.
 */
public final class LegacyDraw {
    private LegacyDraw() {
    }

    public static SubmitNodeCollector collector(MultiBufferSource buffer) {
        if (buffer instanceof SubmitBufferSource source) {
            return source.collector();
        }
        throw new IllegalStateException("LegacyDraw needs a SubmitBufferSource, got " + buffer);
    }

    private static int opaque(int color) {
        // 1.21.6+ text colors are ARGB; old code passed RGB
        return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
    }

    private static PoseStack poseOf(Matrix4f matrix) {
        PoseStack poseStack = new PoseStack();
        poseStack.last().pose().set(matrix);
        return poseStack;
    }

    /**
     * Was {@code Font#drawInBatch(FormattedCharSequence, ...)}. Returns the x after the text, like the old method.
     */
    public static int text(Font font, FormattedCharSequence text, float x, float y, int color, boolean dropShadow, Matrix4f matrix,
                           MultiBufferSource buffer, Font.DisplayMode displayMode, int backgroundColor, int packedLight) {
        collector(buffer).submitText(poseOf(matrix), x, y, text, dropShadow, displayMode, packedLight, opaque(color), backgroundColor, 0);
        return (int) x + font.width(text) + (dropShadow ? 1 : 0);
    }

    public static int text(Font font, Component text, float x, float y, int color, boolean dropShadow, Matrix4f matrix,
                           MultiBufferSource buffer, Font.DisplayMode displayMode, int backgroundColor, int packedLight) {
        return text(font, text.getVisualOrderText(), x, y, color, dropShadow, matrix, buffer, displayMode, backgroundColor, packedLight);
    }

    public static int text(Font font, String text, float x, float y, int color, boolean dropShadow, Matrix4f matrix,
                           MultiBufferSource buffer, Font.DisplayMode displayMode, int backgroundColor, int packedLight) {
        return text(font, Component.literal(text), x, y, color, dropShadow, matrix, buffer, displayMode, backgroundColor, packedLight);
    }

    /**
     * Was {@code ItemRenderer#renderStatic(LivingEntity, ItemStack, ItemDisplayContext, boolean, PoseStack, MultiBufferSource, Level, int, int, int)}.
     */
    public static void item(@Nullable LivingEntity entity, ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack poseStack,
                            MultiBufferSource buffer, @Nullable Level level, int packedLight, int packedOverlay, int seed) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStackRenderState state = new ItemStackRenderState();
        if (entity != null) {
            Minecraft.getInstance().getItemModelResolver().updateForLiving(state, stack, context, entity);
        } else {
            Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, stack, context, level, null, seed);
        }
        state.submit(poseStack, collector(buffer), packedLight, packedOverlay, 0);
    }

    /**
     * Was {@code ItemRenderer#renderStatic(ItemStack, ItemDisplayContext, int, int, PoseStack, MultiBufferSource, Level, int)}.
     */
    public static void item(ItemStack stack, ItemDisplayContext context, int packedLight, int packedOverlay, PoseStack poseStack,
                            MultiBufferSource buffer, @Nullable Level level, int seed) {
        item(null, stack, context, false, poseStack, buffer, level, packedLight, packedOverlay, seed);
    }

    /**
     * Draws an entity with a MC 26.x (vanilla) renderer from legacy code. Was {@code renderer.render(entity, yaw, partialTicks, ...)}.
     */
    public static <E extends Entity, S extends EntityRenderState> void entity(EntityRenderer<? super E, S> renderer, E entity, float partialTicks,
                                                                              PoseStack poseStack, MultiBufferSource buffer) {
        if (!(buffer instanceof SubmitBufferSource source) || source.camera() == null) {
            return;
        }
        S state = renderer.createRenderState(entity, partialTicks);
        renderer.submit(state, poseStack, source.collector(), source.camera());
    }

    /**
     * Was {@code BlockRenderDispatcher#renderSingleBlock(BlockState, PoseStack, MultiBufferSource, int, int, ...)}.
     */
    public static void block(BlockState blockState, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockModelRenderState state = new BlockModelRenderState();
        Minecraft.getInstance().getBlockModelResolver().update(state, blockState, BLOCK_CONTEXT);
        if (!state.isEmpty()) {
            state.submit(poseStack, collector(buffer), packedLight, packedOverlay, 0);
        }
    }

    private static final BlockDisplayContext BLOCK_CONTEXT = BlockDisplayContext.create();
    private static final Map<SkullBlock.Type, Optional<SkullModelBase>> SKULL_MODELS = new HashMap<>();

    /**
     * Was {@code SkullBlockRenderer#renderSkull(null, yRot, 0, poseStack, buffer, light, model, renderType)} after the
     * caller's own transforms; places the skull like the old "no direction" branch did.
     */
    public static void skull(SkullBlock.Type type, @Nullable ResolvableProfile profile, float yRot, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight) {
        SkullModelBase model = SKULL_MODELS.computeIfAbsent(type,
                t -> Optional.ofNullable(SkullBlockRenderer.createModel(Minecraft.getInstance().getEntityModels(), t))).orElse(null);
        if (model == null) {
            return;
        }
        RenderType renderType = type == SkullBlock.Types.PLAYER && profile != null
                ? Minecraft.getInstance().playerSkinRenderCache().getOrDefault(profile).renderType()
                : SkullBlockRenderer.getSkullRenderType(type, null);
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yRot));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        SkullBlockRenderer.submitSkull(0.0F, poseStack, collector(buffer), packedLight, model, renderType, 0, null);
        poseStack.popPose();
    }

    /**
     * Was {@code LevelRenderer#renderLineBox(PoseStack, VertexConsumer, AABB, r, g, b, a)}.
     */
    public static void lineBox(PoseStack poseStack, MultiBufferSource buffer, AABB box, float red, float green, float blue, float alpha) {
        VertexConsumer consumer = buffer.getBuffer(RenderTypes.lines());
        PoseStack.Pose pose = poseStack.last();
        float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
        float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;
        float[][] edges = {
                {x0, y0, z0, x1, y0, z0}, {x0, y0, z0, x0, y1, z0}, {x0, y0, z0, x0, y0, z1},
                {x1, y0, z0, x1, y1, z0}, {x1, y0, z0, x1, y0, z1}, {x0, y1, z0, x1, y1, z0},
                {x0, y1, z0, x0, y1, z1}, {x0, y0, z1, x1, y0, z1}, {x0, y0, z1, x0, y1, z1},
                {x1, y1, z0, x1, y1, z1}, {x0, y1, z1, x1, y1, z1}, {x1, y0, z1, x1, y1, z1}
        };
        for (float[] e : edges) {
            float nx = e[3] - e[0], ny = e[4] - e[1], nz = e[5] - e[2];
            consumer.addVertex(pose, e[0], e[1], e[2]).setColor(red, green, blue, alpha).setNormal(pose, nx, ny, nz).setLineWidth(1.0F);
            consumer.addVertex(pose, e[3], e[4], e[5]).setColor(red, green, blue, alpha).setNormal(pose, nx, ny, nz).setLineWidth(1.0F);
        }
    }

    /**
     * Was {@code ItemInHandRenderer#renderItem(LivingEntity, ItemStack, ItemDisplayContext, boolean, PoseStack, MultiBufferSource, int)}.
     */
    public static void itemInHand(LivingEntity entity, ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack poseStack,
                                  MultiBufferSource buffer, int packedLight) {
        item(entity, stack, context, leftHand, poseStack, buffer, entity.level(), packedLight,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, entity.getId());
    }
}
