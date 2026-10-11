package com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Collects vertices written by the mod's legacy model code and submits them to the MC 26.x
 * {@link SubmitNodeCollector} as custom geometry when closed.
 * <p>
 * Model code runs immediately (so animation state and bone positions are correct at submit time);
 * only the vertex upload is deferred. Vertices are recorded already transformed, so they are submitted
 * with an identity pose.
 * <pre>{@code
 * try (SubmitBufferSource buffers = new SubmitBufferSource(collector, camera)) {
 *     model.renderToBuffer(poseStack, buffers.getBuffer(renderType), light, overlay, color);
 * }
 * }</pre>
 */
public final class SubmitBufferSource implements MultiBufferSource, AutoCloseable {
    private static final PoseStack IDENTITY = new PoseStack();

    private final SubmitNodeCollector collector;
    private final @Nullable CameraRenderState camera;
    private final Map<RenderType, RecordingVertexConsumer> buffers = new LinkedHashMap<>();

    public SubmitBufferSource(SubmitNodeCollector collector, @Nullable CameraRenderState camera) {
        this.collector = collector;
        this.camera = camera;
    }

    public SubmitBufferSource(SubmitNodeCollector collector) {
        this(collector, null);
    }

    public SubmitNodeCollector collector() {
        return collector;
    }

    public @Nullable CameraRenderState camera() {
        return camera;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return buffers.computeIfAbsent(renderType, type -> new RecordingVertexConsumer());
    }

    /**
     * Submits everything recorded so far and starts over.
     */
    public void flush() {
        for (Map.Entry<RenderType, RecordingVertexConsumer> entry : buffers.entrySet()) {
            RecordingVertexConsumer recording = entry.getValue();
            if (!recording.isEmpty()) {
                collector.submitCustomGeometry(IDENTITY, entry.getKey(), (pose, buffer) -> recording.replay(buffer));
            }
        }
        buffers.clear();
    }

    @Override
    public void close() {
        flush();
    }
}
