package com.theconstant.dontstravemc.world.gen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * 阴影裂隙：把一片区域里的原版地形换成阴影方块，并在其中放一个 N×N（1 ≤ N ≤ 4）的漩涡传送门。
 * <p>
 * 选址只用 {@link ChunkGenerator} 的通用接口，所以别的模组换掉主世界生成器时最多是找不到位置、不生成。
 */
public class ShadowRiftStructure extends Structure {

    public static final int MAX_PORTAL_SIZE = 4;
    /** 门的候选区域边长（以区块中心为中心）。只在这个范围里取样，控制选址开销。 */
    private static final int SEARCH_SPAN = 8;
    /** 门上方需要空出来的格数（含门所在那层）。 */
    private static final int HEADROOM = 3;
    private static final int UNDERGROUND_MIN_Y = -48;
    private static final int UNDERGROUND_MAX_Y = 32;

    public static final MapCodec<ShadowRiftStructure> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            settingsCodec(i),
            Placement.CODEC.fieldOf("placement").forGetter(s -> s.placement),
            IntProviders.codec(3, 24).fieldOf("radius").forGetter(s -> s.radius),
            IntProviders.codec(1, MAX_PORTAL_SIZE).fieldOf("portal_size").forGetter(s -> s.portalSize)
    ).apply(i, ShadowRiftStructure::new));

    private final Placement placement;
    private final IntProvider radius;
    private final IntProvider portalSize;

    public ShadowRiftStructure(StructureSettings settings, Placement placement, IntProvider radius, IntProvider portalSize) {
        super(settings);
        this.placement = placement;
        this.radius = radius;
        this.portalSize = portalSize;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.SHADOW_RIFT.get();
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        WorldgenRandom random = context.random();
        int r = radius.sample(random);
        int wantedSize = Math.clamp(portalSize.sample(random), 1, MAX_PORTAL_SIZE);
        long pieceSeed = random.nextLong();
        int targetY = UNDERGROUND_MIN_Y + random.nextInt(UNDERGROUND_MAX_Y - UNDERGROUND_MIN_Y + 1);

        int originX = context.chunkPos().getMiddleBlockX() - SEARCH_SPAN / 2;
        int originZ = context.chunkPos().getMiddleBlockZ() - SEARCH_SPAN / 2;
        ColumnGrid grid = placement == Placement.SURFACE
                ? ColumnGrid.surface(context, originX, originZ)
                : ColumnGrid.underground(context, originX, originZ);

        for (int size = wantedSize; size >= 1; size--) {
            Optional<BlockPos> spot = grid.findSpot(size, targetY);
            if (spot.isEmpty()) {
                continue;
            }
            BlockPos portalPos = spot.get();
            if (isExcludedBiome(context, portalPos)) {
                return Optional.empty();
            }
            int finalSize = size;
            BlockPos center = new BlockPos(portalPos.getX() + size / 2, portalPos.getY() - 1, portalPos.getZ() + size / 2);
            int verticalRadius = placement == Placement.SURFACE ? Math.max(3, Math.round(r * 0.6F)) : Math.max(3, Math.round(r * 0.8F));
            return Optional.of(new GenerationStub(portalPos, builder -> builder.addPiece(
                    new ShadowRiftPiece(center, r, verticalRadius, portalPos, finalSize, pieceSeed))));
        }
        return Optional.empty();
    }

    private static boolean isExcludedBiome(GenerationContext context, BlockPos pos) {
        Holder<Biome> biome = context.biomeSource().getNoiseBiome(
                QuartPos.fromBlock(pos.getX()), QuartPos.fromBlock(pos.getY()), QuartPos.fromBlock(pos.getZ()),
                context.randomState().sampler());
        return biome.is(ModStructures.SHADOW_RIFT_EXCLUDED_BIOMES);
    }

    /** 门能站的地面：下面是实心方块，门所在那层及以上 {@link #HEADROOM} 格是空气（不含水）。 */
    private static boolean isFloorAt(NoiseColumn column, int y) {
        BlockState below = column.getBlock(y - 1);
        if (below.isAir() || !below.getFluidState().isEmpty()) {
            return false;
        }
        for (int dy = 0; dy < HEADROOM; dy++) {
            if (!column.getBlock(y + dy).isAir()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 候选区域里每一列可以放门的高度。地表每列至多一个高度（-1 表示不能放）；
     * 地下每列可能有多层洞穴地面。
     */
    private record ColumnGrid(int originX, int originZ, int minY, boolean[][][] floors) {

        static ColumnGrid surface(GenerationContext context, int originX, int originZ) {
            ChunkGenerator generator = context.chunkGenerator();
            LevelHeightAccessor height = context.heightAccessor();
            RandomState randomState = context.randomState();
            int minY = height.getMinY();
            boolean[][][] floors = new boolean[SEARCH_SPAN][SEARCH_SPAN][];
            for (int dx = 0; dx < SEARCH_SPAN; dx++) {
                for (int dz = 0; dz < SEARCH_SPAN; dz++) {
                    int x = originX + dx;
                    int z = originZ + dz;
                    // 两种高度图不一致说明表面是水，不在水上放门。
                    int top = generator.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, height, randomState);
                    int floor = generator.getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, height, randomState);
                    boolean[] column = new boolean[height.getHeight()];
                    int portalY = top + 1;
                    if (top == floor && portalY > minY && portalY - minY < column.length) {
                        column[portalY - minY] = true;
                    }
                    floors[dx][dz] = column;
                }
            }
            return new ColumnGrid(originX, originZ, minY, floors);
        }

        static ColumnGrid underground(GenerationContext context, int originX, int originZ) {
            ChunkGenerator generator = context.chunkGenerator();
            LevelHeightAccessor height = context.heightAccessor();
            int minY = height.getMinY();
            int lowY = Math.max(UNDERGROUND_MIN_Y, minY + 2);
            int highY = Math.min(UNDERGROUND_MAX_Y, height.getMaxY() - HEADROOM);
            boolean[][][] floors = new boolean[SEARCH_SPAN][SEARCH_SPAN][];
            for (int dx = 0; dx < SEARCH_SPAN; dx++) {
                for (int dz = 0; dz < SEARCH_SPAN; dz++) {
                    NoiseColumn noise = generator.getBaseColumn(originX + dx, originZ + dz, height, context.randomState());
                    boolean[] column = new boolean[height.getHeight()];
                    for (int y = lowY; y <= highY; y++) {
                        column[y - minY] = isFloorAt(noise, y);
                    }
                    floors[dx][dz] = column;
                }
            }
            return new ColumnGrid(originX, originZ, minY, floors);
        }

        /** 找一块 size×size、所有列地面同高的位置，返回门的最小角。优先靠近候选区中心，其次靠近 targetY。 */
        Optional<BlockPos> findSpot(int size, int targetY) {
            BlockPos best = null;
            long bestScore = Long.MAX_VALUE;
            int center2 = SEARCH_SPAN - size; // 窗口最小角的“中心”坐标乘 2
            for (int wx = 0; wx + size <= SEARCH_SPAN; wx++) {
                for (int wz = 0; wz + size <= SEARCH_SPAN; wz++) {
                    int dist = Math.abs(wx * 2 - center2) + Math.abs(wz * 2 - center2);
                    boolean[] first = floors[wx][wz];
                    for (int idx = 0; idx < first.length; idx++) {
                        if (!first[idx] || !allHave(wx, wz, size, idx)) {
                            continue;
                        }
                        int y = idx + minY;
                        long score = (long) dist * 10_000L + Math.abs(y - targetY);
                        if (score < bestScore) {
                            bestScore = score;
                            best = new BlockPos(originX + wx, y, originZ + wz);
                        }
                    }
                }
            }
            return Optional.ofNullable(best);
        }

        private boolean allHave(int wx, int wz, int size, int idx) {
            for (int dx = 0; dx < size; dx++) {
                for (int dz = 0; dz < size; dz++) {
                    if (!floors[wx + dx][wz + dz][idx]) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    public enum Placement implements StringRepresentable {
        SURFACE("surface"),
        UNDERGROUND("underground");

        public static final Codec<Placement> CODEC = StringRepresentable.fromEnum(Placement::values);
        private final String name;

        Placement(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
