package com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge;

import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.Arrays;

/**
 * Records fully transformed vertices so they can be replayed later into the real buffer.
 * <p>
 * Each vertex stores position, uv, normal, line width, color, overlay and light. Elements that the
 * target vertex format does not use are ignored by the real buffer on replay.
 */
public final class RecordingVertexConsumer implements VertexConsumer {
    // x, y, z, u, v, nx, ny, nz, lineWidth
    private static final int FLOATS = 9;
    // color, overlay, light
    private static final int INTS = 3;

    private float[] floats = new float[FLOATS * 256];
    private int[] ints = new int[INTS * 256];
    private int count = 0;

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        if ((count + 1) * FLOATS > floats.length) {
            floats = Arrays.copyOf(floats, floats.length * 2);
            ints = Arrays.copyOf(ints, ints.length * 2);
        }
        int f = count * FLOATS;
        int i = count * INTS;
        floats[f] = x;
        floats[f + 1] = y;
        floats[f + 2] = z;
        floats[f + 3] = 0;
        floats[f + 4] = 0;
        floats[f + 5] = 0;
        floats[f + 6] = 1;
        floats[f + 7] = 0;
        floats[f + 8] = 1;
        ints[i] = 0xFFFFFFFF;
        ints[i + 1] = 0;
        ints[i + 2] = 0;
        count++;
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        return setColor((a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF));
    }

    @Override
    public VertexConsumer setColor(int color) {
        ints[(count - 1) * INTS] = color;
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        int f = (count - 1) * FLOATS;
        floats[f + 3] = u;
        floats[f + 4] = v;
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        ints[(count - 1) * INTS + 1] = (v & 0xFFFF) << 16 | (u & 0xFFFF);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        ints[(count - 1) * INTS + 2] = (v & 0xFFFF) << 16 | (u & 0xFFFF);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        int f = (count - 1) * FLOATS;
        floats[f + 5] = x;
        floats[f + 6] = y;
        floats[f + 7] = z;
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        floats[(count - 1) * FLOATS + 8] = width;
        return this;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * Replays every recorded vertex into the real buffer, unchanged.
     */
    public void replay(VertexConsumer out) {
        for (int n = 0; n < count; n++) {
            int f = n * FLOATS;
            int i = n * INTS;
            out.addVertex(floats[f], floats[f + 1], floats[f + 2]);
            out.setColor(ints[i]);
            out.setUv(floats[f + 3], floats[f + 4]);
            out.setUv1(ints[i + 1] & 0xFFFF, ints[i + 1] >>> 16);
            out.setUv2(ints[i + 2] & 0xFFFF, ints[i + 2] >>> 16);
            out.setNormal(floats[f + 5], floats[f + 6], floats[f + 7]);
            out.setLineWidth(floats[f + 8]);
        }
    }
}
