package com.github.tartaricacid.touhoulittlemaid.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilePackTexture extends SizeTexture {
    private final Path textureFile;

    public FilePackTexture(Path rootPath, Identifier texturePath) {
        super(texturePath);
        this.textureFile = rootPath.resolve("assets").resolve(texturePath.getNamespace()).resolve(texturePath.getPath());
    }

    @Override
    public boolean isExist() {
        return Files.isRegularFile(this.textureFile);
    }

    @Override
    protected NativeImage readImage(ResourceManager manager) throws IOException {
        try (InputStream stream = Files.newInputStream(this.textureFile)) {
            return NativeImage.read(stream);
        }
    }
}
