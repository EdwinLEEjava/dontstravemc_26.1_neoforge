package com.theconstant.dontstravemc.block.custom;

import com.theconstant.dontstravemc.block.ModBlocks;
import com.theconstant.dontstravemc.block.blockentity.ModBlockEntities;
import com.theconstant.dontstravemc.block.blockentity.custom.ConstantPortalBlockEntity;
import com.theconstant.dontstravemc.particle.ModParticles;
import com.theconstant.dontstravemc.world.ConstantTeleporter;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class ConstantPortalBlock extends BaseEntityBlock implements Portal {
    public static final MapCodec<ConstantPortalBlock> CODEC = simpleCodec(ConstantPortalBlock::new);
    private static final int MAX_GROUP_BLOCKS = 64;
    /** 和渲染器画出的平面一致：贴地 1 像素厚。渲染器的 SURFACE_Y 必须等于这里的高度。 */
    public static final float SURFACE_HEIGHT = 1.0F / 16.0F;
    public static final double WHIRL_TWIST = 2.4;           // 每格高度转过的弧度
    public static final double WHIRL_TICKS_PER_TURN = 30.0; // 1.5 秒一圈
    public static final double WHIRL_RISE_PER_TICK = 0.06;  // 墨点沿螺旋线上升的速度（格/tick）
    public static final int WHIRL_STRANDS = 3;
    private static final int WHIRL_SPAWNS_PER_STRAND = 1;   // 每 tick 每条线补几颗，越多线越连续

    /** 旋风高度（格）。 */
    public static double whirlHeight(double radius) {
        return 2.0 + radius * 2.0;
    }

    /** 螺旋线在某高度、某时刻的角度。 */
    public static double whirlAngle(double height, double gameTime, double strandPhase) {
        return height * WHIRL_TWIST - gameTime * Mth.TWO_PI / WHIRL_TICKS_PER_TURN + strandPhase;
    }
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);

    public ConstantPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConstantPortalBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** 站进传送门的判定也只用这块薄板，实体脚底碰到它才算进入。 */
    @Override
    protected VoxelShape getEntityInsideCollisionShape(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.CONSTANT_PORTAL.get(),
                (tickLevel, pos, tickState, blockEntity) -> spawnWhirl(tickLevel, pos));
    }

    /** 每 tick 在每条螺旋线的根部补几颗墨点，墨点自己沿线绕圈上升。整组只由最小角的方块生成。 */
    private static void spawnWhirl(Level level, BlockPos pos) {
        GroupBounds bounds = findGroup(level, pos);
        if (pos.getX() != bounds.minX() || pos.getZ() != bounds.minZ()) {
            return;
        }
        double baseY = pos.getY() + SURFACE_HEIGHT;
        for (int strand = 0; strand < WHIRL_STRANDS; strand++) {
            double phase = Mth.TWO_PI * strand / WHIRL_STRANDS;
            for (int i = 0; i < WHIRL_SPAWNS_PER_STRAND; i++) {
                double startHeight = WHIRL_RISE_PER_TICK * i / WHIRL_SPAWNS_PER_STRAND;
                level.addParticle(ModParticles.PORTAL_WHIRL.get(), bounds.centerX(), baseY, bounds.centerZ(),
                        bounds.radius(), phase, startHeight);
            }
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier, boolean touching) {
        if (level.dimension() != Level.OVERWORLD || (entity instanceof Player player && player.isSpectator())) {
            return;
        }
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public @Nullable TeleportTransition getPortalDestination(ServerLevel currentLevel, Entity entity, BlockPos portalEntryPos) {
        return ConstantTeleporter.toTheConstant(currentLevel, entity);
    }

    /**
     * 同一高度、上下左右相邻的传送门算一组。斜角不算。超过 {@link #MAX_GROUP_BLOCKS} 的部分不计入包围盒。
     */
    public static GroupBounds findGroup(Level level, BlockPos origin) {
        int y = origin.getY();
        int minX = origin.getX();
        int maxX = origin.getX();
        int minZ = origin.getZ();
        int maxZ = origin.getZ();
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        seen.add(origin);
        queue.add(origin);

        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
            if (seen.size() >= MAX_GROUP_BLOCKS) {
                continue;
            }
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = pos.relative(direction);
                if (next.getY() != y || seen.contains(next)) {
                    continue;
                }
                if (!level.getBlockState(next).is(ModBlocks.CONSTANT_PORTAL.get())) {
                    continue;
                }
                seen.add(next);
                queue.add(next);
            }
        }
        return new GroupBounds(minX, maxX, minZ, maxZ);
    }

    public record GroupBounds(int minX, int maxX, int minZ, int maxZ) {
        public float centerX() {
            return (minX + maxX + 1) * 0.5F;
        }

        public float centerZ() {
            return (minZ + maxZ + 1) * 0.5F;
        }

        public float radius() {
            return Math.max(maxX - minX + 1, maxZ - minZ + 1) * 0.5F;
        }
    }
}
