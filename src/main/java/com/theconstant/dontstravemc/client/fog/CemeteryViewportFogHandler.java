package com.theconstant.dontstravemc.client.fog;

import com.theconstant.dontstravemc.TheConstant;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * 用 {@link ViewportEvent.RenderFog} 在 {@link AtmosphericFogEnvironment} 填好的 {@link FogData} 上
 * 做与旧 {@code CemeteryForestAtmosphericFogMixin} 相同的 lerp，保证 environmental / sky / cloud 与 mixin 期一致。
 */
@EventBusSubscriber(modid = TheConstant.MODID, value = Dist.CLIENT)
public final class CemeteryViewportFogHandler {

    private static final float SPATIAL_EPS = 1e-4f;

    private CemeteryViewportFogHandler() {
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (event.getType() != FogType.ATMOSPHERIC) {
            return;
        }
        if (!(event.getEnvironment() instanceof AtmosphericFogEnvironment)) {
            return;
        }
        Camera camera = event.getCamera();
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.level instanceof ClientLevel clientLevel)) {
            return;
        }
        BlockPos blockPos = camera.blockPosition();
        float spatial = CemeteryBiomeInfluence.cemeterySpatialWeight(clientLevel, blockPos);
        if (spatial <= SPATIAL_EPS) {
            return;
        }
        int rdChunks = Math.max(2, mc.options.getEffectiveRenderDistance());
        float renderDistanceBlocks = (float) (rdChunks * 16);

        FogData fogData = event.getFogData();
        float vanillaEnd = fogData.environmentalEnd;
        float vanillaStart = fogData.environmentalStart;
        float vanillaSky = fogData.skyEnd;
        float vanillaCloud = fogData.cloudEnd;

        float cappedDist = Math.min(renderDistanceBlocks, 192.0F);
        float thickEnd = Math.min(vanillaEnd, cappedDist * 0.48F);
        float thickStart = Math.min(vanillaStart, renderDistanceBlocks * 0.06F);
        float thickStartSafe = thickStart;
        float thickEndSafe = thickEnd;
        if (thickStartSafe >= thickEndSafe) {
            thickStartSafe = thickEndSafe * 0.25F;
        }

        float s = spatial;
        fogData.environmentalEnd = Mth.lerp(s, vanillaEnd, thickEndSafe);
        fogData.environmentalStart = Mth.lerp(s, vanillaStart, thickStartSafe);
        if (fogData.environmentalStart >= fogData.environmentalEnd) {
            fogData.environmentalStart = fogData.environmentalEnd * 0.25F;
        }

        float skyThick = Math.min(vanillaSky, thickEndSafe);
        float cloudThick = Math.min(vanillaCloud, thickEndSafe);
        fogData.skyEnd = Mth.lerp(s, vanillaSky, skyThick);
        fogData.cloudEnd = Mth.lerp(s, vanillaCloud, cloudThick);
    }
}
