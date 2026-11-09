package com.theconstant.dontstravemc.sound.client;

import com.theconstant.dontstravemc.entity.animal.MoleWormEntity;
import com.theconstant.dontstravemc.sound.ModSoundEvents;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 客户端鼹鼠虫音效管理器。
 * 在 END_CLIENT_TICK 中每帧对范围内的每只鼹鼠调用 {@link #tick(MoleWormEntity)}。
 */
@OnlyIn(Dist.CLIENT)
public class MoleWormSoundManager {

    // ---- 每实体状态 ----
    private static final Map<UUID, PerEntityState> STATES = new HashMap<>();

    public static void tick(MoleWormEntity mole) {
        UUID id = mole.getUUID();
        PerEntityState s = STATES.computeIfAbsent(id, k -> new PerEntityState());

        int transition = mole.getTransitionAnimPublic(); // 0=none 1=enter 2=exit
        int stunPhase = mole.getStunPhasePublic();           // 0=none 1=pre 2=loop 3=pst
        boolean isIdle   = mole.isInIdleAnim();
        boolean isSleep  = mole.isInSleepLoopAnim();
        boolean isAction = mole.isInActionAnim();

        // ---------- STUNNED_PRE（stunPhase == 1）----------
        if (stunPhase == 1) {
            if (!s.stunPreSoundPlayed) {
                playAt(mole, ModSoundEvents.MOLE_WORM_JUMP.get(), 1.0f, 1.0f);
                s.stunPreSoundPlayed = true;
            }
            return;
        } else {
            s.stunPreSoundPlayed = false;
        }

        // ---------- ENTER_ANIM（transition == 1：地下→地上）----------
        if (transition == 1) {
            if (!s.enterSoundPlayed) {
                playAt(mole, ModSoundEvents.MOLE_WORM_DIG.get(),    1.0f, 1.0f);
                playAt(mole, ModSoundEvents.MOLE_WORM_EMERGE.get(), 1.0f, 1.0f);
                s.enterSoundPlayed = true;
            }
            s.exitDigPlayed    = false;
            s.exitTicksElapsed = 0;
            s.sniffCooldown    = 0;
            s.actionSoundPlayed = false;
            return;
        } else {
            s.enterSoundPlayed = false;
        }

        // ---------- EXIT_ANIM（transition == 2：地上→地下）----------
        if (transition == 2) {
            s.exitTicksElapsed++;
            // 1.25 秒 = 25 tick
            if (!s.exitDigPlayed && s.exitTicksElapsed >= 25) {
                playAt(mole, ModSoundEvents.MOLE_WORM_DIG.get(), 1.0f, 1.0f);
                s.exitDigPlayed = true;
            }
            s.sniffCooldown     = 0;
            s.actionSoundPlayed = false;
            return;
        } else {
            s.exitDigPlayed    = false;
            s.exitTicksElapsed = 0;
        }

        // ---------- IDLE_ANIM ----------
        if (isIdle) {
            s.sniffCooldown--;
            if (s.sniffCooldown <= 0) {
                playAt(mole, ModSoundEvents.MOLE_WORM_SNIFF.get(), 0.6f, 1.0f);
                // sniff 各变体平均约 1.5s，加随机抖动
                s.sniffCooldown = 25 + mole.getRandom().nextInt(15);
            }
            s.actionSoundPlayed = false;
            return;
        }

        // ---------- SLEEP_LOOP ----------
        if (isSleep) {
            s.sniffCooldown--;
            if (s.sniffCooldown <= 0) {
                playAt(mole, ModSoundEvents.MOLE_WORM_SNIFF.get(), 0.4f, 0.9f);
                // 音效结束后再等 1 秒（20tick），总冷却约 50tick
                s.sniffCooldown = 50 + mole.getRandom().nextInt(10);
            }
            s.actionSoundPlayed = false;
            return;
        }

        // 不在 idle/sleep，重置 sniff 计时，下次进入立刻播
        s.sniffCooldown = 0;

        // ---------- ACTION_ANIM ----------
        if (isAction) {
            if (!s.actionSoundPlayed) {
                playAt(mole, ModSoundEvents.MOLE_WORM_PICKUP.get(),
                        0.8f, 0.9f + mole.getRandom().nextFloat() * 0.2f);
                s.actionSoundPlayed = true;
            }
            return;
        } else {
            s.actionSoundPlayed = false;
        }
    }

    public static void remove(UUID id) {
        STATES.remove(id);
    }

    public static void clearAll() {
        STATES.clear();
    }

    // ---- 工具方法：在实体位置播放定位音效 ----
    private static void playAt(MoleWormEntity mole, SoundEvent event, float volume, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        mc.level.playLocalSound(
                mole.getX(), mole.getY(), mole.getZ(),
                event, SoundSource.NEUTRAL,
                volume, pitch, false
        );
    }

    // ---- 每实体状态容器 ----
    private static class PerEntityState {
        boolean stunPreSoundPlayed = false;
        boolean enterSoundPlayed  = false;
        boolean exitDigPlayed     = false;
        int     exitTicksElapsed  = 0;
        int     sniffCooldown     = 0;   // <= 0 时触发下一个 sniff
        boolean actionSoundPlayed = false;
    }
}
