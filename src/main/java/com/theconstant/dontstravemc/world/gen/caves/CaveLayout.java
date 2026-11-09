package com.theconstant.dontstravemc.world.gen.caves;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 洞穴维度的平面与剖面布局：一片岛，其余全是深渊。纯函数，生成器、群系源、传送共用。
 * <p>
 * 剖面（Y 0~127）：底部基岩 0~4 只在岛心下方；岛体下表面向边缘收缩成倒锥；
 * 中间是开阔层；顶部岩层从 {@link #ceilingBottom} 一直到 122，123~127 为基岩。
 * 石柱两头粗、中间细，连接地面和天花板。
 */
public final class CaveLayout {

    public static final int MIN_Y = 0;
    public static final int TOP_Y = 127;
    public static final int BEDROCK_LAYERS = 5;
    public static final int GROUND_LEVEL = 40;
    public static final int CEILING_LEVEL = 88;

    private static final double ISLAND_RADIUS = 420.0;
    private static final int PILLAR_CELL = 40;

    private static volatile CaveLayout current;

    private final long seed;
    private final SimplexNoise shape;
    private final SimplexNoise detail;
    private final SimplexNoise holes;
    private final SimplexNoise height;
    private final SimplexNoise ceiling;
    /** 每格最多一根柱，逐方块查询时避免反复采样布局。 */
    private final Map<Long, Optional<Pillar>> pillarCache = new ConcurrentHashMap<>();

    private CaveLayout(long seed) {
        this.seed = seed;
        RandomSource random = RandomSource.create(seed ^ 0x5CA7E5L);
        this.shape = new SimplexNoise(random);
        this.detail = new SimplexNoise(random);
        this.holes = new SimplexNoise(random);
        this.height = new SimplexNoise(random);
        this.ceiling = new SimplexNoise(random);
    }

    public static void bind(long seed) {
        CaveLayout existing = current;
        if (existing == null || existing.seed != seed) {
            current = new CaveLayout(seed);
        }
    }

    public static CaveLayout get() {
        CaveLayout layout = current;
        if (layout == null) {
            layout = new CaveLayout(0L);
            current = layout;
        }
        return layout;
    }

    public enum Region { ROCKY, SPIDER, BIRD, MUDDY }

    /**
     * @param land    该列是否有岛体
     * @param edge    0=岸边，1=岛心（离深渊足够远）
     * @param surface 地表最高实心方块的 Y
     * @param bottom  岛体下表面 Y（≤ BEDROCK_LAYERS 时与底部基岩相连）
     * @param ceiling 天花板下沿 Y
     */
    public record Column(boolean land, double edge, int surface, int bottom, int ceiling, Region region) {
    }

    public Column sample(int x, int z) {
        double r = Math.sqrt((double) x * x + (double) z * z) / ISLAND_RADIUS;
        double outline = r
                + 0.32 * this.shape.getValue(x / 160.0, z / 160.0)
                + 0.12 * this.detail.getValue(x / 45.0, z / 45.0);
        // 岛内的深渊坑：值越接近 1 越靠近坑边
        double hole = this.holes.getValue(x / 110.0, z / 110.0);
        double landness = Math.min(1.0 - outline, (0.62 - hole) * 1.6);

        int ceil = CEILING_LEVEL + (int) Math.round(7.0 * this.ceiling.getValue(x / 38.0, z / 38.0)
                + 3.0 * this.detail.getValue(x / 12.0, z / 12.0));
        int surface = GROUND_LEVEL + (int) Math.round(4.0 * this.height.getValue(x / 32.0, z / 32.0)
                + 1.5 * this.detail.getValue(x / 9.0, z / 9.0));
        Region region = region(x, z);
        if (landness <= 0.0) {
            return new Column(false, 0.0, surface, surface, ceil, region);
        }
        double edge = Mth.clamp(landness * 5.0, 0.0, 1.0);
        // 下表面向边缘收缩：岛心一直连到底部基岩，岸边只剩几格厚
        int thickness = 4 + (int) Math.round(Math.pow(edge, 0.6) * (surface - 2));
        int bottom = Math.max(MIN_Y, surface - thickness);
        return new Column(true, edge, surface, bottom, ceil, region);
    }

    private Region region(int x, int z) {
        // 抖动网格 Voronoi，单元 180 格
        int cell = 180;
        int cx = Math.floorDiv(x, cell);
        int cz = Math.floorDiv(z, cell);
        double best = Double.MAX_VALUE;
        long bestHash = 0L;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                long h = hash(cx + dx, cz + dz, 11);
                double px = (cx + dx + unit(h)) * cell;
                double pz = (cz + dz + unit(h >>> 20)) * cell;
                double d = (px - x) * (px - x) + (pz - z) * (pz - z);
                if (d < best) {
                    best = d;
                    bestHash = h;
                }
            }
        }
        Region[] values = Region.values();
        return values[(int) Math.floorMod(bestHash >>> 40, values.length)];
    }

    /**
     * 该点是否在石柱（含悬挂、站立两种）内。只在开阔层内调用。
     */
    public boolean insidePillar(int x, int y, int z) {
        int cx = Math.floorDiv(x, PILLAR_CELL);
        int cz = Math.floorDiv(z, PILLAR_CELL);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Pillar pillar = pillar(cx + dx, cz + dz);
                if (pillar == null) {
                    continue;
                }
                double ddx = x - pillar.x;
                double ddz = z - pillar.z;
                double dist = Math.sqrt(ddx * ddx + ddz * ddz);
                if (dist > pillar.rMax + 2) {
                    continue;
                }
                double radius = pillar.radiusAt(y);
                if (radius <= 0.0) {
                    continue;
                }
                radius += 0.8 * this.detail.getValue(x / 3.0 + y / 7.0, z / 3.0 - y / 7.0);
                if (dist < radius) {
                    return true;
                }
            }
        }
        return false;
    }

    /** FULL 连接上下；HANGING 只从天花板垂下；STANDING 只从地面立起。 */
    public enum PillarKind { FULL, HANGING, STANDING }

    private record Pillar(PillarKind kind, double x, double z, double rMin, double rMax, int floor, int ceiling, int length) {

        /** 该高度的截面半径，≤0 表示这一层没有柱体。 */
        double radiusAt(int y) {
            return switch (this.kind) {
                case FULL -> {
                    double t = Mth.clamp((y - this.floor) / (double) Math.max(1, this.ceiling - this.floor), 0.0, 1.0);
                    // 沙漏形：两端 rMax，中间 rMin
                    yield this.rMin + (this.rMax - this.rMin) * Math.pow(Math.abs(2.0 * t - 1.0), 1.7);
                }
                case HANGING -> taper(this.ceiling - y);
                case STANDING -> taper(y - this.floor);
            };
        }

        /** 从根部 rMax 收到尖端约 0.6。 */
        private double taper(int fromBase) {
            if (fromBase < 0 || fromBase > this.length) {
                return -1.0;
            }
            double s = fromBase / (double) this.length;
            return 0.6 + (this.rMax - 0.6) * Math.pow(1.0 - s, 1.3);
        }
    }

    private Pillar pillar(int cx, int cz) {
        if (this.pillarCache.size() > 65536) {
            this.pillarCache.clear();
        }
        return this.pillarCache.computeIfAbsent(((long) cx << 32) ^ (cz & 0xFFFFFFFFL),
                key -> Optional.ofNullable(this.computePillar(cx, cz))).orElse(null);
    }

    private Pillar computePillar(int cx, int cz) {
        long h = hash(cx, cz, 37);
        if (unit(h >>> 8) > 0.7) {
            return null;
        }
        long h2 = hash(cx, cz, 53);
        double roll = unit(h2);
        PillarKind kind = roll < 0.45 ? PillarKind.FULL : roll < 0.75 ? PillarKind.HANGING : PillarKind.STANDING;
        double px = (cx + 0.2 + 0.6 * unit(h)) * PILLAR_CELL;
        double pz = (cz + 0.2 + 0.6 * unit(h >>> 20)) * PILLAR_CELL;
        Column column = this.sample((int) px, (int) pz);
        // 悬挂柱可以挂在深渊上方；落地的两种只长在岛心
        if (kind != PillarKind.HANGING && (!column.land() || column.edge() < 0.6)) {
            return null;
        }
        boolean big = unit(h >>> 32) < 0.4;
        double rMax = big ? 9.0 + 3.0 * unit(h >>> 44) : 5.0 + 2.0 * unit(h >>> 44);
        if (kind != PillarKind.FULL) {
            rMax *= 0.75;
        }
        double rMin = big ? 3.5 : 2.0;
        int floor = column.land() ? column.surface() : GROUND_LEVEL;
        int span = Math.max(4, column.ceiling() - floor);
        int length = (int) Math.round(span * (0.35 + 0.35 * unit(h2 >>> 20)));
        return new Pillar(kind, px, pz, rMin, rMax, floor, column.ceiling(), length);
    }

    private long hash(int a, int b, int salt) {
        long h = this.seed ^ ((long) a * 0x9E3779B97F4A7C15L) ^ ((long) b * 0xC2B2AE3D27D4EB4FL) ^ salt;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;
        return h;
    }

    private static double unit(long bits) {
        return (bits & 0xFFFFF) / (double) 0x100000;
    }
}
