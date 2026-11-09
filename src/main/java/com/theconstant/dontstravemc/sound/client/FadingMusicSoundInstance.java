package com.theconstant.dontstravemc.sound.client;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * 可循环播放的流式音乐，支持在一段时间无战斗后淡出。
 */
@OnlyIn(Dist.CLIENT)
public final class FadingMusicSoundInstance extends AbstractTickableSoundInstance {

    private final float peakVolume;
    private boolean fadingOut;
    /** 约 3 秒从满音量淡出到 0 */
    private static final float FADE_STEP = 1.0f / 60.0f;

    public FadingMusicSoundInstance(SoundEvent event, float volume, boolean looping) {
        super(event, SoundSource.MUSIC, SoundInstance.createUnseededRandom());
        this.peakVolume = volume;
        this.volume = volume;
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
        if (this.fadingOut) {
            this.volume -= FADE_STEP * this.peakVolume;
            if (this.volume <= 0.02f) {
                this.stop();
            }
        }
    }

    public void beginFadeOut() {
        this.fadingOut = true;
    }

    public void cancelFade() {
        this.fadingOut = false;
        this.volume = this.peakVolume;
    }

    /** 供 {@link DangerBedFollowerSoundInstance} 等与主层同步音量的逻辑使用。 */
    float fadeVolume() {
        return this.volume;
    }
}
