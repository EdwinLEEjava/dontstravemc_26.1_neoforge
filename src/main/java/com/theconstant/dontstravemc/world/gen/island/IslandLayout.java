package com.theconstant.dontstravemc.world.gen.island;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 一座由六边形地块拼成的岛。斜边对齐后共用整条边，沙滩只出现在外侧海岸。
 */
public final class IslandLayout {

    private static final Logger LOGGER = LoggerFactory.getLogger(IslandLayout.class);

    private static final int SHORE_SAND = 24;
    private static final int SHORE_END = 40;
    private static final double SQRT3 = 1.7320508075688772;
    private static final IslandBiome[] BRANCH_BIOMES = {
            IslandBiome.EVERGREEN_FOREST,
            IslandBiome.GRASSLAND,
            IslandBiome.BIRCH_FOREST,
            IslandBiome.PIG_VILLAGE_FOREST,
            IslandBiome.BEEFALO_SAVANNA,
            IslandBiome.SAVANNA,
            IslandBiome.DESERT,
            IslandBiome.ROCKYLAND,
            IslandBiome.SWAMP,
            IslandBiome.MERM_SWAMP,
            IslandBiome.WALRUS_FOREST,
            IslandBiome.CEMETERY_FOREST,
            IslandBiome.LUNAR_FOREST,
            IslandBiome.MANDRAKE_FOREST,
            IslandBiome.METEOR_PLAIN,
            IslandBiome.POND_FOREST
    };

    private static volatile IslandLayout current;

    private final long seed;
    private final double radius;
    private final Map<Long, IslandBiome> hexes;

    private IslandLayout(long seed, double radius, Map<Long, IslandBiome> hexes) {
        this.seed = seed;
        this.radius = radius;
        this.hexes = hexes;
    }

    public static void bind(long seed) {
        IslandLayout existing = current;
        if (existing != null && existing.seed == seed) {
            return;
        }
        IslandLayout layout = generate(seed);
        current = layout;
        LOGGER.info("Constant island layout bound for seed {}", seed);
    }

    public static IslandLayout get() {
        IslandLayout layout = current;
        if (layout == null) {
            bind(0L);
            layout = current;
        }
        return layout;
    }

    public long seed() {
        return this.seed;
    }

    public Sample sample(int blockX, int blockZ) {
        double qf = (SQRT3 / 3.0 * blockX - blockZ / 3.0) / this.radius;
        double rf = (2.0 / 3.0 * blockZ) / this.radius;
        int q = 0;
        int r = 0;
        int[] axial = roundAxial(qf, rf);
        q = axial[0];
        r = axial[1];
        IslandBiome biome = this.hexes.get(axialKey(q, r));
        double centerX = this.radius * SQRT3 * (q + r / 2.0);
        double centerZ = this.radius * 1.5 * r;
        double localX = blockX - centerX;
        double localZ = blockZ - centerZ;
        if (biome == null || !insideHex(localX, localZ)) {
            double far = Math.hypot(blockX, blockZ);
            IslandBiome water = far > 1700.0 ? IslandBiome.DEEP_OCEAN : IslandBiome.OCEAN;
            int floor = Mth.clamp(water.surfaceY() + (int) Math.round(fbm(this.seed ^ 0x33L, blockX * 0.008, blockZ * 0.008) * 5.0), 24, IslandBiome.SEA_LEVEL - 4);
            return new Sample(water, floor, false);
        }

        double coast = exposedEdgeDistance(q, r, localX, localZ);
        double origin = Math.hypot(blockX, blockZ);
        if (coast < SHORE_SAND && origin > 96.0) {
            int shore = IslandBiome.SEA_LEVEL + 1 + (coast > 16.0 ? 1 : 0);
            return new Sample(IslandBiome.BEACH, shore, true);
        }

        double inland = Double.isInfinite(coast) ? this.radius : coast;
        int surface = landHeight(biome, inland, blockX, blockZ);
        if (origin < 80.0) {
            double blend = origin / 80.0;
            surface = (int) Math.round(Mth.lerp(blend, 78.0, surface));
            surface = Math.max(IslandBiome.SEA_LEVEL + 1, surface);
            biome = IslandBiome.ENTRY_FOREST;
        }
        return new Sample(biome, surface, true);
    }

    private boolean insideHex(double localX, double localZ) {
        double apothem = this.radius * SQRT3 / 2.0;
        for (int[] step : AXIAL) {
            double[] normal = edgeNormal(step[0], step[1]);
            double distance = apothem - (localX * normal[0] + localZ * normal[1]);
            if (distance < -0.75) {
                return false;
            }
        }
        return true;
    }

    /**
     * 只量朝向空格子的那几条边。三块陆地共用的顶点在每块内部，不会再被当成海岸。
     */
    private double exposedEdgeDistance(int q, int r, double localX, double localZ) {
        double apothem = this.radius * SQRT3 / 2.0;
        double nearest = Double.POSITIVE_INFINITY;
        for (int[] step : AXIAL) {
            if (this.hexes.containsKey(axialKey(q + step[0], r + step[1]))) {
                continue;
            }
            double[] normal = edgeNormal(step[0], step[1]);
            double distance = apothem - (localX * normal[0] + localZ * normal[1]);
            nearest = Math.min(nearest, distance);
        }
        return nearest;
    }

    private static double[] edgeNormal(int dq, int dr) {
        double x = SQRT3 * (dq + dr / 2.0);
        double z = 1.5 * dr;
        double length = Math.hypot(x, z);
        return new double[]{x / length, z / length};
    }

