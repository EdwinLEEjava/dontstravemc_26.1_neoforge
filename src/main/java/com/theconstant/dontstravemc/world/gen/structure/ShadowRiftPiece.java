package com.theconstant.dontstravemc.world.gen.structure;

import com.theconstant.dontstravemc.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.ArrayList;
import java.util.List;

/**
 * 阴影裂隙的唯一部件。每个区块只处理自己范围内的方块；每个方块的结果只由坐标和 {@link #seed} 决定，
 * 所以区块生成顺序不同也不会出现接缝。
 * <p>
 * 兼容约束：
 * <ul>
 *   <li>只替换 {@link ModStructures#SHADOW_RIFT_REPLACEABLE}、只清除 {@link ModStructures#SHADOW_RIFT_CLEARABLE} 里的方块；</li>
 *   <li>跳过带方块实体的方块、含液体的方块、{@code #minecraft:features_cannot_replace}；</li>
 *   <li>跳过其他任何结构部件的范围（外扩 {@link #STRUCTURE_MARGIN} 格）；</li>
 *   <li>写方块不触发邻居更新。</li>
 * </ul>
 */
public class ShadowRiftPiece extends StructurePiece {

    private static final int STRUCTURE_MARGIN = 2;
    /** 边缘噪声振幅（相对半径）。 */
    private static final double EDGE_NOISE = 0.25;
    private static final double EDGE_NOISE_SCALE = 0.12;
    /** 只更新客户端，不通知邻居，避免连锁弄坏旁边的方块。 */
    private static final int SET_FLAGS = Block.UPDATE_CLIENTS;
    private static final int PORTAL_HEADROOM = 3;

    private final BlockPos center;
    private final int radius;
    private final int verticalRadius;
    private final BlockPos portalPos;
    private final int portalSize;
    private final long seed;

    public ShadowRiftPiece(BlockPos center, int radius, int verticalRadius, BlockPos portalPos, int portalSize, long seed) {
        super(ModStructures.SHADOW_RIFT_PIECE.get(), 0, makeBounds(center, radius, verticalRadius));
        this.center = center;
        this.radius = radius;
        this.verticalRadius = verticalRadius;
        this.portalPos = portalPos;
        this.portalSize = portalSize;
        this.seed = seed;
    }

    public ShadowRiftPiece(CompoundTag tag) {
        super(ModStructures.SHADOW_RIFT_PIECE.get(), tag);
        this.center = new BlockPos(tag.getIntOr("CX", 0), tag.getIntOr("CY", 0), tag.getIntOr("CZ", 0));
        this.radius = tag.getIntOr("R", 6);
        this.verticalRadius = tag.getIntOr("RY", 4);
        this.portalPos = new BlockPos(tag.getIntOr("PX", 0), tag.getIntOr("PY", 0), tag.getIntOr("PZ", 0));
        this.portalSize = tag.getIntOr("PS", 1);
        this.seed = tag.getLongOr("Seed", 0L);
    }

