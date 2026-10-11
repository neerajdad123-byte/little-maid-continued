package com.github.tartaricacid.touhoulittlemaid.tileentity;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.github.tartaricacid.touhoulittlemaid.util.PortUtil;
import com.github.tartaricacid.touhoulittlemaid.init.InitBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class TileEntityMaidBed extends BlockEntity {
    public static final BlockEntityType<TileEntityMaidBed> TYPE = BlockEntityType.Builder.of(TileEntityMaidBed::new, InitBlocks.MAID_BED.get()).build(null);
    private static final String COLOR_TAG = "BedColor";
    private DyeColor color = DyeColor.PINK;

    public TileEntityMaidBed(BlockPos blockPos, BlockState blockState) {
        super(TYPE, blockPos, blockState);
    }

    public void setColor(DyeColor color) {
        this.color = color;
        this.refresh();
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        HolderLookup.Provider registries = PortUtil.registries();
        CompoundTag tag = new CompoundTag();
        try {
            getPersistentData().putInt(COLOR_TAG, color.getId());
            super.saveAdditional(valueOutput);
        } finally {
            PortUtil.writeRoot(valueOutput, tag);
        }
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        HolderLookup.Provider registries = valueInput.lookup();
        CompoundTag tag = PortUtil.readRoot(valueInput);
        super.loadAdditional(valueInput);
        color = DyeColor.byId(getPersistentData().getIntOr(COLOR_TAG, 0));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void refresh() {
        this.setChanged();
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    public DyeColor getColor() {
        return color;
    }
}
