package com.theconstant.dontstravemc.block.util;

import com.theconstant.dontstravemc.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Helpers for pigman AI around the pigman bed (e.g. standing beside the bed for pathing).
 */
public final class PigmanHouseHelper {

    private static final double PIGMAN_WIDTH = 0.6;
    private static final double PIGMAN_HEIGHT = 1.95;

    private PigmanHouseHelper() {
    }

    @Nullable
    public static Vec3 findSpawnBesideBed(ServerLevel level, BlockPos footPos) {
        BlockState st = level.getBlockState(footPos);
        if (!st.is(ModBlocks.PIGMAN_BED.get()) || !st.hasProperty(BedBlock.PART) || st.getValue(BedBlock.PART) != BedPart.FOOT) {
            return null;
        }
        Direction facing = st.getValue(BedBlock.FACING);
        Direction[] tryDirs = {facing.getClockWise(), facing.getCounterClockWise(), facing.getOpposite(), facing};
        for (Direction d : tryDirs) {
            BlockPos side = footPos.relative(d);
            BlockState ground = level.getBlockState(side.below());
            BlockState feet = level.getBlockState(side);
            BlockState head = level.getBlockState(side.above());
            if (!ground.blocksMotion() || feet.blocksMotion() || head.blocksMotion()) {
                continue;
            }
            Vec3 v = Vec3.atBottomCenterOf(side);
            AABB box = new AABB(v.x - PIGMAN_WIDTH / 2, v.y, v.z - PIGMAN_WIDTH / 2,
                    v.x + PIGMAN_WIDTH / 2, v.y + PIGMAN_HEIGHT, v.z + PIGMAN_WIDTH / 2);
            if (level.noCollision(null, box)) {
                return v;
            }
        }
        return null;
    }
}
