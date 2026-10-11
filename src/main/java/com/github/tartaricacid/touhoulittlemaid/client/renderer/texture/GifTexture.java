package com.github.tartaricacid.touhoulittlemaid.client.renderer.texture;

import com.github.tartaricacid.touhoulittlemaid.api.client.decoder.GifDecoder;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TickableTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class GifTexture extends SizeTexture implements TickableTexture {
    private NativeImage[] frames = new NativeImage[0];
    private int[] frameDelays = new int[0];
    private int currentFrame = 0;
    private int currentFrameDelay = 0;

    public GifTexture(Identifier texturePath) {
        super(texturePath);
    }

    @Override
    @SuppressWarnings("all")
    protected NativeImage readImage(ResourceManager manager) throws IOException {
        try (InputStream stream = manager.open(this.resourceId())) {
            GifDecoder decoder = new GifDecoder();
            decoder.read(stream);

            int totalFrames = decoder.getFrameCount();
            Dimension frameSize = decoder.getFrameSize();
            NativeImage[] decoded = new NativeImage[totalFrames];
            int[] delays = new int[totalFrames];

            // 让图片学习原版序列帧竖向排列
            for (int i = 0; i < totalFrames; i++) {
                NativeImage nativeImage = new NativeImage(frameSize.width, frameSize.height, true);
                BufferedImage image = decoder.getFrame(i);
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        nativeImage.setPixel(x, y, image.getRGB(x, y));
                    }
                }
                decoded[i] = nativeImage;
                delays[i] = Math.max(decoder.getDelay(i) / 50, 1);
            }
            for (NativeImage old : this.frames) {
                old.close();
            }
            this.frames = decoded;
            this.frameDelays = delays;
            this.currentFrame = 0;
            this.currentFrameDelay = 0;
            if (decoded.length == 0) {
                throw new IOException("GIF has no frames: " + this.resourceId());
            }
            // The first frame is uploaded (and closed) by the texture manager, so hand it a copy
            NativeImage first = new NativeImage(frameSize.width, frameSize.height, true);
            first.copyFrom(decoded[0]);
            return first;
        }
    }

    @Override
    public boolean isExist() {
        return true;
    }

    @Override
    public void tick() {
        if (frames.length <= 1) {
            return;
        }
        currentFrameDelay++;
        if (currentFrameDelay >= frameDelays[currentFrame]) {
            currentFrameDelay = 0;
            currentFrame = (currentFrame + 1) % frames.length;
            this.uploadFrame(frames[currentFrame]);
        }
    }

    @Override
    public void close() {
        super.close();
        for (NativeImage frame : this.frames) {
            frame.close();
        }
        this.frames = new NativeImage[0];
    }
}
