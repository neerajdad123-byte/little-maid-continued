/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.github.tartaricacid.touhoulittlemaid.geckolib3.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy.LegacyEntityRenderer;
import net.minecraft.world.entity.Entity;

@SuppressWarnings({"unchecked"})
public class AnimationUtils {
    public static double convertTicksToSeconds(double ticks) {
        return ticks / 20;
    }

    public static double convertSecondsToTicks(double seconds) {
        return seconds * 20;
    }

    /**
     * 获取实体的 Renderer
     */
    public static <T extends Entity> LegacyEntityRenderer<T> getRenderer(T entity) {
        EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        return (LegacyEntityRenderer<T>) renderManager.getRenderer(entity);
    }
}