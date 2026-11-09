package com.theconstant.dontstravemc.block.custom;

import com.theconstant.dontstravemc.attachment.ModAttachments;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class DsFlowerBedBlock extends FlowerBedBlock {
    public static final MapCodec<FlowerBedBlock> CODEC = simpleCodec(DsFlowerBedBlock::new);

    public DsFlowerBedBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<FlowerBedBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ModAttachments.getSanity(serverPlayer).addSanity(5.0f);
            ModAttachments.syncSanity(serverPlayer);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
