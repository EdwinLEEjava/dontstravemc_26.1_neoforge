package com.theconstant.dontstravemc.block.custom;

import com.theconstant.dontstravemc.attachment.ModAttachments;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Don't Starve style flower block.
 * Inherits from Minecraft's FlowerBlock and supports planting on custom turfs.
 */
public class DsFlowerBlock extends FlowerBlock {
    public static final MapCodec<DsFlowerBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(EFFECTS_FIELD.forGetter(FlowerBlock::getSuspiciousEffects), propertiesCodec()).apply(instance, DsFlowerBlock::new)
    );

    public DsFlowerBlock(Holder<MobEffect> holder, float f, BlockBehaviour.Properties properties) {
        super(holder, f, properties);
    }

    public DsFlowerBlock(SuspiciousStewEffects suspiciousStewEffects, BlockBehaviour.Properties properties) {
        super(suspiciousStewEffects, properties);
    }

    @Override
    public MapCodec<? extends FlowerBlock> codec() {
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
