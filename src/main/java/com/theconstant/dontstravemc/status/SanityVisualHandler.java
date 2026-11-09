package com.theconstant.dontstravemc.status;

import com.theconstant.dontstravemc.attachment.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;

public class SanityVisualHandler {
    // 修正：确保这里的 ID 与你的 JSON 资源路径严格一致
    // 对应 assets/dontstravemc/post_effect/sanity_distort.json
    private static final Identifier DISTORT = Identifier.fromNamespaceAndPath("dontstravemc", "sanity_distort");

    private static float smoothedSanity = 1.0f;

    public static void update() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gameRenderer == null) return;

        GameRenderer renderer = client.gameRenderer;

        // 创造 / 旁观者下，彻底关闭理智视觉效果，并重置平滑值，避免残留
        if (client.player.getAbilities().instabuild || client.player.isSpectator()) {
            smoothedSanity = 1.0f;
            SanityShaderData.currentSanity = 1.0f;
            SanityShaderData.gameTime = (float) (System.currentTimeMillis() % 1000000) / 1000.0f;

            renderer.clearPostEffect();
            renderer.effectActive = false;
            return;
        }

        // 1. 获取理智百分比
        float realPercent = ModAttachments.getSanity(client.player).getSanity() / 200.0f;

        // 2. 平滑插值
        smoothedSanity += (realPercent - smoothedSanity) * 0.05f;

        // 3. 更新全局数据供 MixinPostPass 读取
        SanityShaderData.currentSanity = smoothedSanity;
        SanityShaderData.gameTime = (float) (System.currentTimeMillis() % 1000000) / 1000.0f;

        // 4. 控制逻辑优化
        if (smoothedSanity < 0.99f) {
            if (renderer.currentPostEffect() == null || !renderer.currentPostEffect().equals(DISTORT)) {
                renderer.setPostEffect(DISTORT);
            }
            renderer.effectActive = true;
        } else {
            if (DISTORT.equals(renderer.currentPostEffect())) {
                renderer.clearPostEffect();
            }
            renderer.effectActive = false;
        }
    }
}
