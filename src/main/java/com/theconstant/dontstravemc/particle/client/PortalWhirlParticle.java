package com.theconstant.dontstravemc.particle.client;

import com.theconstant.dontstravemc.block.custom.ConstantPortalBlock;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * 旋风墨点。不靠速度移动：每 tick 按「当前高度 + 当前游戏时间」直接算出在螺旋线上的位置，
 * 所以同一条线上的墨点始终排成一条连续、整体旋转的螺旋。
 * <p>
 * 生成参数：(x, y, z) = 整组中心与地面高度，xd = 整组半径，yd = 所在螺旋线的相位，zd = 起始高度。
 */
public class PortalWhirlParticle extends SingleQuadParticle {
    private static final float INK_R = 0x21 / 255.0F;
    private static final float INK_G = 0x0A / 255.0F;
    private static final float INK_B = 0x0F / 255.0F;
    /**
     * 粒子方片大小随传送门变大：quadSize = 整组半径 × SIZE_PER_RADIUS，限制在 [MIN_SIZE, MAX_SIZE]，整个生命周期不变。
     * quadSize 是方片的半边长（格）。墨点看起来多大由贴图决定，见 {@link #updatePosition}。
     */
    private static final float SIZE_PER_RADIUS = 0.1F;
    private static final float MIN_SIZE = 0.05F;
    private static final float MAX_SIZE = 0.35F;
    private static final int SPRITE_COUNT = 6; // 和 portal_whirl.json 的贴图张数一致
    /**
     * 每颗墨点换贴图的高度随机错开的幅度（占旋风总高度的比例）。
     * 0.08 表示每颗墨点的阶段在 ±8% 高度内随机提前或推迟，同一高度上会混着相邻两三档大小。
     */
    private static final double STAGE_JITTER = 0.08;

    private final SpriteSet sprites;
    private final double centerX;
    private final double centerZ;
    private final double baseY;
    private final double groupRadius;
    private final double strandPhase;
    private final double maxHeight;
    private double height;
    /** 这颗墨点自己的阶段偏移，出生时随机定下，之后不变。 */
    private final double stageOffset;

    protected PortalWhirlParticle(ClientLevel level, double x, double y, double z,
                                  double groupRadius, double strandPhase, double startHeight, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());
        this.sprites = sprites;
        this.centerX = x;
        this.centerZ = z;
        this.baseY = y;
        this.groupRadius = groupRadius;
        this.strandPhase = strandPhase;
        this.maxHeight = ConstantPortalBlock.whirlHeight(groupRadius);
        this.height = startHeight;
        this.stageOffset = (this.random.nextDouble() * 2.0 - 1.0) * STAGE_JITTER;
        this.quadSize = Mth.clamp((float) groupRadius * SIZE_PER_RADIUS, MIN_SIZE, MAX_SIZE);
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.lifetime = Mth.ceil((maxHeight - startHeight) / ConstantPortalBlock.WHIRL_RISE_PER_TICK) + 1;
        this.setColor(INK_R, INK_G, INK_B);
        updatePosition();
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    private void updatePosition() {
        double h = height / maxHeight;
        double r = groupRadius * (0.5 + 0.55 * h);
        double angle = ConstantPortalBlock.whirlAngle(height, level.getGameTime(), strandPhase);
        setPos(centerX + r * Math.cos(angle), baseY + height, centerZ + r * Math.sin(angle));
        // 列表下标 = generic 编号：portal_whirl.json 按 generic_0 ~ generic_5 排列。
        // 底部取最后一张 generic_5（最大阶段），越往上编号越小，顶端是 generic_0（1 像素）。
        double stage = h + stageOffset;
        setSprite(sprites.get(Mth.clamp(Mth.floor((1.0 - stage) * (SPRITE_COUNT - 1) + 0.5), 0, SPRITE_COUNT - 1), SPRITE_COUNT - 1));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.height += ConstantPortalBlock.WHIRL_RISE_PER_TICK;
        if (this.age++ >= this.lifetime || this.height >= this.maxHeight) {
            this.remove();
            return;
        }
        updatePosition();
    }

    @Override
    protected Layer getLayer() {
        return Layer.OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xd, double yd, double zd, RandomSource random) {
            return new PortalWhirlParticle(level, x, y, z, xd, yd, zd, sprites);
        }
    }
}
