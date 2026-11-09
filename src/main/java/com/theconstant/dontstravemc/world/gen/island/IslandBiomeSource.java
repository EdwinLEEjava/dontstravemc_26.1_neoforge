package com.theconstant.dontstravemc.world.gen.island;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 只读 {@link IslandLayout}。岛外和缺表时都是海。
 */
public class IslandBiomeSource extends BiomeSource {

    public static final MapCodec<IslandBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Biome.CODEC.listOf().fieldOf("biomes").forGetter(IslandBiomeSource::biomes)
    ).apply(instance, instance.stable(IslandBiomeSource::new)));

    private final List<Holder<Biome>> biomes;
    private final Map<ResourceKey<Biome>, Holder<Biome>> byKey;
    private final Holder<Biome> ocean;

    public IslandBiomeSource(List<Holder<Biome>> biomes) {
        this.biomes = List.copyOf(biomes);
        Map<ResourceKey<Biome>, Holder<Biome>> mapped = new HashMap<>();
        for (Holder<Biome> biome : this.biomes) {
            biome.unwrapKey().ifPresent(key -> mapped.put(key, biome));
        }
        this.byKey = Map.copyOf(mapped);
        Holder<Biome> oceanHolder = this.byKey.get(IslandBiome.OCEAN.key());
        if (oceanHolder == null) {
            throw new IllegalStateException("Island biome source is missing dontstravemc:ocean");
        }
        this.ocean = oceanHolder;
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
        IslandLayout.Sample sample = IslandLayout.get().sample(QuartPos.toBlock(quartX), QuartPos.toBlock(quartZ));
        Holder<Biome> biome = this.byKey.get(sample.biome().key());
        return biome == null ? this.ocean : biome;
    }
}
