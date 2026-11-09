package com.theconstant.dontstravemc.block.custom;

import com.theconstant.dontstravemc.block.component.BlockSanityAura;
import com.theconstant.dontstravemc.block.component.BlockSanityAuraProvider;
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
 * Evil flower block - variant of Don't Starve flowers.
 * Inherits from Minecraft's FlowerBlock and supports planting on custom turfs.
 */
public class EvilFlowerBlock extends FlowerBlock implements BlockSanityAuraProvider {
    public static final MapCodec<EvilFlowerBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(EFFECTS_FIELD.forGetter(FlowerBlock::getSuspiciousEffects), propertiesCodec()).apply(instance, EvilFlowerBlock::new)
    );
    private static final BlockSanityAura SANITY_AURA = new BlockSanityAura(-25.0 / 60.0);

    public EvilFlowerBlock(Holder<MobEffect> holder, float f, BlockBehaviour.Properties properties) {
        super(holder, f, properties);
    }

    public EvilFlowerBlock(SuspiciousStewEffects suspiciousStewEffects, BlockBehaviour.Properties properties) {
        super(suspiciousStewEffects, properties);
    }

    @Override
    public MapCodec<? extends FlowerBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ModAttachments.getSanity(serverPlayer).addSanity(-5.0f);
            ModAttachments.syncSanity(serverPlayer);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public BlockSanityAura getBlockSanityAura() {
        return SANITY_AURA;
    }
}
