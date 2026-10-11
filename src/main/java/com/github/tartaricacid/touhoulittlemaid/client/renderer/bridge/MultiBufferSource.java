package com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * Little Maid Continued: stand-in for vanilla's {@code MultiBufferSource}, which was removed in MC 26.x.
 * <p>
 * The mod's bedrock / gecko model code still writes vertices into buffers obtained from here.
 * {@link SubmitBufferSource} records those vertices and hands them to the 26.x submit pipeline.
 */
@FunctionalInterface
public interface MultiBufferSource {
    VertexConsumer getBuffer(RenderType renderType);
}
