package com.github.tartaricacid.touhoulittlemaid.tileentity;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.github.tartaricacid.touhoulittlemaid.util.PortUtil;
import com.github.tartaricacid.touhoulittlemaid.init.InitBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class TileEntityGarageKit extends BlockEntity {
    public static final BlockEntityType<TileEntityGarageKit> TYPE = new BlockEntityType<>(TileEntityGarageKit::new, InitBlocks.GARAGE_KIT.get());
    private static final String FACING_TAG = "GarageKitFacing";
    private static final String EXTRA_DATA = "ExtraData";
    private Direction facing = Direction.NORTH;
    private CompoundTag extraData = new CompoundTag();

    public TileEntityGarageKit(BlockPos blockPos, BlockState blockState) {
        super(TYPE, blockPos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        HolderLookup.Provider pRegistries = PortUtil.registries();
        CompoundTag pTag = new CompoundTag();
        try {
            getPersistentData().putString(FACING_TAG, facing.getSerializedName());
            getPersistentData().put(EXTRA_DATA, extraData);
            super.saveAdditional(valueOutput);
        } finally {
            PortUtil.writeRoot(valueOutput, pTag);
        }
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        HolderLookup.Provider pRegistries = valueInput.lookup();
        CompoundTag pTag = PortUtil.readRoot(valueInput);
        super.loadAdditional(valueInput);
        facing = Direction.byName(getPersistentData().getStringOr(FACING_TAG, ""));
        extraData = getPersistentData().getCompoundOrEmpty(EXTRA_DATA);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return this.saveWithoutMetadata(pRegistries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public Direction getFacing() {
        return facing;
    }

    public CompoundTag getExtraData() {
        return extraData;
    }

    public void setData(Direction facing, CompoundTag extraData) {
        this.facing = facing;
        this.extraData = extraData;
        this.setChanged();
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }
}
