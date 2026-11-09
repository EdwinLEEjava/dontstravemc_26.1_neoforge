package com.theconstant.dontstravemc.world.gen.island;

import com.mojang.serialization.MapCodec;
import com.theconstant.dontstravemc.TheConstant;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IslandWorldgen {

    public static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, TheConstant.MODID);
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, TheConstant.MODID);

    public static final DeferredHolder<MapCodec<? extends BiomeSource>, MapCodec<IslandBiomeSource>> ISLAND_BIOMES =
            BIOME_SOURCES.register("island", () -> IslandBiomeSource.CODEC);
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<IslandChunkGenerator>> ISLAND_CHUNK_GENERATOR =
            CHUNK_GENERATORS.register("island", () -> IslandChunkGenerator.CODEC);

    public static final DeferredHolder<MapCodec<? extends BiomeSource>, MapCodec<com.theconstant.dontstravemc.world.gen.caves.CaveBiomeSource>> CAVE_BIOMES =
            BIOME_SOURCES.register("caves", () -> com.theconstant.dontstravemc.world.gen.caves.CaveBiomeSource.CODEC);
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<com.theconstant.dontstravemc.world.gen.caves.CaveChunkGenerator>> CAVE_CHUNK_GENERATOR =
            CHUNK_GENERATORS.register("caves", () -> com.theconstant.dontstravemc.world.gen.caves.CaveChunkGenerator.CODEC);

    private IslandWorldgen() {
    }

    public static void register() {
        TheConstant.LOGGER.info("Island worldgen codecs registered for {}", TheConstant.MODID);
    }
}
