package com.theconstant.dontstravemc.world.gen.island;

import com.theconstant.dontstravemc.TheConstant;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/**
 * 岛屿栅格上的群系。陆地高度是离海足够远时的地表，海岸会再混到海平面。
 */
public enum IslandBiome {
    OCEAN("ocean", false, 42),
    DEEP_OCEAN("deep_ocean", false, 28),
    BEACH("beach", true, 66),
    ENTRY_FOREST("entry_forest", true, 84),
    EVERGREEN_FOREST("evergreen_forest", true, 88),
    GRASSLAND("grassland", true, 76),
    BIRCH_FOREST("birch_forest", true, 86),
    PIG_VILLAGE_FOREST("pig_village_forest", true, 82),
    BEEFALO_SAVANNA("beefalo_savanna", true, 96),
    SAVANNA("savanna", true, 94),
    DESERT("desert", true, 108),
    ROCKYLAND("rockyland", true, 124),
    SWAMP("swamp", true, 67),
    MERM_SWAMP("merm_swamp", true, 66),
    WALRUS_FOREST("walrus_forest", true, 90),
    CEMETERY_FOREST("cemetery_forest", true, 86),
    LUNAR_FOREST("lunar_forest", true, 92),
    MANDRAKE_FOREST("mandrake_forest", true, 84),
    METEOR_PLAIN("meteor_plain", true, 112),
    POND_FOREST("pond_forest", true, 74);

    public static final int SEA_LEVEL = 63;

    private final ResourceKey<Biome> key;
    private final boolean land;
    private final int surfaceY;

    IslandBiome(String path, boolean land, int surfaceY) {
        this.key = ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TheConstant.MODID, path));
        this.land = land;
        this.surfaceY = surfaceY;
    }

    public ResourceKey<Biome> key() {
        return this.key;
    }

    public boolean land() {
        return this.land;
    }

    public int surfaceY() {
        return this.surfaceY;
    }

    /** 离岸之后叠在基础高度上的起伏幅度。岩石和沙漠用脊状噪声，平原幅度小。 */
    public int relief() {
        return switch (this) {
            case ROCKYLAND -> 42;
            case METEOR_PLAIN -> 34;
            case DESERT -> 28;
            case BEEFALO_SAVANNA, SAVANNA -> 16;
            case EVERGREEN_FOREST, WALRUS_FOREST, LUNAR_FOREST, BIRCH_FOREST, CEMETERY_FOREST -> 14;
            case ENTRY_FOREST, PIG_VILLAGE_FOREST, MANDRAKE_FOREST -> 11;
            case GRASSLAND, POND_FOREST -> 7;
            case SWAMP, MERM_SWAMP, BEACH -> 3;
            default -> 0;
        };
    }

    public boolean ridged() {
        return this == ROCKYLAND || this == DESERT || this == METEOR_PLAIN || this == SAVANNA || this == BEEFALO_SAVANNA;
    }

    public static IslandBiome byOrdinal(int ordinal) {
        IslandBiome[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return OCEAN;
        }
        return values[ordinal];
    }
}
