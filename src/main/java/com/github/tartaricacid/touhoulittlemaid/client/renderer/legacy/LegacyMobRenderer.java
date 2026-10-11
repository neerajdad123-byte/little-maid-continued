package com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;

/**
 * Little Maid Continued: the MC 1.21.1 {@code MobRenderer} on top of {@link LegacyLivingEntityRenderer}.
 * Leashes are handled by the MC 26.x base renderer.
 */
public abstract class LegacyMobRenderer<T extends Mob, M extends LegacyEntityModel<T>> extends LegacyLivingEntityRenderer<T, M> {
    protected LegacyMobRenderer(EntityRendererProvider.Context context, M model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Override
    protected boolean shouldShowName(T entity, double distanceToCameraSq) {
        return super.shouldShowName(entity, distanceToCameraSq)
               && (entity.shouldShowName() || entity.hasCustomName() && entity == this.entityRenderDispatcher.crosshairPickEntity);
    }
}
