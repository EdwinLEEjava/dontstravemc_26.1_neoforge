package com.theconstant.dontstravemc.world.gen.structure;

import com.theconstant.dontstravemc.TheConstant;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 结构类型与结构部件的注册。和 {@code IslandWorldgen} 一样自带注册表，在 {@code TheConstant} 构造器里挂到模组总线。
 */
public final class ModStructures {

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, TheConstant.MODID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, TheConstant.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<ShadowRiftStructure>> SHADOW_RIFT =
            STRUCTURE_TYPES.register("shadow_rift", () -> () -> ShadowRiftStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> SHADOW_RIFT_PIECE =
            STRUCTURE_PIECES.register("shadow_rift", () -> (StructurePieceType.ContextlessType) ShadowRiftPiece::new);

    /** 会被替换成阴影方块的方块。默认只放原版自然地形，其他模组的方块要靠数据包加进来。 */
    public static final TagKey<Block> SHADOW_RIFT_REPLACEABLE = blockTag("shadow_rift_replaceable");
    /** 会被清成空气的非完整方块（原版小植物、雪层等）。 */
    public static final TagKey<Block> SHADOW_RIFT_CLEARABLE = blockTag("shadow_rift_clearable");
    /** 裂隙不会出现在这些群系里。 */
    public static final TagKey<Biome> SHADOW_RIFT_EXCLUDED_BIOMES =
            TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(TheConstant.MODID, "shadow_rift_excluded_biomes"));

    private ModStructures() {
    }

    private static TagKey<Block> blockTag(String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TheConstant.MODID, path));
    }

    public static void register() {
        TheConstant.LOGGER.info("Structures registered for {}", TheConstant.MODID);
    }
}
