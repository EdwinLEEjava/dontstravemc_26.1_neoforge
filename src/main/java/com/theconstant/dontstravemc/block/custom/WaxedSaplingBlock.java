package com.theconstant.dontstravemc.block.custom;

import com.theconstant.dontstravemc.block.ModBlocks;
import com.theconstant.dontstravemc.block.util.PlantDropHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import com.theconstant.dontstravemc.item.ModItems;

import java.util.List;

/**
 * Waxed Sapling: obtained by digging a sapling with a shovel.
 * When placed, defaults to HARVESTED=true (no twigs, directly enters regrowth timer).
 * All other logic inherited from SaplingBlock.
 */
public class WaxedSaplingBlock extends SaplingBlock {
    public static final MapCodec<WaxedSaplingBlock> CODEC = simpleCodec(WaxedSaplingBlock::new);

    public WaxedSaplingBlock(BlockBehaviour.Properties properties) {
        super(properties);
        // Waxed sapling starts in harvested state (directly enters regrowth)
        this.registerDefaultState(this.stateDefinition.any().setValue(HARVESTED, true));
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    protected Block getSelfDrop() {
        return ModBlocks.SAPLING_WAXED.get();
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return PlantDropHelper.getDrops(params,
                !state.getValue(HARVESTED),
                getSelfDrop(), 1,
                ModItems.TWIGS.get(), 1,
                ModItems.TWIGS.get(), 2);
    }
}
