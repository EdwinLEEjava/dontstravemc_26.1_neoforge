package com.theconstant.dontstravemc.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class ProximityLanternBlock extends LanternBlock {
    public static final MapCodec<ProximityLanternBlock> CODEC = simpleCodec(ProximityLanternBlock::new);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public ProximityLanternBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HANGING, false)
                .setValue(WATERLOGGED, false)
                .setValue(LIT, true));
    }

    @Override
    public MapCodec<? extends LanternBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(LIT, true);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel serverLevel) {
            ensureTickChain(serverLevel, pos);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        syncLitToProximity(level, pos);
        level.scheduleTick(pos, this, 10);
    }

    /**
     * Normal placement / onPlace: sync {@link #LIT} and start the scheduled tick chain.
     */
    public static void ensureTickChain(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof ProximityLanternBlock)) {
            return;
        }
        syncLitToProximity(level, pos);
        level.scheduleTick(pos, state.getBlock(), 10);
    }

    /**
     * World generation only: {@link net.minecraft.server.level.WorldGenRegion#setBlock} does not run {@link Block#onPlace}.
     * Queue the tick on this {@link WorldGenLevel}'s block tick access so it merges into the generating chunk; scheduling only
     * on {@link ServerLevel} here often never executes the block tick. Do not call {@link ServerLevel#setBlock} or
     * {@link #syncLitToProximity} during worldgen (deadlock risk).
     */
    public static void scheduleTickAfterWorldGen(WorldGenLevel level, BlockPos pos, BlockState placedState) {
        if (!(placedState.getBlock() instanceof ProximityLanternBlock block)) {
            return;
        }
        level.scheduleTick(pos, block, 10);
    }

    /**
     * Lit when no player within range; matches structure placement (NBT may bake wrong {@link #LIT}).
     */
    private static void syncLitToProximity(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(LIT)) {
            return;
        }
        boolean lit = shouldBeLit(level, pos);
        if (state.getValue(LIT) != lit) {
            level.setBlock(pos, state.setValue(LIT, lit), 3);
        }
    }

    private static boolean shouldBeLit(ServerLevel level, BlockPos pos) {
        return level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 11.0D, false) == null;
    }
}
