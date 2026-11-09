package com.theconstant.dontstravemc.world.gen.caves;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.List;
import java.util.stream.Stream;

/**
 * 按 {@link CaveLayout} 的分区取群系。biomes 列表顺序对应 {@link CaveLayout.Region}。
 */
public class CaveBiomeSource extends BiomeSource {

    public static final MapCodec<CaveBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Biome.CODEC.listOf().fieldOf("biomes").forGetter(CaveBiomeSource::biomes)
    ).apply(instance, instance.stable(CaveBiomeSource::new)));

    private final List<Holder<Biome>> biomes;

    public CaveBiomeSource(List<Holder<Biome>> biomes) {
        if (biomes.isEmpty()) {
            throw new IllegalStateException("Cave biome source needs at least one biome");
        }
        this.biomes = List.copyOf(biomes);
    }

    public List<Holder<Biome>> biomes() {
        return this.biomes;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.biomes.stream();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        CaveLayout.Region region = CaveLayout.get().sample(QuartPos.toBlock(quartX), QuartPos.toBlock(quartZ)).region();
        return this.biomes.get(region.ordinal() % this.biomes.size());
    }
}
