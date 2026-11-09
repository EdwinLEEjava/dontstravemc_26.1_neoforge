package com.theconstant.dontstravemc.block.component;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

public class BlockSanityAura {
    public static final double SANITY_EFFECT_RANGE = 20.0;
    public static final double SANITY_EFFECT_RANGE_SQ = SANITY_EFFECT_RANGE * SANITY_EFFECT_RANGE;

    private final double aura;
    private Double maxDistSq = null;

    public BlockSanityAura(double aura) {
        this.aura = aura;
    }

    public void setMaxDistSq(double maxDistSq) {
        this.maxDistSq = maxDistSq;
    }

    public double getAura(Entity observer, BlockPos sourcePos) {
        double distSq = observer.distanceToSqr(sourcePos.getX() + 0.5, sourcePos.getY() + 0.5, sourcePos.getZ() + 0.5);
        double checkDist = (maxDistSq != null) ? maxDistSq : SANITY_EFFECT_RANGE_SQ;

        if (distSq > checkDist) {
            return 0.0;
        }

        return this.aura / Math.max(1.0, distSq);
    }
}
