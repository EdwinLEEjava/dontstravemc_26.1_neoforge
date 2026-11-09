package com.theconstant.dontstravemc.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 客户端设置（每个玩家本地，{@code config/dontstravemc-client.toml}）。
 * <p>
 * 在「模组列表 → 永恒领域 → 配置」或「选项 → 永恒领域设置」里编辑，修改即时生效。
 * 每一项的界面文字在语言文件里：{@code dontstravemc.configuration.<键名>} 与 {@code .tooltip}。
 */
public final class ClientConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static {
        BUILDER.push("sky");
    }

    public static final ModConfigSpec.BooleanValue CUSTOM_SKY = BUILDER
            .comment("Render the hand-painted sky in The Constant. When off, the vanilla sky, fog colour and clouds are used.")
            .define("customSky", true);

    public static final ModConfigSpec.DoubleValue PAPER_GRAIN = BUILDER
            .comment("Strength of the paper grain texture on the sky.")
            .defineInRange("paperGrain", 0.10, 0.0, 0.30);

    public static final ModConfigSpec.DoubleValue WATERCOLOR_STRENGTH = BUILDER
            .comment("Multiplier for the watercolour wash and brush strokes on the sky. 0 gives a clean gradient.")
            .defineInRange("watercolorStrength", 1.0, 0.0, 2.0);

    public static final ModConfigSpec.BooleanValue SKY_FOG_COLOR = BUILDER
            .comment("Tint distant fog with the sky's horizon colour so terrain blends into the sky.")
            .define("skyFogColor", true);

    static {
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ClientConfig() {
    }

    /** 配置文件加载前（极早期）返回默认值，避免读取未加载的配置报错。 */
    public static boolean customSky() {
        return !SPEC.isLoaded() || CUSTOM_SKY.getAsBoolean();
    }

    public static float paperGrain() {
        return SPEC.isLoaded() ? PAPER_GRAIN.get().floatValue() : 0.10F;
    }

    public static float watercolorStrength() {
        return SPEC.isLoaded() ? WATERCOLOR_STRENGTH.get().floatValue() : 1.0F;
    }

    public static boolean skyFogColor() {
        return !SPEC.isLoaded() || SKY_FOG_COLOR.getAsBoolean();
    }
}
