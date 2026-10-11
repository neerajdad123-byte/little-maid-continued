package com.github.tartaricacid.touhoulittlemaid.client.renderer.texture;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipPackTexture extends SizeTexture {
    private final Path zipFilePath;
    private final String entryName;

    public ZipPackTexture(String zipFilePath, Identifier texturePath) {
        super(texturePath);
        this.zipFilePath = Paths.get(zipFilePath);
        this.entryName = String.format("assets/%s/%s", texturePath.getNamespace(), texturePath.getPath());
    }

    @Override
    public boolean isExist() {
        try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
            return zipFile.getEntry(this.entryName) != null;
        } catch (IOException e) {
            TouhouLittleMaid.LOGGER.error("Failed to inspect zip texture {}", this.resourceId(), e);
        }
        return false;
    }

    @Override
    protected NativeImage readImage(ResourceManager manager) throws IOException {
        try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
            ZipEntry entry = zipFile.getEntry(this.entryName);
            if (entry == null) {
                throw new FileNotFoundException(this.entryName);
            }
            try (InputStream stream = zipFile.getInputStream(entry)) {
                return NativeImage.read(stream);
            }
        }
    }
}
