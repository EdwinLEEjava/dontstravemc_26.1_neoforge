package com.theconstant.dontstravemc.sound.client;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * 危险战斗 BGM 的底色层：音量始终为主层当前音量的一定比例（主层满音量时达到 {@code bedPeakVolume}），
 * 以便与 {@link FadingMusicSoundInstance} 同步淡出。
 */
@OnlyIn(Dist.CLIENT)
public final class DangerBedFollowerSoundInstance extends AbstractTickableSoundInstance {

    private final FadingMusicSoundInstance mainLayer;
    private final float mainPeakVolume;
    private final float bedPeakVolume;

    public DangerBedFollowerSoundInstance(
            SoundEvent bedEvent,
            FadingMusicSoundInstance mainLayer,
            float mainPeakVolume,
            float bedPeakVolume,
            boolean looping
    ) {
        super(bedEvent, SoundSource.MUSIC, SoundInstance.createUnseededRandom());
        this.mainLayer = mainLayer;
        this.mainPeakVolume = mainPeakVolume;
        this.bedPeakVolume = bedPeakVolume;
        this.volume = bedPeakVolume;
        this.pitch = 1.0f;
        this.looping = looping;
        this.relative = true;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
    }

    @Override
    public void tick() {
        if (this.mainLayer.isStopped()) {
            this.stop();
            return;
        }
        float ratio = this.mainPeakVolume > 0.001f ? this.mainLayer.fadeVolume() / this.mainPeakVolume : 0.0f;
        if (ratio < 0.0f) {
            ratio = 0.0f;
        }
        this.volume = this.bedPeakVolume * ratio;
    }
}
