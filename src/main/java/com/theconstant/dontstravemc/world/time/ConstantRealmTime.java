package com.theconstant.dontstravemc.world.time;

import com.theconstant.dontstravemc.world.ConstantDimensions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;

/**
 * 永恒领域 {@code the_constant}：白天 [0,12000)、黄昏 [12000,14000)、夜晚 [14000,24000)。<br>
 * 其他维度：无「黄昏」带；夜晚为 tick ≥12000（与原版常用 dayTime 分界一致）。
 */
public final class ConstantRealmTime {

    public static final int DAY_END = 12000;
    public static final int NIGHT_START = 14000;

    public enum Phase {
        DAY,
        DUSK,
        NIGHT;

        public static Phase atTheConstant(long timeOfDay) {
            if (timeOfDay < DAY_END) {
                return DAY;
            }
            if (timeOfDay < NIGHT_START) {
                return DUSK;
            }
            return NIGHT;
        }
    }

    private ConstantRealmTime() {
    }

    public static long timeOfDay(long dayTime) {
        return Math.floorMod(dayTime, 24000L);
    }

    public static boolean isTheConstant(Level level) {
        return level != null && level.dimension() == ConstantDimensions.THE_CONSTANT;
    }

    /**
     * 永恒领域：真夜（≥14000）。其他维度：tick ≥12000 视为夜晚半段。
     */
    public static boolean isNight(Level level, long dayTime) {
        long tod = timeOfDay(dayTime);
        if (isTheConstant(level)) {
            return tod >= NIGHT_START;
        }
        return tod >= DAY_END;
    }

    /**
     * 白天：tick &lt; 12000（各维度相同）。
     */
    public static boolean isDay(Level level, long dayTime) {
        return timeOfDay(dayTime) < DAY_END;
    }

    /**
     * 仅永恒领域存在黄昏带；其他维度恒为 false。
     */
    public static boolean isDusk(Level level, long dayTime) {
        if (!isTheConstant(level)) {
            return false;
        }
        long tod = timeOfDay(dayTime);
        return tod >= DAY_END && tod < NIGHT_START;
    }

    public static Phase phaseTheConstant(long dayTime) {
        return Phase.atTheConstant(timeOfDay(dayTime));
    }

    /**
     * 月光强度：满月 1、凸月 0.75、弦月 0.5、蛾眉月 0.25、新月 0。
     * 月相每 24000 tick 切换一次（原版 {@code visual/moon_phase} 环境属性）。
     */
    public static float moonBrightness(MoonPhase phase) {
        return Math.abs(phase.index() - 4) / 4.0F;
    }

    public static boolean isNewMoon(MoonPhase phase) {
        return phase == MoonPhase.NEW_MOON;
    }

    public static boolean isFullMoon(MoonPhase phase) {
        return phase == MoonPhase.FULL_MOON;
    }
}
