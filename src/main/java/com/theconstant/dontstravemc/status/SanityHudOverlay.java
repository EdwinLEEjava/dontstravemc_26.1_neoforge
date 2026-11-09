package com.theconstant.dontstravemc.status;

import com.theconstant.dontstravemc.sound.ModSoundEvents;
import com.theconstant.dontstravemc.attachment.ModAttachments;
import com.theconstant.dontstravemc.status.sanity.SanityComponents;
import net.minecraft.util.ARGB;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;

public class SanityHudOverlay {
    /** 与原版盔甲相对生命、各行 HUD 之间一致：间隔 10px（见 {@code Gui.renderArmor} / {@code Gui.renderPlayerHealth}） */
    public static final int HUD_ROW_STRIDE_PX = 10;

    /**
     * 显示理智条时，将原版氧气泡再上移一行，避免与理智条（对齐在饥饿条上方 {@link #HUD_ROW_STRIDE_PX}）重合。
     */
    public static final int AIR_BAR_VERTICAL_OFFSET_PX = HUD_ROW_STRIDE_PX;

    public static final SanityHudOverlay INSTANCE = new SanityHudOverlay();

    private static final Identifier SANITY_TEXTURE = Identifier.fromNamespaceAndPath("dontstravemc", "textures/gui/sanity.png");
    private static final Identifier SANITY_OVERLAY = Identifier.fromNamespaceAndPath("dontstravemc", "textures/misc/sanity_overlay.png");

    private static float overlayAlpha = 0.0f;
    private static float targetAlpha = 0.0f;
    private static long lastAlphaUpdateTime = -1;
    private static boolean soundPlayed = false;

    private SanityHudOverlay() {
    }

    public void onHudRender(GuiGraphicsExtractor drawContext, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || client.options.hideGui) return;

        if (client.player.getAbilities().instabuild) return;
        if (client.player.isSpectator()) return;

        SanityComponents sanityComp = ModAttachments.getSanity(client.player);
        float currentSanity = sanityComp.getSanity();
        float rate = sanityComp.getEnvironmentalRate();

        SanitySoundManager.tick(client.player, currentSanity);

        int screenWidth = drawContext.guiWidth();
        int screenHeight = drawContext.guiHeight();
        int baseX = screenWidth / 2 + 10;
        int baseY = screenHeight - 39 - HUD_ROW_STRIDE_PX;

        long currentTime = net.minecraft.util.Util.getMillis();
        if (lastAlphaUpdateTime == -1) {
            lastAlphaUpdateTime = currentTime;
        }
        float deltaTime = (currentTime - lastAlphaUpdateTime) / 1000.0f;
        lastAlphaUpdateTime = currentTime;

        if (currentSanity < 30.0f) {
            targetAlpha = 0.5f;
            if (!soundPlayed) {
                client.getSoundManager().play(SimpleSoundInstance.forUI(ModSoundEvents.MUSIC_DUSK_GONECRAZIER, 1.0f));
                soundPlayed = true;
            }
        } else if (currentSanity > 35.0f) {
            targetAlpha = 0.0f;
            soundPlayed = false;
        }

        if (overlayAlpha < targetAlpha) {
            overlayAlpha = Math.min(targetAlpha, overlayAlpha + deltaTime / 2.0f);
        } else if (overlayAlpha > targetAlpha) {
            overlayAlpha = Math.max(targetAlpha, overlayAlpha - deltaTime / 2.0f);
        }

        if (overlayAlpha > 0.0f) {
            renderTendrils(drawContext, screenWidth, screenHeight, overlayAlpha);
        }

        for (int i = 0; i < 10; i++) {
            float thresholdMin = 180 - (i * 20);
            int uOffset = (currentSanity > thresholdMin + 10) ? 0 : (currentSanity > thresholdMin ? 9 : 18);

            drawContext.blit(
                    RenderPipelines.GUI_TEXTURED,
                    SANITY_TEXTURE,
                    baseX + (i * 8), baseY,
                    (float) uOffset, 0,
                    9, 9,
                    9, 9,
                    256, 256
            );
        }

        renderArrows(drawContext, rate, baseX + 84, baseY, deltaTracker.getGameTimeDeltaTicks());
    }

    private void renderTendrils(GuiGraphicsExtractor context, int w, int h, float alpha) {
        int color = ARGB.colorFromFloat(alpha, 1.0f, 1.0f, 1.0f);

        context.blit(
                RenderPipelines.GUI_TEXTURED,
                SANITY_OVERLAY,
                0, 0,
                0.0f, 0.0f,
                w, h,
                1, 1,
                1, 1,
                color
        );
    }

    private void renderArrows(GuiGraphicsExtractor drawContext, float rate, int x, int y, float partialTicks) {
        float absRate = Math.abs(rate);
        boolean isRising = rate > 0;

        int size = 0;
        if (isRising) {
            if (absRate > 0.2f) size = 3;
            else if (absRate > 0.1f) size = 2;
            else if (absRate > 0.01f) size = 1;
        } else {
            if (absRate > 0.3f) size = 3;
            else if (absRate > 0.1f) size = 2;
            else if (absRate > 0.02f) size = 1;
        }
        if (size == 0) return;

        int u = (size == 1) ? (isRising ? 29 : 28) : (size == 2 ? 35 : 43);
        int v = isRising ? (size == 3 ? 10 : 11) : (size == 1 ? 2 : (size == 2 ? 1 : 0));
        int w = (size == 1) ? (isRising ? 4 : 5) : (size == 2 ? 7 : 9);
        int h = (size == 1) ? 6 : (size == 2 ? 8 : 10);

        float clientTicks = Minecraft.getInstance().level.getGameTime() + partialTicks;
        float speed = 0.6f;
        int spacing = 20;

        float scrollOffset = (clientTicks * speed) % spacing;

        drawContext.enableScissor(x, y, x + 12, y + 10);

        for (int i = -1; i <= 1; i++) {
            float drawY;
            if (isRising) {
                drawY = y + (spacing - scrollOffset) + (i * spacing);
            } else {
                drawY = y + (scrollOffset - spacing) + (i * spacing);
            }

            drawContext.blit(
                    RenderPipelines.GUI_TEXTURED,
                    SANITY_TEXTURE,
                    x, (int) drawY,
                    (float) u, (float) v,
                    w, h,
                    256, 256
            );
        }

        drawContext.disableScissor();
    }

    /** 与 {@link #onHudRender} 中是否绘制理智条的判定一致，用于同步移动原版氧气条位置 */
    public static boolean shouldShowSanityHudBar(Minecraft client) {
        if (client == null || client.player == null || client.options.hideGui) {
            return false;
        }
        if (client.player.getAbilities().instabuild || client.player.isSpectator()) {
            return false;
        }
        return true;
    }
}
