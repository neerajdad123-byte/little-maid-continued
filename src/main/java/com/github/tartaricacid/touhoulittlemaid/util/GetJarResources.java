package com.github.tartaricacid.touhoulittlemaid.util;

import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.ModList;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

public final class GetJarResources {
    private GetJarResources() {
    }

    /**
     * 复制本模组的文件到指定文件夹
     *
     * @param filePath jar 里面的文件地址
     * @param destPath 想要复制到的目录
     * @param fileName 复制后的文件名
     */
    public static void copyTouhouLittleMaidFile(String filePath, Path destPath, String fileName) {
        URL url = TouhouLittleMaid.class.getResource(filePath);
        try {
            if (url != null) {
                FileUtils.copyURLToFile(url, destPath.resolve(fileName).toFile());
            }
        } catch (IOException e) {
            TouhouLittleMaid.LOGGER.error("Failed to copy built-in resource {}", filePath, e);
        }
    }

    @Nullable
    public static InputStream readTouhouLittleMaidFile(String filePath) {
        URL url = TouhouLittleMaid.class.getResource(filePath);
        try {
            if (url != null) {
                return url.openStream();
            }
        } catch (IOException e) {
            TouhouLittleMaid.LOGGER.error("Failed to read built-in resource {}", filePath, e);
        }
        return null;
    }

    public static void copyFolder(String sourcePath, Path targetPath) throws IOException, URISyntaxException {
        // Little Maid Continued: FML 11 does not resolve folders through Class#getResource; list them through the mod's jar contents
        String prefix = sourcePath.startsWith("/") ? sourcePath.substring(1) : sourcePath;
        JarContents contents = ModList.get().getModFileById(TouhouLittleMaid.MOD_ID).getFile().getContents();
        contents.visitContent(prefix, (path, resource) -> {
            Path target = targetPath.resolve(path.substring(prefix.length()).replaceFirst("^/", ""));
            try {
                Files.createDirectories(target.getParent());
                try (InputStream in = resource.open()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                TouhouLittleMaid.LOGGER.error("Failed to copy built-in file {} to {}", path, target, e);
            }
        });
    }
}
