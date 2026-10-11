package com.github.tartaricacid.touhoulittlemaid.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

/**
 * A texture that knows its pixel size.
 * <p>
 * Little Maid Continued: MC 26.x uploads textures through the GPU device; subclasses only read the image
 * and the vanilla {@link ReloadableTexture} pipeline uploads it. Register with
 * {@code TextureManager#registerAndLoad}.
 */
public abstract class SizeTexture extends ReloadableTexture {
    protected int width = 16;
    protected int height = 16;

    protected SizeTexture(Identifier resourceId) {
        super(resourceId);
    }

    /**
     * Reads the full image; called by the texture manager when the texture is (re)loaded.
     */
    protected abstract NativeImage readImage(ResourceManager manager) throws IOException;

    @Override
    public TextureContents loadContents(ResourceManager resourceManager) throws IOException {
        NativeImage image = this.readImage(resourceManager);
        this.width = image.getWidth();
        this.height = image.getHeight();
        return new TextureContents(image, null);
    }

    /**
     * Replaces the GPU contents with another image of the same size (used for animated textures).
     */
    protected void uploadFrame(NativeImage frame) {
        if (this.texture != null) {
            RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, frame);
        }
    }

    /**
     * 获取材质宽度
     *
     * @return 宽度（像素）
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * 获取材质高度
     *
     * @return 高度（像素）
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * 给定路径的文件是否存在
     *
     * @return 存在与否
     */
    public abstract boolean isExist();
}
