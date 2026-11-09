package com.theconstant.dontstravemc.particle;

import com.theconstant.dontstravemc.TheConstant;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, TheConstant.MODID);

    /** 传送门上方沿螺旋线绕圈上升的墨点，连起来就是旋风。 */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PORTAL_WHIRL =
            PARTICLE_TYPES.register("portal_whirl", () -> new SimpleParticleType(false));

    /** 恒定维度雨滴落地的灰白水花（替代原版蓝色 rain 粒子，行为与原版相同）。 */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RAIN_SPLASH =
            PARTICLE_TYPES.register("rain_splash", () -> new SimpleParticleType(false));

    private ModParticles() {
    }
}
