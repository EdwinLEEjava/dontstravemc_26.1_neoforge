package com.theconstant.dontstravemc.block.blockentity.custom;

import com.theconstant.dontstravemc.block.blockentity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ConstantPortalBlockEntity extends BlockEntity {
    public ConstantPortalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONSTANT_PORTAL.get(), pos, state);
    }
}