    private static BoundingBox makeBounds(BlockPos center, int radius, int verticalRadius) {
        int h = (int) Math.ceil(radius * (1.0 + EDGE_NOISE));
        int v = (int) Math.ceil(verticalRadius * (1.0 + EDGE_NOISE));
        // 门上方的净空可能超出椭球，顶部多留几格。
        return new BoundingBox(center.getX() - h, center.getY() - v, center.getZ() - h,
                center.getX() + h, center.getY() + Math.max(v, PORTAL_HEADROOM + 1), center.getZ() + h);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("CX", center.getX());
        tag.putInt("CY", center.getY());
        tag.putInt("CZ", center.getZ());
        tag.putInt("R", radius);
        tag.putInt("RY", verticalRadius);
        tag.putInt("PX", portalPos.getX());
        tag.putInt("PY", portalPos.getY());
        tag.putInt("PZ", portalPos.getZ());
        tag.putInt("PS", portalSize);
        tag.putLong("Seed", seed);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        List<BoundingBox> protectedBoxes = collectOtherStructures(structureManager, chunkPos, chunkBB);
        SimplexNoise noise = new SimplexNoise(RandomSource.create(seed));
        BlockState shadow = ModBlocks.SHADOW_BLOCK.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        int minX = Math.max(boundingBox.minX(), chunkBB.minX());
        int maxX = Math.min(boundingBox.maxX(), chunkBB.maxX());
        int minZ = Math.max(boundingBox.minZ(), chunkBB.minZ());
        int maxZ = Math.min(boundingBox.maxZ(), chunkBB.maxZ());
        int minY = Math.max(boundingBox.minY(), level.getMinY());
        int maxY = Math.min(boundingBox.maxY(), level.getMaxY());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    if (!insideRift(noise, x, y, z) || isProtected(protectedBoxes, x, y, z)) {
                        continue;
                    }
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!canTouch(state)) {
                        continue;
                    }
                    if (state.is(ModStructures.SHADOW_RIFT_REPLACEABLE)) {
                        level.setBlock(pos, shadow, SET_FLAGS);
                    } else if (state.is(ModStructures.SHADOW_RIFT_CLEARABLE)) {
                        level.setBlock(pos, air, SET_FLAGS);
                    }
                }
            }
        }

        placePortal(level, chunkBB, protectedBoxes, shadow, air);
    }

    /**
     * 门的每一格：地面换成阴影方块，门所在层及上方净空里只清掉白名单方块（地表的树干、树叶、草），
     * 碰到白名单以外的方块就放弃这一格。
     */
    private void placePortal(WorldGenLevel level, BoundingBox chunkBB, List<BoundingBox> protectedBoxes,
                             BlockState shadow, BlockState air) {
        BlockState portal = ModBlocks.CONSTANT_PORTAL.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = 0; dx < portalSize; dx++) {
            for (int dz = 0; dz < portalSize; dz++) {
                int x = portalPos.getX() + dx;
                int z = portalPos.getZ() + dz;
                int y = portalPos.getY();
                if (!chunkBB.isInside(x, y, z) || isProtected(protectedBoxes, x, y, z)) {
                    continue;
                }
                if (!clearHeadroom(level, pos, x, y, z, air)) {
                    continue;
                }
                pos.set(x, y - 1, z);
                BlockState floor = level.getBlockState(pos);
                if (canTouch(floor) && floor.is(ModStructures.SHADOW_RIFT_REPLACEABLE)) {
                    level.setBlock(pos, shadow, SET_FLAGS);
                }
                pos.set(x, y, z);
                level.setBlock(pos, portal, SET_FLAGS);
                // 生成阶段要主动取一次，才会创建传送门的方块实体（渲染和旋风都靠它）。
                level.getBlockEntity(pos);
            }
        }
    }

    private static boolean clearHeadroom(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int y, int z, BlockState air) {
        for (int dy = 0; dy < PORTAL_HEADROOM; dy++) {
            pos.set(x, y + dy, z);
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            // 阴影方块也算：门上方的树干和树叶在前一步已经被换成了阴影方块。
            boolean whitelisted = state.is(ModStructures.SHADOW_RIFT_CLEARABLE)
                    || state.is(ModStructures.SHADOW_RIFT_REPLACEABLE)
                    || state.is(ModBlocks.SHADOW_BLOCK.get());
            if (!canTouch(state) || !whitelisted) {
                return false;
            }
        }
        for (int dy = 0; dy < PORTAL_HEADROOM; dy++) {
            pos.set(x, y + dy, z);
            if (!level.getBlockState(pos).isAir()) {
                level.setBlock(pos, air, SET_FLAGS);
            }
        }
        return true;
    }

    private boolean insideRift(SimplexNoise noise, int x, int y, int z) {
        double dx = (x + 0.5 - (center.getX() + 0.5)) / radius;
        double dy = (y + 0.5 - (center.getY() + 0.5)) / verticalRadius;
        double dz = (z + 0.5 - (center.getZ() + 0.5)) / radius;
        double limit = 1.0 + EDGE_NOISE * noise.getValue(x * EDGE_NOISE_SCALE, y * EDGE_NOISE_SCALE, z * EDGE_NOISE_SCALE);
        return dx * dx + dy * dy + dz * dz < limit * limit;
    }

    /** 带方块实体、含液体、或原版标记为地物不能替换的方块，一律不碰。 */
    private static boolean canTouch(BlockState state) {
        return !state.isAir()
                && !state.hasBlockEntity()
                && state.getFluidState().isEmpty()
                && !state.is(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    /** 经过本区块的其他结构（原版或其他模组）的部件范围，外扩一点。 */
    private static List<BoundingBox> collectOtherStructures(StructureManager structureManager, ChunkPos chunkPos, BoundingBox chunkBB) {
        List<BoundingBox> boxes = new ArrayList<>();
        BoundingBox wideChunk = chunkBB.inflatedBy(STRUCTURE_MARGIN);
        for (StructureStart start : structureManager.startsForStructure(chunkPos, structure -> true)) {
            if (!start.isValid() || isShadowRift(start)) {
                continue;
            }
            for (StructurePiece piece : start.getPieces()) {
                if (piece.getBoundingBox().intersects(wideChunk)) {
                    boxes.add(piece.getBoundingBox().inflatedBy(STRUCTURE_MARGIN));
                }
            }
        }
        return boxes;
    }

    private static boolean isShadowRift(StructureStart start) {
        for (StructurePiece piece : start.getPieces()) {
            if (piece instanceof ShadowRiftPiece) {
                return true;
            }
        }
        return false;
    }

    private static boolean isProtected(List<BoundingBox> boxes, int x, int y, int z) {
        for (BoundingBox box : boxes) {
            if (box.isInside(x, y, z)) {
                return true;
            }
        }
        return false;
    }
}
