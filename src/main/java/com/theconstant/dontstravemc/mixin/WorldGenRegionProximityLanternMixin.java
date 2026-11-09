package com.theconstant.dontstravemc.mixin;

import com.theconstant.dontstravemc.block.custom.ProximityLanternBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@link WorldGenRegion#setBlock} writes chunk state without running the same placement-time paths as normal world interaction
 * (world generation does not execute the usual block-after-place logic), so proximity lanterns never get their scheduled tick chain during structure generation.
 * <p>
 * Schedules via {@link WorldGenLevel#scheduleTick} so ticks merge into the chunk. Never {@link net.minecraft.server.level.ServerLevel#setBlock} during worldgen
 * (see {@link ProximityLanternBlock#scheduleTickAfterWorldGen}).
 */
@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionProximityLanternMixin {

    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("RETURN")
    )
    private void dontstravemc$startProximityLanternTicks(
            BlockPos pos,
            BlockState state,
            int flags,
            int recursionLeft,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!Boolean.TRUE.equals(cir.getReturnValue())) {
            return;
        }
        if (!(state.getBlock() instanceof ProximityLanternBlock)) {
            return;
        }
        WorldGenLevel worldGenLevel = (WorldGenLevel) (Object) this;
        ProximityLanternBlock.scheduleTickAfterWorldGen(worldGenLevel, pos, state);
    }
}
