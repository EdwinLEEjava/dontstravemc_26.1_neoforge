package com.theconstant.dontstravemc.item.custom;

import com.theconstant.dontstravemc.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class PineconeItem extends Item {
    public PineconeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(pos);
        BlockPos placePos = pos;

        if (clickedState.isSolid() && context.getClickedFace() == Direction.UP) {
            placePos = pos.above();
        }

        if (level.getBlockState(placePos).isAir()) {
            BlockState saplingState = ModBlocks.EVERGREEN_SAPLING.get().defaultBlockState();
            if (saplingState.canSurvive(level, placePos)) {
                if (!level.isClientSide()) {
                    level.setBlock(placePos, saplingState, 3);
                    level.playSound(null, placePos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
                    context.getItemInHand().shrink(1);
                }

                return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }
}
