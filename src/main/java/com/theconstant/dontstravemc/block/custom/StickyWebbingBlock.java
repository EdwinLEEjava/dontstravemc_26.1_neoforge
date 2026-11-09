package com.theconstant.dontstravemc.block.custom;

import com.theconstant.dontstravemc.entity.monster.MonsterSpiderEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StickyWebbingBlock extends MultifaceBlock {
    public static final MapCodec<StickyWebbingBlock> CODEC = simpleCodec(StickyWebbingBlock::new);

    public StickyWebbingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends MultifaceBlock> codec() {
        return CODEC;
    }

    @Override
    public float getDestroyProgress(BlockState state, net.minecraft.world.entity.player.Player player, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        float baseSpeed = super.getDestroyProgress(state, player, level, pos);
        if (player.getMainHandItem().is(net.minecraft.tags.ItemTags.SWORDS)) {
            return Math.min(1.0f, baseSpeed * 15.0F);
        }
        return baseSpeed;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier, boolean bl) {
        if (entity instanceof MonsterSpiderEntity
                || entity.getType() == EntityType.SPIDER
                || entity.getType() == EntityType.CAVE_SPIDER) {
            return;
        }

        VoxelShape blockShapeInWorld = state.getShape(level, pos).move(pos.getX(), pos.getY(), pos.getZ());
        AABB entityBox = entity.getBoundingBox();

        if (Shapes.joinIsNotEmpty(blockShapeInWorld, Shapes.create(entityBox), BooleanOp.AND)) {
            entity.makeStuckInBlock(state, new Vec3(0.6D, 0.05D, 0.6D));
        }
    }
}
