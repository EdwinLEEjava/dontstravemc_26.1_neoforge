package com.theconstant.dontstravemc.client.fog;

import com.theconstant.dontstravemc.TheConstant;
import com.theconstant.dontstravemc.config.ClientConfig;
import com.theconstant.dontstravemc.render.sky.ConstantSkyPalette;
import com.theconstant.dontstravemc.render.sky.ConstantSkyRenderer;
import com.theconstant.dontstravemc.world.time.ConstantRealmTime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * 永恒领域里让雾色跟随自定义天空的地平线色，远处地形融进天空底部。
 * 水下、岩浆、细雪中的雾色保持原版。
 */
@EventBusSubscriber(modid = TheConstant.MODID, value = Dist.CLIENT)
public final class ConstantSkyFogHandler {

    private static final ConstantSkyPalette.Frame FRAME = new ConstantSkyPalette.Frame();

    private ConstantSkyFogHandler() {
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        if (!ClientConfig.customSky() || !ClientConfig.skyFogColor()) {
            return;
        }
        if (!(Minecraft.getInstance().level instanceof ClientLevel level) || !ConstantRealmTime.isTheConstant(level)) {
            return;
        }
        if (event.getCamera().getFluidInCamera() != FogType.NONE) {
            return;
        }
        ConstantSkyRenderer.computeFrame(level, (float) event.getPartialTick(), FRAME);
        event.setRed(FRAME.fog.x);
        event.setGreen(FRAME.fog.y);
        event.setBlue(FRAME.fog.z);
    }
}
