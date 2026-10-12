package com.github.tartaricacid.touhoulittlemaid.client.gui.entity.detail;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.cache.CacheIconManager;
import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.ModelDetailsButton;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.ChairModelInfo;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityChair;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.bridge.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;

public class ChairModelDetailsGui extends AbstractModelDetailsGui<EntityChair, ChairModelInfo> {
    private EntityMaid maid;
    private boolean showPassenger = false;

    public ChairModelDetailsGui(EntityChair sourceEntity, ChairModelInfo modelInfo) {
        super(sourceEntity, InitEntities.CHAIR.get().create(sourceEntity.level(), EntitySpawnReason.LOAD), modelInfo);
        guiEntity.setModelId(modelInfo.getModelId().toString());
        if (Minecraft.getInstance().level != null) {
            this.maid = InitEntities.MAID.get().create(Minecraft.getInstance().level, EntitySpawnReason.LOAD);
            if (this.maid != null) {
                this.maid.setModelId("authors_and_credits:wine_fox_maid");
            }
        }
    }

    @Override
    protected void applyReturnButtonLogic() {
        CacheIconManager.openChairModelGui(sourceEntity);
    }

    @Override
    protected void initSideButton() {
        ModelDetailsButton showPassengerButton = new ModelDetailsButton(2, 17, "gui.touhou_little_maid.skin_details.show_passenger",
                this::applyShowPassengerLogic);
        this.addRenderableWidget(showPassengerButton);
    }

    @Override
    public void tick() {
        guiEntity.tickCount++;
        if (maid != null) {
            maid.tickCount++;
        }
    }

    @Override
    protected Entity extraEntity() {
        return showPassenger ? maid : null;
    }

    @Override
    protected float extraEntityYOffset() {
        return -0.375F + modelInfo.getMountedYOffset();
    }

    private void applyShowPassengerLogic(boolean isStateTriggered) {
        this.showPassenger = isStateTriggered;
        if (isStateTriggered && maid != null) {
            maid.startRiding(guiEntity, true, false);
        } else {
            guiEntity.ejectPassengers();
        }
    }
}
