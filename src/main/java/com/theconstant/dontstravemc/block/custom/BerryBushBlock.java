package com.theconstant.dontstravemc.block.custom;

import com.theconstant.dontstravemc.block.ModBlocks;
import com.theconstant.dontstravemc.block.util.PlantDropHelper;
import com.theconstant.dontstravemc.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.RandomSource;

import java.util.List;

/**
 * Natural berry bush block - variant 1 (Don't Starve style).
 * - Naturally generated with ripe=true (full fruit)
 * - Right-click to harvest: drops 1 berry, becomes ripe=false
 * - Random Tick regrowth: ~3-5 days average, regrows to ripe=true
 * - Shovel: drops waxed berry bush + berries if ripe
 * - Non-shovel break: drops nothing
 */
public class BerryBushBlock extends VegetationBlock implements BonemealableBlock {
    public static final MapCodec<BerryBushBlock> CODEC = simpleCodec(BerryBushBlock::new);

    public static final BooleanProperty RIPE = BooleanProperty.create("ripe");

    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0);

    public BerryBushBlock(BlockBehaviour.Properties properties) {
        super(properties.randomTicks());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(RIPE, true));
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RIPE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * Returns the waxed (transplanted) version of this bush to drop when dug with a shovel.
     * Subclasses override this to return their own variant.
     */
    protected Block getWaxedDrop() {
        return ModBlocks.BERRY_BUSH_WAXED.get();
    }

    // --- Harvest (right-click) ---

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (state.getValue(RIPE)) {
                Block.popResource(level, pos, new ItemStack(ModItems.BERRIES.get(), 1));
                level.setBlock(pos, state.setValue(RIPE, false), 3);
                return InteractionResult.SUCCESS;
            }
        }
        return state.getValue(RIPE) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    // --- Random tick regrowth ---

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(RIPE) && random.nextInt(70) == 0) {
            level.setBlock(pos, state.setValue(RIPE, true), 3);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(RIPE) || super.isRandomlyTicking(state);
    }

    // --- Bonemeal ---

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !state.getValue(RIPE);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(RIPE, true), 3);
    }

    // --- Drops ---

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return PlantDropHelper.getDrops(params,
                state.getValue(RIPE),
                getWaxedDrop(), 1,
                ModItems.BERRIES.get(), 1,
                null, 0);
    }
}
