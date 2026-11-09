package com.theconstant.dontstravemc.block.blockentity;

import com.theconstant.dontstravemc.TheConstant;

import com.theconstant.dontstravemc.block.ModBlocks;
import com.theconstant.dontstravemc.block.blockentity.custom.ConstantPortalBlockEntity;
import com.theconstant.dontstravemc.block.blockentity.custom.UrnBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Set;

public class ModBlockEntities {

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UrnBlockEntity>> URN_BLOCK_ENTITY =
            TheConstant.BLOCK_ENTITY_TYPES.register("urn", () ->
                    new BlockEntityType<>(UrnBlockEntity::new, Set.of(ModBlocks.URN.get()))
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConstantPortalBlockEntity>> CONSTANT_PORTAL =
            TheConstant.BLOCK_ENTITY_TYPES.register("constant_portal", () ->
                    new BlockEntityType<>(ConstantPortalBlockEntity::new, Set.of(ModBlocks.CONSTANT_PORTAL.get()))
            );

    public static void registerBlockEntities() {
        TheConstant.LOGGER.info("Registering Block Entities for " + TheConstant.MODID);
    }
}
