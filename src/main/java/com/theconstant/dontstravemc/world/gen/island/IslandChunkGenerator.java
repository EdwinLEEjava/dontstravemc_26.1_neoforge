package com.theconstant.dontstravemc.world.gen.island;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

/**
 * 先走现有噪声（洞穴留在深处），再把每一列的地表改成岛屿布局的高度。
 */
public class IslandChunkGenerator extends NoiseBasedChunkGenerator {

    public static final MapCodec<IslandChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(IslandChunkGenerator::generatorSettings)
    ).apply(instance, instance.stable(IslandChunkGenerator::new)));

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final int CRUST = 4;

    public IslandChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
    }

    @Override
    protected MapCodec<IslandChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSets, RandomState randomState, long legacyLevelSeed) {
        IslandLayout.bind(legacyLevelSeed);
        return super.createState(structureSets, randomState, legacyLevelSeed);
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        return super.fillFromNoise(blender, randomState, structureManager, chunk).thenApply(filled -> {
            this.reshapeToIsland(filled);
            return filled;
        });
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
        IslandLayout.Sample sample = IslandLayout.get().sample(x, z);
        if (!sample.land() && type.isOpaque().test(WATER)) {
            return this.getSeaLevel() + 1;
        }
        return sample.surfaceY() + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        int minY = heightAccessor.getMinY();
        int count = heightAccessor.getHeight();
        BlockState[] states = new BlockState[count];
        BlockState filler = this.generatorSettings().value().defaultBlock();
        IslandLayout.Sample sample = IslandLayout.get().sample(x, z);
        int sea = this.getSeaLevel();
        int floor = sample.land() ? sample.surfaceY() : Math.min(sample.surfaceY(), sea - 2);
        for (int i = 0; i < count; i++) {
            int y = minY + i;
            if (sample.land()) {
                states[i] = y > floor ? AIR : filler;
            } else if (y > sea) {
                states[i] = AIR;
            } else if (y > floor) {
                states[i] = WATER;
            } else {
                states[i] = filler;
            }
        }
        return new NoiseColumn(minY, states);
    }

    /**
     * 海里每一列的水面都停在海平面。陆地按布局填到实心为止，下面的洞穴保留。
     */
    private void reshapeToIsland(ChunkAccess chunk) {
        BlockState filler = this.generatorSettings().value().defaultBlock();
        int sea = this.getSeaLevel();
        int minY = chunk.getMinY() + 5;
        ChunkPos pos = chunk.getPos();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int blockX = pos.getMinBlockX() + localX;
                int blockZ = pos.getMinBlockZ() + localZ;
                IslandLayout.Sample sample = IslandLayout.get().sample(blockX, blockZ);
                int columnTop = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, localX, localZ);
                if (sample.land()) {
                    int surface = sample.surfaceY();
                    int top = Math.max(columnTop, surface);
                    for (int y = top; y > surface; y--) {
                        this.setColumnBlock(chunk, cursor, blockX, y, blockZ, AIR);
                    }
                    this.fillCrust(chunk, cursor, blockX, blockZ, surface, minY, filler);
                } else {
                    int floor = Math.min(sample.surfaceY(), sea - 2);
                    int top = Math.max(columnTop, sea);
                    for (int y = top; y > sea; y--) {
                        this.setColumnBlock(chunk, cursor, blockX, y, blockZ, AIR);
                    }
                    for (int y = sea; y > floor; y--) {
                        this.setColumnBlock(chunk, cursor, blockX, y, blockZ, WATER);
                    }
                    this.fillCrust(chunk, cursor, blockX, blockZ, floor, minY, filler);
                }
            }
        }
    }

    private void fillCrust(ChunkAccess chunk, BlockPos.MutableBlockPos cursor, int blockX, int blockZ, int surface, int minY, BlockState filler) {
        boolean connected = false;
        for (int y = surface; y >= minY; y--) {
            BlockState current = chunk.getBlockState(cursor.set(blockX, y, blockZ));
            boolean solid = !current.isAir() && current.getFluidState().isEmpty();
            if (!connected || y > surface - CRUST) {
                this.setColumnBlock(chunk, cursor, blockX, y, blockZ, filler);
                if (solid && y < surface) {
                    connected = true;
                }
            } else {
                break;
            }
        }
    }

    private void setColumnBlock(ChunkAccess chunk, BlockPos.MutableBlockPos cursor, int x, int y, int z, BlockState state) {
        if (!chunk.getBlockState(cursor.set(x, y, z)).equals(state)) {
            chunk.setBlockState(cursor, state);
        }
    }
}
