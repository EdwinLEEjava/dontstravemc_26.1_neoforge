package com.theconstant.dontstravemc.block.blockentity.custom;

import com.theconstant.dontstravemc.entity.neutral.PigmanEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class PigmanBedBlockEntity extends BedBlockEntity {

    private static final double CLEAR_HOME_SCAN_PADDING = 32.0;

    public PigmanBedBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state, DyeColor.BROWN);
    }

    /**
     * Called before the foot block is destroyed; clears pigman home for any mob still bound to this bed.
     */
    public void onRemoveBed(ServerLevel level) {
        BlockPos foot = getBlockPos();
        AABB box = new AABB(foot).inflate(CLEAR_HOME_SCAN_PADDING);
        for (PigmanEntity pig : level.getEntitiesOfClass(PigmanEntity.class, box, p -> foot.equals(p.getHomePos()))) {
            pig.setHomePos(null);
            if (pig.isAsleep()) {
                pig.setAsleep(false);
            }
        }
        setChanged();
    }
}
