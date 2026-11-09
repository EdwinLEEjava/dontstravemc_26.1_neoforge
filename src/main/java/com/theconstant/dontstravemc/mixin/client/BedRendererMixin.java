package com.theconstant.dontstravemc.mixin.client;

import com.theconstant.dontstravemc.block.ModBlocks;
import com.theconstant.dontstravemc.render.state.BedRenderStateExtension;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BedRenderer;
import net.minecraft.client.renderer.blockentity.state.BedRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 猪人床使用 {@link Sheets#BED_MAPPER}，与原版床同一图集 {@link Sheets#BED_SHEET}；
 * 纹理：{@code assets/dontstravemc/textures/entity/bed/pigman_bed.png}。
 */
@Mixin(BedRenderer.class)
public class BedRendererMixin {

    private static final SpriteId PIGMAN_BED_SPRITE =
            Sheets.BED_MAPPER.apply(Identifier.fromNamespaceAndPath("dontstravemc", "pigman_bed"));

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void dontstravemc$markPigmanBed(
            BedBlockEntity bedBlockEntity,
            BedRenderState bedRenderState,
            float partialTick,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            CallbackInfo ci
    ) {
        ((BedRenderStateExtension) bedRenderState).dontstravemc$setPigmanBed(
                bedBlockEntity.getBlockState().is(ModBlocks.PIGMAN_BED.get())
        );
    }

    @ModifyVariable(method = "submit", at = @At(value = "STORE", ordinal = 0))
    private SpriteId dontstravemc$usePigmanBedSprite(
            SpriteId sprite,
            BedRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState
    ) {
        if (((BedRenderStateExtension) state).dontstravemc$isPigmanBed()) {
            return PIGMAN_BED_SPRITE;
        }
        return sprite;
    }
}
