package com.theconstant.dontstravemc.client.fog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.resources.Identifier;

/**
 * 与 {@link net.minecraft.client.multiplayer.ClientLevel#calculateBlockTint} 相同的几何范围，
 * 统计邻域内墓地群系占比，用于雾浓度与草色边界混合一致。
 */
public final class CemeteryBiomeInfluence {

    private static final Identifier CEMETERY_FOREST =
            Identifier.fromNamespaceAndPath("dontstravemc", "cemetery_forest");

    private CemeteryBiomeInfluence() {
    }

    /**
     * @return [0, 1]，邻域内墓地格数 / 总格数；{@code biomeBlendRadius == 0} 时为单格 0/1。
     */
    public static float cemeterySpatialWeight(ClientLevel level, BlockPos blockPos) {
        int i = Minecraft.getInstance().options.biomeBlendRadius().get();
        if (i == 0) {
            return level.getBiome(blockPos).unwrapKey().map(k -> k.identifier().equals(CEMETERY_FOREST)).orElse(false) ? 1f : 0f;
        }

        int j = (i * 2 + 1) * (i * 2 + 1);
        int cemeteryCount = 0;
        Cursor3D cursor3D = new Cursor3D(
                blockPos.getX() - i,
                blockPos.getY(),
                blockPos.getZ() - i,
                blockPos.getX() + i,
                blockPos.getY(),
                blockPos.getZ() + i
        );
        MutableBlockPos mutableBlockPos = new MutableBlockPos();

        while (cursor3D.advance()) {
            mutableBlockPos.set(cursor3D.nextX(), cursor3D.nextY(), cursor3D.nextZ());
            if (level.getBiome(mutableBlockPos).unwrapKey().map(k -> k.identifier().equals(CEMETERY_FOREST)).orElse(false)) {
                cemeteryCount++;
            }
        }

        return (float) cemeteryCount / (float) j;
    }
}
