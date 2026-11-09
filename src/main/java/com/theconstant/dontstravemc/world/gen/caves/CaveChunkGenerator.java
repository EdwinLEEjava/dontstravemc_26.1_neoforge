package com.theconstant.dontstravemc.world.gen.caves;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
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

import java.util.concurrent.CompletableFuture;

/**
 * 不走原版噪声，按 {@link CaveLayout} 逐列直接写方块：底部基岩、倒锥形岛体、开阔层、沙漏石柱、天花板、顶部基岩。
 * 群系装饰和刷怪仍沿用父类。
 */
public class CaveChunkGenerator extends NoiseBasedChunkGenerator {

    public static final MapCodec<CaveChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(CaveChunkGenerator::generatorSettings)
    ).apply(instance, instance.stable(CaveChunkGenerator::new)));

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();

    public CaveChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
    }

    @Override
    protected MapCodec<CaveChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSets, RandomState randomState, long legacyLevelSeed) {
        CaveLayout.bind(legacyLevelSeed);
        return super.createState(structureSets, randomState, legacyLevelSeed);
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        CaveLayout layout = CaveLayout.get();
        ChunkPos pos = chunk.getPos();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        Heightmap ocean = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        int minY = Math.max(chunk.getMinY(), CaveLayout.MIN_Y);
        int maxY = Math.min(chunk.getMaxY(), CaveLayout.TOP_Y);
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int x = pos.getMinBlockX() + localX;
                int z = pos.getMinBlockZ() + localZ;
                CaveLayout.Column column = layout.sample(x, z);
                for (int y = minY; y <= maxY; y++) {
                    BlockState state = this.stateAt(layout, column, x, y, z);
                    if (state != AIR) {
                        chunk.setBlockState(cursor.set(x, y, z), state);
                        ocean.update(localX, y, localZ, state);
                        surface.update(localX, y, localZ, state);
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    private BlockState stateAt(CaveLayout layout, CaveLayout.Column column, int x, int y, int z) {
        int top = CaveLayout.TOP_Y;
        // 顶部基岩，下沿参差
        if (y > top - CaveLayout.BEDROCK_LAYERS + (int) (noiseBits(x, y, z) % 3)) {
            return BEDROCK;
        }
        if (y >= column.ceiling()) {
            return this.rock();
        }
        if (!column.land()) {
            // 深渊上方只可能有悬挂柱
            return layout.insidePillar(x, y, z) ? this.rock() : AIR;
        }
        if (y < column.bottom()) {
            return AIR;
        }
        // 岛心下方连到底部基岩
        if (column.bottom() <= CaveLayout.BEDROCK_LAYERS && y < 1 + (int) (noiseBits(x, y, z) % 4)) {
            return BEDROCK;
        }
        if (y <= column.surface()) {
            return y == column.surface() ? topBlock(column.region()) : this.rock();
        }
        if (layout.insidePillar(x, y, z)) {
            return this.rock();
        }
        return AIR;
    }

    private BlockState rock() {
        return this.generatorSettings().value().defaultBlock();
    }

    private static BlockState topBlock(CaveLayout.Region region) {
        return switch (region) {
            case MUDDY -> Blocks.MUD.defaultBlockState();
            case BIRD -> Blocks.MOSS_BLOCK.defaultBlockState();
            case SPIDER -> Blocks.COBBLESTONE.defaultBlockState();
            case ROCKY -> Blocks.GRAVEL.defaultBlockState();
        };
    }

    private static long noiseBits(int x, int y, int z) {
        long h = x * 3129871L ^ z * 116129781L ^ y;
        h = h * h * 42317861L + h * 11L;
        return (h >>> 16) & 0xFFFFL;
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState, ChunkAccess protoChunk) {
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk) {
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
        CaveLayout.Column column = CaveLayout.get().sample(x, z);
        return column.land() ? column.surface() + 1 : heightAccessor.getMinY();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        CaveLayout layout = CaveLayout.get();
        CaveLayout.Column column = layout.sample(x, z);
        int minY = heightAccessor.getMinY();
        BlockState[] states = new BlockState[heightAccessor.getHeight()];
        for (int i = 0; i < states.length; i++) {
            int y = minY + i;
            states[i] = y < CaveLayout.MIN_Y || y > CaveLayout.TOP_Y ? AIR : this.stateAt(layout, column, x, y, z);
        }
        return new NoiseColumn(minY, states);
    }
}
