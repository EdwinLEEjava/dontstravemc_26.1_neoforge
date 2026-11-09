package com.theconstant.dontstravemc.sound.client;

import com.theconstant.dontstravemc.entity.animal.MoleWormEntity;
import com.theconstant.dontstravemc.sound.ModSoundEvents;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.sounds.SoundSource;

@OnlyIn(Dist.CLIENT)
public class MoleWormTravelLoopSoundInstance extends EntityBoundSoundInstance {

    private final MoleWormEntity moleWorm;

    public MoleWormTravelLoopSoundInstance(MoleWormEntity moleWorm) {
        super(ModSoundEvents.MOLE_WORM_TRAVEL.get(), SoundSource.NEUTRAL, 0.8F, 1.0F, moleWorm, moleWorm.getRandom().nextLong());
        this.moleWorm = moleWorm;
        this.looping = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.moleWorm.isAlive() || !this.moleWorm.isInWalkLoopAnim()) {
            this.stop();
        }
    }
}