    private static int[] roundAxial(double qf, double rf) {
        double xf = qf;
        double zf = rf;
        double yf = -xf - zf;
        int rx = (int) Math.round(xf);
        int ry = (int) Math.round(yf);
        int rz = (int) Math.round(zf);
        double xDiff = Math.abs(rx - xf);
        double yDiff = Math.abs(ry - yf);
        double zDiff = Math.abs(rz - zf);
        if (xDiff > yDiff && xDiff > zDiff) {
            rx = -ry - rz;
        } else if (yDiff > zDiff) {
            ry = -rx - rz;
        } else {
            rz = -rx - ry;
        }
        return new int[]{rx, rz};
    }

    private int landHeight(IslandBiome biome, double inland, int blockX, int blockZ) {
        double blend = smoothstep(SHORE_SAND, SHORE_END, inland);
        double shaped = relief(biome, blockX, blockZ);
        double inlandHeight = biome.surfaceY() + shaped;
        double shore = IslandBiome.SEA_LEVEL + 2.0;
        int height = (int) Math.round(Mth.lerp(blend, shore, inlandHeight));
        return Math.max(IslandBiome.SEA_LEVEL + 1, height);
    }

    private double relief(IslandBiome biome, int blockX, int blockZ) {
        if (biome.relief() <= 0) {
            return 0.0;
        }
        double broad = fbm(this.seed ^ 0x5L, blockX * 0.0045, blockZ * 0.0045);
        double mid = fbm(this.seed ^ 0x9L, blockX * 0.016, blockZ * 0.016);
        double fine = fbm(this.seed ^ 0xDL, blockX * 0.045, blockZ * 0.045);
        double shaped;
        if (biome.ridged()) {
            double ridge = 1.0 - Math.abs(broad * 0.65 + mid * 0.35);
            ridge = ridge * ridge;
            shaped = (ridge * 1.35 - 0.25) + fine * 0.18;
        } else {
            shaped = broad * 0.55 + mid * 0.35 + fine * 0.2;
        }
        return shaped * biome.relief();
    }

    private static IslandLayout generate(long seed) {
        RandomSource random = RandomSource.create(seed);
        double radius = 180.0;
        Map<Long, IslandBiome> hexes = new HashMap<>();
        addHex(hexes, 0, 0, IslandBiome.ENTRY_FOREST);

        int branches = 6;
        for (int branch = 0; branch < branches; branch++) {
            int q = 0;
            int r = 0;
            int direction = branch;
            int steps = 5 + random.nextInt(3);
            for (int step = 0; step < steps; step++) {
                if (random.nextInt(4) == 0) {
                    direction = Math.floorMod(direction + (random.nextBoolean() ? 1 : -1), AXIAL.length);
                }
                int nextQ = q + AXIAL[direction][0];
                int nextR = r + AXIAL[direction][1];
                if (hexes.containsKey(axialKey(nextQ, nextR))) {
                    direction = Math.floorMod(direction + 1, AXIAL.length);
                    nextQ = q + AXIAL[direction][0];
                    nextR = r + AXIAL[direction][1];
                }
                if (hexes.containsKey(axialKey(nextQ, nextR)) || Math.max(Math.abs(nextQ), Math.abs(nextR)) > 9) {
                    break;
                }
                q = nextQ;
                r = nextR;
                addHex(hexes, q, r, BRANCH_BIOMES[random.nextInt(BRANCH_BIOMES.length)]);
            }
        }
        return new IslandLayout(seed, radius, hexes);
    }

    private static final int[][] AXIAL = {
            {1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}
    };

    private static void addHex(Map<Long, IslandBiome> hexes, int q, int r, IslandBiome biome) {
        hexes.putIfAbsent(axialKey(q, r), biome);
    }

    private static long axialKey(int q, int r) {
        return ((long) q << 32) ^ (r & 0xffffffffL);
    }

    private static double fbm(long seed, double x, double z) {
        double sum = 0.0;
        double amplitude = 0.5;
        double frequency = 1.0;
        for (int octave = 0; octave < 4; octave++) {
            sum += amplitude * valueNoise(seed + octave * 101L, x * frequency, z * frequency);
            frequency *= 2.05;
            amplitude *= 0.5;
        }
        return sum;
    }

    private static double valueNoise(long seed, double x, double z) {
        int x0 = Mth.floor(x);
        int z0 = Mth.floor(z);
        double tx = x - x0;
        double tz = z - z0;
        tx = tx * tx * (3.0 - 2.0 * tx);
        tz = tz * tz * (3.0 - 2.0 * tz);
        return Mth.lerp(
                tz,
                Mth.lerp(tx, lattice(seed, x0, z0), lattice(seed, x0 + 1, z0)),
                Mth.lerp(tx, lattice(seed, x0, z0 + 1), lattice(seed, x0 + 1, z0 + 1))
        );
    }

    private static double lattice(long seed, int x, int z) {
        long hash = seed;
        hash ^= x * 0x9E3779B97F4A7C15L;
        hash ^= (long) z * 0xC2B2AE3D27D4EB4FL;
        hash = (hash ^ (hash >>> 30)) * 0xBF58476D1CE4E5B9L;
        hash = (hash ^ (hash >>> 27)) * 0x94D049BB133111EBL;
        hash ^= hash >>> 31;
        return (hash & 0xFFFFL) / 65535.0 * 2.0 - 1.0;
    }

    private static double smoothstep(double edge0, double edge1, double value) {
        double t = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }

    public record Sample(IslandBiome biome, int surfaceY, boolean land) {
    }
}
