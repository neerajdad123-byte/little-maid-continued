package com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity;

import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityJoy;
import com.github.tartaricacid.touhoulittlemaid.util.RenderHelper;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy.LegacyBlockEntityRenderer;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public abstract class TileEntityJoyRenderer<T extends TileEntityJoy> implements LegacyBlockEntityRenderer<T> {
    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox(T te) {
        return RenderHelper.getAABB(te.getWorldPosition().offset(-2, 0, -2), te.getWorldPosition().offset(2, 1, 2));
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
