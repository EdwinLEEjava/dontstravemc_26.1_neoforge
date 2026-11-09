package com.theconstant.dontstravemc.world;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public final class ConstantTeleporter {

    private static final int ARRIVAL_X = 0;
    private static final int ARRIVAL_Z = 0;

    private ConstantTeleporter() {
    }

    /**
     * 先生成到达点所在区块，再按地表高度做一次传送。
     * 不能先传到固定的 Y=128 再在传送回调里改高度：那一次回调读到的还是旧坐标，
     * 客户端也已经按 (0.5, 128, 0.5) 落位，人会停在这个固定点。
     */
    public static @Nullable TeleportTransition toTheConstant(ServerLevel from, Entity entity) {
        if (from.dimension() != Level.OVERWORLD) {
            return null;
        }
        ServerLevel target = from.getServer().getLevel(ConstantDimensions.THE_CONSTANT);
        if (target == null) {
            return null;
        }

        target.getChunk(SectionPos.blockToSectionCoord(ARRIVAL_X), SectionPos.blockToSectionCoord(ARRIVAL_Z));
        int surfaceY = target.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ARRIVAL_X, ARRIVAL_Z);
        return new TeleportTransition(
                target,
                new Vec3(ARRIVAL_X + 0.5, surfaceY, ARRIVAL_Z + 0.5),
                Vec3.ZERO,
                entity.getYRot(),
                entity.getXRot(),
                Set.of(),
                TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET)
        );
    }
}
