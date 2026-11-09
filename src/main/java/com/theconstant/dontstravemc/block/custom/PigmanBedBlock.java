package com.theconstant.dontstravemc.block.custom;

import com.theconstant.dontstravemc.block.blockentity.custom.PigmanBedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PigmanBedBlock extends BedBlock {
    public PigmanBedBlock(BlockBehaviour.Properties properties) {
        super(DyeColor.BROWN, properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS_SERVER;
        }

        player.sendOverlayMessage(Component.translatable("block.dontstravemc.pigman_bed.not_yours"));
        return InteractionResult.SUCCESS_SERVER;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(PART) == BedPart.FOOT) {
            return new PigmanBedBlockEntity(pos, state);
        }
        return new BedBlockEntity(pos, state, DyeColor.BROWN);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()
                && state.getValue(PART) == BedPart.FOOT
                && level.getBlockEntity(pos) instanceof PigmanBedBlockEntity be) {
            be.onRemoveBed((ServerLevel) level);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
