package com.theconstant.dontstravemc.mixin.client;

import com.theconstant.dontstravemc.world.time.ConstantRealmTime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Lightmap.class)
public class LightTextureMixin {

    private static final Identifier THE_CONSTANT_ID =
            Identifier.fromNamespaceAndPath("dontstravemc", "the_constant");

    /**
     * 平滑后的 skyFactor 值，初始 -1 表示未初始化。
     */
    private static float smoothedSkyFactor = -1.0f;

    /**
     * 插值速度：0.02 ≈ 2.5 秒过渡（60fps）。
     */
    private static final float INTERP_SPEED = 0.02f;

    /** 满月夜天光 = 原版 × 倍率 + 下限。 */
    private static final float FULL_MOON_BOOST = 1.35f;
    private static final float FULL_MOON_FLOOR = 0.10f;

    /**
     * 在写入 Lightmap UBO 之前调整 {@link LightmapRenderState#skyFactor}（对应旧版对 skyFactor putFloat 的修改）。
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void dontstravemc$modifySkyFactor(LightmapRenderState renderState, CallbackInfo ci) {
        if (!renderState.needsUpdate) {
            return;
        }

        float originalI = renderState.skyFactor;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        ResourceKey<Level> dim = mc.level.dimension();
        boolean isTheConstant = dim.identifier().equals(THE_CONSTANT_ID);

        if (smoothedSkyFactor < 0.0f) {
            smoothedSkyFactor = originalI;
        }

        float target;
        if (isTheConstant) {
            long tod = ConstantRealmTime.timeOfDay(mc.level.getDefaultClockTime());
            if (tod < ConstantRealmTime.NIGHT_START) {
                target = originalI;
            } else {
                // 普通夜晚：比原版更暗
                float darkNight = Math.max(0.0f, (originalI - 0.24f) / 0.76f) * originalI;
                MoonPhase phase = mc.gameRenderer.getMainCamera().attributeProbe()
                        .getValue(EnvironmentAttributes.MOON_PHASE, 1.0f);
                if (ConstantRealmTime.isNewMoon(phase)) {
                    // 新月：天光完全消失，只剩方块光源
                    target = 0.0f;
                } else if (ConstantRealmTime.isFullMoon(phase)) {
                    // 满月：月光洒满大地，比原版夜晚更亮
                    target = Math.min(1.0f, originalI * FULL_MOON_BOOST + FULL_MOON_FLOOR);
                } else {
                    target = darkNight;
                }
            }
        } else {
            target = originalI;
        }

        smoothedSkyFactor += (target - smoothedSkyFactor) * INTERP_SPEED;
        renderState.skyFactor = Math.max(0.0f, smoothedSkyFactor);
    }
}
