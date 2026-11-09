package com.theconstant.dontstravemc.mixin.client;

import com.theconstant.dontstravemc.render.state.BedRenderStateExtension;
import net.minecraft.client.renderer.blockentity.state.BedRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BedRenderState.class)
public class BedRenderStateMixin implements BedRenderStateExtension {
    @Unique
    private boolean dontstravemc$pigmanBed;

    @Override
    public boolean dontstravemc$isPigmanBed() {
        return this.dontstravemc$pigmanBed;
    }

    @Override
    public void dontstravemc$setPigmanBed(boolean pigmanBed) {
        this.dontstravemc$pigmanBed = pigmanBed;
    }
}
