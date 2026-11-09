package com.theconstant.dontstravemc.mixin.client;

import com.theconstant.dontstravemc.status.SanityHudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 理智条对齐在饥饿条上方 10px 后，与原版氧气泡仍同 Y；将传入 {@link Gui} 私有方法 {@code extractAirBubbles}
 * 的氧气泡基准 Y（三个 int 参数中为 {@code (10, yLine, xLine)} 的第二个，{@code argsOnly} 下 {@code ordinal = 1}）上移，
 * 使氧气泡整体上移（与 {@link SanityHudOverlay#AIR_BAR_VERTICAL_OFFSET_PX} 一致）。
 * <p>
 * 26.1 起 HUD 由 {@code Gui#extractRenderState} + {@code GuiLayerManager} 分层绘制，旧版 {@code renderPlayerHealth}/{@code renderAirBubbles}
 * 已拆分为 {@code extractHealthLevel}/{@code extractAirLevel} 与 {@code extractAirBubbles}。
 */
@Mixin(Gui.class)
public class GuiMixin {

    @ModifyVariable(method = "extractAirBubbles", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int dontstravemc$offsetAirBarForSanityHud(int yLineAir) {
        if (SanityHudOverlay.shouldShowSanityHudBar(Minecraft.getInstance())) {
            return yLineAir - SanityHudOverlay.AIR_BAR_VERTICAL_OFFSET_PX;
        }
        return yLineAir;
    }
}
