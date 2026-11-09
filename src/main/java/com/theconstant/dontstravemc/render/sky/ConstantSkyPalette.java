package com.theconstant.dontstravemc.render.sky;

import com.theconstant.dontstravemc.world.time.ConstantRealmTime;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/**
 * 永恒领域天空的分时段配色。
 * <p>
 * 关键帧按一天内的 tick（0–24000）排列，相邻关键帧之间用 smoothstep 插值。
 * 时段划分与 {@link ConstantRealmTime} 一致：白天 [0,12000)、黄昏 [12000,14000)、夜晚 [14000,24000)。
 * 雾色取地平线色略提亮，保证远处地形与天空底部无缝衔接。
 */
public final class ConstantSkyPalette {

    /** 一组天空颜色：天顶、地平线、地平线以下。 */
    private record Colors(Vector3f zenith, Vector3f horizon, Vector3f ground, float blotch) {
    }

    private record Key(float tick, Colors colors) {
    }

    // 白天：灰青天顶 + 羊皮纸地平线（饥荒式阴郁白天）
    private static final Colors DAY = new Colors(
            rgb(0.34F, 0.40F, 0.43F), rgb(0.72F, 0.68F, 0.56F), rgb(0.42F, 0.39F, 0.32F), 0.20F);
    // 黄昏：紫灰天顶 + 褐橙地平线（压低粉色，偏土色）
    private static final Colors DUSK = new Colors(
            rgb(0.22F, 0.20F, 0.30F), rgb(0.66F, 0.46F, 0.36F), rgb(0.26F, 0.20F, 0.20F), 0.20F);
    // 夜晚：靛蓝天顶 + 偏亮的紫色地平线（参考图一：夜空不是一片死黑）
    private static final Colors NIGHT = new Colors(
            rgb(0.11F, 0.14F, 0.23F), rgb(0.34F, 0.30F, 0.45F), rgb(0.10F, 0.11F, 0.15F), 0.22F);
    // 黎明：介于夜与昼之间的冷粉
    private static final Colors DAWN = new Colors(
            rgb(0.28F, 0.31F, 0.40F), rgb(0.68F, 0.58F, 0.52F), rgb(0.32F, 0.30F, 0.30F), 0.20F);

    private static final Key[] KEYS = {
            new Key(0F, DAWN),
            new Key(1500F, DAY),
            new Key(11000F, DAY),
            new Key(12500F, DUSK),
            new Key(14000F, NIGHT),
            new Key(22500F, NIGHT),
            new Key(24000F, DAWN),
    };

    /** 雨天：向此灰褐色靠拢（参考图二的风暴色调）。 */
    private static final Vector3f STORM_TINT = rgb(0.40F, 0.38F, 0.33F);

    /** 地平线色到雾色的提亮系数，需与着色器里地平线辉光强度一致。 */
    public static final float HORIZON_GLOW = 0.10F;

    public static final float PAPER_GRAIN = 0.10F;

    /** 当前帧的天空参数（可变对象，复用以避免每帧分配）。 */
    public static final class Frame {
        public final Vector3f zenith = new Vector3f();
        public final Vector3f horizon = new Vector3f();
        public final Vector3f ground = new Vector3f();
        public final Vector3f fog = new Vector3f();
        public float blotch;
        public float grain;
        /** 0 = 白天，1 = 深夜（{@link #nightFactor}）。 */
        public float night;
    }

    /** 新月夜：天空亮度乘数（接近漆黑，但地平线仍能分辨地形轮廓）。 */
    private static final float NEW_MOON_DARKEN = 0.30F;
    /** 满月夜：天空亮度乘数。 */
    private static final float FULL_MOON_BRIGHTEN = 1.25F;
    /** 满月时天空靠拢的月光蓝。 */
    private static final Vector3f MOONLIGHT_ZENITH = rgb(0.10F, 0.18F, 0.36F);
    private static final Vector3f MOONLIGHT_HORIZON = rgb(0.26F, 0.36F, 0.58F);

    private ConstantSkyPalette() {
    }

    /** 0 = 白天，1 = 深夜；黄昏 12800–14200 淡入，黎明 22400–23600 淡出。 */
    public static float nightFactor(float timeOfDay) {
        float in = Mth.clamp((timeOfDay - 12800.0F) / 1400.0F, 0.0F, 1.0F);
        float out = Mth.clamp((timeOfDay - 22400.0F) / 1200.0F, 0.0F, 1.0F);
        float f = in * (1.0F - out);
        return f * f * (3.0F - 2.0F * f);
    }

    /**
     * @param timeOfDay 0–24000 内的时刻（可带小数）
     * @param rain      0–1 的降雨强度
     * @param moon      0–1 的月光强度（新月 0，满月 1），见 ConstantRealmTime.moonBrightness
     */
    public static void compute(float timeOfDay, float rain, float moon, Frame out) {
        float t = Mth.positiveModulo(timeOfDay, 24000F);
        int i = 0;
        while (i < KEYS.length - 2 && t >= KEYS[i + 1].tick) {
            i++;
        }
        Key a = KEYS[i];
        Key b = KEYS[i + 1];
        float span = b.tick - a.tick;
        float f = span <= 0F ? 0F : Mth.clamp((t - a.tick) / span, 0F, 1F);
        f = f * f * (3F - 2F * f);

        a.colors.zenith.lerp(b.colors.zenith, f, out.zenith);
        a.colors.horizon.lerp(b.colors.horizon, f, out.horizon);
        a.colors.ground.lerp(b.colors.ground, f, out.ground);
        out.blotch = Mth.lerp(f, a.colors.blotch, b.colors.blotch);
        out.grain = PAPER_GRAIN;

        // 月相：只在夜里起作用。新月整片天压到接近漆黑；满月整体提亮并偏向月光的蓝色
        float night = nightFactor(t);
        out.night = night;
        if (night > 0F) {
            float bright = Mth.lerp(moon, NEW_MOON_DARKEN, FULL_MOON_BRIGHTEN);
            float mul = Mth.lerp(night, 1F, bright);
            out.zenith.mul(mul);
            out.horizon.mul(mul);
            out.ground.mul(mul);
            float blue = night * moon * 0.30F;
            out.zenith.lerp(MOONLIGHT_ZENITH, blue);
            out.horizon.lerp(MOONLIGHT_HORIZON, blue);
        }

        if (rain > 0F) {
            // 雨天地平线也要压暗，否则远处一圈发亮、与暗下来的地面脱节
            float r = Mth.clamp(rain, 0F, 1F) * 0.85F;
            stormify(out.zenith, r, 0.40F);
            stormify(out.horizon, r, 0.50F);
            stormify(out.ground, r, 0.45F);
            out.blotch += 0.08F * r;
        }

        out.horizon.mul(1F + HORIZON_GLOW, out.fog);
        out.fog.min(new Vector3f(1F));
    }

    private static void stormify(Vector3f color, float amount, float brightness) {
        float luma = color.x * 0.299F + color.y * 0.587F + color.z * 0.114F;
        Vector3f target = new Vector3f(STORM_TINT).mul(Math.max(luma, 0.08F) / 0.38F * brightness);
        color.lerp(target, amount);
    }

    private static Vector3f rgb(float r, float g, float b) {
        return new Vector3f(r, g, b);
    }
}
