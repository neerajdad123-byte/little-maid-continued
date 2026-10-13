package com.github.tartaricacid.touhoulittlemaid.client.animation;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.animation.inner.InnerAnimation;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.IModelInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.annotation.Nullable;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@OnlyIn(Dist.CLIENT)
public class CustomJsAnimationManger {
    // pack scripts may only use the mod's animation wrappers (script/*); servers can push packs, so no other Java access
    private static final String SCRIPT_API_PACKAGE = "com.github.tartaricacid.touhoulittlemaid.client.animation.script.";
    private static @Nullable ScriptEngine engine;
    private static boolean engineFailed;

    /**
     * The script engine, created on first use; null if it cannot start (pack animations are then skipped).
     */
    public static @Nullable ScriptEngine engine() {
        if (engine == null && !engineFailed) {
            try {
                engine = new NashornScriptEngineFactory().getScriptEngine(
                        className -> className.startsWith(SCRIPT_API_PACKAGE) && className.indexOf('.', SCRIPT_API_PACKAGE.length()) < 0);
            } catch (Throwable e) {
                engineFailed = true;
                TouhouLittleMaid.LOGGER.error("JavaScript engine unavailable; custom pack animations are disabled", e);
            }
        }
        return engine;
    }
    private static final Map<Identifier, Object> CUSTOM_ANIMATION_MAP = Maps.newHashMap();

    @Nullable
    public static List<Object> getCustomAnimation(Path rootPath, IModelInfo item) {
        List<Object> animations = Lists.newArrayList();
        if (item.getAnimation() != null && !item.getAnimation().isEmpty()) {
            for (Identifier res : item.getAnimation()) {
                Object animation = CustomJsAnimationManger.getCustomAnimation(rootPath, res);
                if (animation != null) {
                    animations.add(animation);
                }
            }
            return animations;
        }
        return null;
    }

    @Nullable
    public static List<Object> getCustomAnimation(ZipFile zipFile, IModelInfo item) {
        List<Object> animations = Lists.newArrayList();
        if (item.getAnimation() != null && !item.getAnimation().isEmpty()) {
            for (Identifier res : item.getAnimation()) {
                Object animation = CustomJsAnimationManger.getCustomAnimation(zipFile, res);
                if (animation != null) {
                    animations.add(animation);
                }
            }
            return animations;
        }
        return null;
    }

    @Nullable
    private static Object getCustomAnimation(Path rootPath, @Nullable Identifier resourceLocation) {
        if (resourceLocation == null) {
            return null;
        }
        if (CUSTOM_ANIMATION_MAP.containsKey(resourceLocation)) {
            return CUSTOM_ANIMATION_MAP.get(resourceLocation);
        }
        if (InnerAnimation.containsKey(resourceLocation)) {
            return InnerAnimation.get(resourceLocation);
        }
        File file = rootPath.resolve("assets").resolve(resourceLocation.getNamespace()).resolve(resourceLocation.getPath()).toFile();
        if (!file.isFile()) {
            return null;
        }
        try (InputStream stream = new FileInputStream(file)) {
            ScriptEngine nashorn = engine();
            if (nashorn == null) {
                return null;
            }
            Bindings bindings = nashorn.createBindings();
            Object scriptObject = nashorn.eval(IOUtils.toString(stream, StandardCharsets.UTF_8), bindings);
            CUSTOM_ANIMATION_MAP.put(resourceLocation, scriptObject);
            return scriptObject;
        } catch (IOException | ScriptException | RuntimeException | LinkageError e) {
            // one broken pack script must not abort the whole resource reload (that leaves a black screen)
            TouhouLittleMaid.LOGGER.error("Failed to load custom animation {}", resourceLocation, e);
        }
        return null;
    }

    @Nullable
    private static Object getCustomAnimation(ZipFile zipFile, @Nullable Identifier resourceLocation) {
        if (resourceLocation == null) {
            return null;
        }
        if (CUSTOM_ANIMATION_MAP.containsKey(resourceLocation)) {
            return CUSTOM_ANIMATION_MAP.get(resourceLocation);
        }
        if (InnerAnimation.containsKey(resourceLocation)) {
            return InnerAnimation.get(resourceLocation);
        }
        ZipEntry entry = zipFile.getEntry(String.format("assets/%s/%s", resourceLocation.getNamespace(), resourceLocation.getPath()));
        if (entry == null) {
            return null;
        }
        try (InputStream stream = zipFile.getInputStream(entry)) {
            ScriptEngine nashorn = engine();
            if (nashorn == null) {
                return null;
            }
            Bindings bindings = nashorn.createBindings();
            Object scriptObject = nashorn.eval(IOUtils.toString(stream, StandardCharsets.UTF_8), bindings);
            CUSTOM_ANIMATION_MAP.put(resourceLocation, scriptObject);
            return scriptObject;
        } catch (IOException | ScriptException | RuntimeException | LinkageError e) {
            // one broken pack script must not abort the whole resource reload (that leaves a black screen)
            TouhouLittleMaid.LOGGER.error("Failed to load custom animation {}", resourceLocation, e);
        }
        return null;
    }

    public static void clearAll() {
        CUSTOM_ANIMATION_MAP.clear();
    }
}
