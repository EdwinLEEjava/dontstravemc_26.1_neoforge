package com.theconstant.dontstravemc.sound.client;

import com.theconstant.dontstravemc.sound.ModSoundEvents;
import com.theconstant.dontstravemc.world.time.ConstantRealmTime;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * 永恒领域：昼夜过渡（黎明/黄昏）、战斗音乐（危险/Boss 占位）、劳作音乐�?
 * 危险 BGM：主层为 {@link ModSoundEvents#MUSIC_FALL_DANGER} 三首之一（数据包内随机），底色为 {@link ModSoundEvents#MUSIC_FALL_DANGER_BED}（opera），峰值音量为 {@link #DANGER_BED_VOLUME}，与主层同步淡出�?
 * 战斗 BGM：本地玩家攻击实体（{@link #onPlayerAttackEntity}）或受伤（{@code hurtTime} 上升）时触发�?
 * Boss 战斗曲暂未接入实体，仅保�?{@link #enterBossCombat()} 供日后调用�?
 */
@OnlyIn(Dist.CLIENT)
public final class ConstantRealmMusicManager {

    private static final Identifier THE_CONSTANT =
            Identifier.fromNamespaceAndPath("dontstravemc", "the_constant");

    /** 无攻�?受击后经过该时间（tick）开始淡出战斗音�?*/
    private static final int COMBAT_GRACE_TICKS = 8 * 20;
    private static final float DANGER_MUSIC_VOLUME = 0.85f;
    /** 危险 BGM 底色（opera）相对主层的峰值音量，与主层同步比例淡�?*/
    private static final float DANGER_BED_VOLUME = 0.1f;
    private static final float BOSS_MUSIC_VOLUME = 0.9f;
    private static final float WORK_MUSIC_VOLUME = 0.8f;
    private static final float STINGER_VOLUME = 0.55f;

    /** 劳作 BGM：首次尝�?0.3，每多破坏一个相关方�?+0.05，上�?0.7 */
    private static final float WORK_TRIGGER_BASE = 0.3f;
    private static final float WORK_TRIGGER_STEP = 0.05f;
    private static final float WORK_TRIGGER_MAX = 0.7f;
    /** 劳作 BGM 触发后的冷却（tick），期间不累�?streak、不判定 */
    private static final int WORK_MUSIC_COOLDOWN_TICKS = 120 * 20;

    private static final Random RANDOM = new Random();

    private static int combatGraceTicksRemaining;
    /** -1 表示尚未初始化，避免进世界首帧误判为受击 */
    private static int lastPlayerHurtTime = -1;

    private static @Nullable FadingMusicSoundInstance dangerLayerMusic;
    private static @Nullable DangerBedFollowerSoundInstance dangerBedMusic;
    private static @Nullable FadingMusicSoundInstance bossMusic;
    private static @Nullable SoundInstance workMusic;

    /** 用于检测时间回拨；�?{@link #lastTheConstantPhase} 一起初始化昼夜条状�?*/
    private static long lastDayTimeObserved = -1L;
    /** 永恒领域上一 tick 的昼夜相位（白天 / 黄昏 / 夜晚�?*/
    private static @Nullable ConstantRealmTime.Phase lastTheConstantPhase = null;

    /** 自上次成功播放劳�?BGM 以来，在冷却外已判定失败的破坏次数（用于抬高下次概率�?*/
    private static int workBreakStreak;
    /** >0 时不进行劳作判定、不增加 streak */
    private static int workMusicCooldownTicksRemaining;

    private ConstantRealmMusicManager() {}

    public static void resetForDisconnect() {
        stopAllManaged(Minecraft.getInstance());
        combatGraceTicksRemaining = 0;
        lastPlayerHurtTime = -1;
        lastDayTimeObserved = -1L;
        lastTheConstantPhase = null;
        workBreakStreak = 0;
        workMusicCooldownTicksRemaining = 0;
    }

    /** 日后 Boss 战开始时由实体或事件调用�?*/
    public static void enterBossCombat() {
        Minecraft mc = Minecraft.getInstance();
        if (!isTheConstant(mc)) return;
        stopWork(mc);
        stopDangerLayered(mc);
        if (bossMusic != null && !bossMusic.isStopped()) {
            bossMusic.cancelFade();
            combatGraceTicksRemaining = COMBAT_GRACE_TICKS;
            return;
        }
        bossMusic = new FadingMusicSoundInstance(ModSoundEvents.MUSIC_FALL_BOSS.get(), BOSS_MUSIC_VOLUME, true);
        mc.getSoundManager().play(bossMusic);
        combatGraceTicksRemaining = COMBAT_GRACE_TICKS;
    }

    /** Boss 战结束时可调用（例如 Boss 死亡）�?*/
    public static void exitBossCombat() {
        Minecraft mc = Minecraft.getInstance();
        if (bossMusic != null) {
            mc.getSoundManager().stop(bossMusic);
            bossMusic = null;
        }
    }

    public static void onPlayerAttackEntity() {
        markCombatActivity(Minecraft.getInstance());
    }

    public static void onClientBlockBroken(BlockState state) {
        Minecraft mc = Minecraft.getInstance();
        if (!isTheConstant(mc) || mc.player == null) return;
        if (!isWorkBlock(state)) return;
        if (workMusicCooldownTicksRemaining > 0) return;
        if (bossMusic != null && !bossMusic.isStopped()) return;
        if (dangerLayerMusic != null && !dangerLayerMusic.isStopped()) return;
        if (workMusic != null && mc.getSoundManager().isActive(workMusic)) return;

        float chance = Math.min(WORK_TRIGGER_BASE + workBreakStreak * WORK_TRIGGER_STEP, WORK_TRIGGER_MAX);
        if (RANDOM.nextFloat() > chance) {
            workBreakStreak++;
            return;
        }

        workBreakStreak = 0;
        workMusicCooldownTicksRemaining = WORK_MUSIC_COOLDOWN_TICKS;
        workMusic = new SimpleSoundInstance(
                ModSoundEvents.MUSIC_FALL_WORK.get(),
                SoundSource.MUSIC,
                WORK_MUSIC_VOLUME,
                1.0F,
                RandomSource.create(),
                0.0,
                0.0,
                0.0
        );
        mc.getSoundManager().play(workMusic);
    }

    public static void tick(Minecraft mc) {
        if (mc.level == null) {
            resetForDisconnect();
            return;
        }
        if (mc.player == null) {
            return;
        }
        if (!isTheConstant(mc)) {
            stopAllManaged(mc);
            combatGraceTicksRemaining = 0;
            lastPlayerHurtTime = -1;
            lastDayTimeObserved = -1L;
            lastTheConstantPhase = null;
            workBreakStreak = 0;
            workMusicCooldownTicksRemaining = 0;
            return;
        }

        if (workMusicCooldownTicksRemaining > 0) {
            workMusicCooldownTicksRemaining--;
        }

        Player player = mc.player;
        int hurt = player.hurtTime;
        // 被攻击时与主动攻击一样进入战�?BGM
        if (lastPlayerHurtTime >= 0 && hurt > lastPlayerHurtTime) {
            markCombatActivity(mc);
        }
        lastPlayerHurtTime = hurt;

        tickDayNightStingers(mc, player.level());

        boolean bossActive = bossMusic != null && !bossMusic.isStopped();
        boolean dangerActive = dangerLayerMusic != null && !dangerLayerMusic.isStopped();

        if (bossActive || dangerActive) {
            if (combatGraceTicksRemaining > 0) {
                combatGraceTicksRemaining--;
            }
            if (combatGraceTicksRemaining <= 0) {
                if (bossActive && bossMusic != null) {
                    bossMusic.beginFadeOut();
                } else if (dangerActive && dangerLayerMusic != null) {
                    dangerLayerMusic.beginFadeOut();
                }
            }
        }

        if (workMusic != null && !mc.getSoundManager().isActive(workMusic)) {
            workMusic = null;
        }

        if (dangerLayerMusic != null && dangerLayerMusic.isStopped()) {
            dangerLayerMusic = null;
            if (dangerBedMusic != null) {
                mc.getSoundManager().stop(dangerBedMusic);
                dangerBedMusic = null;
            }
        }
        if (dangerBedMusic != null && dangerBedMusic.isStopped()) {
            dangerBedMusic = null;
        }
        if (bossMusic != null && bossMusic.isStopped()) {
            bossMusic = null;
        }
    }

    /**
     * 永恒领域：按 {@link ConstantRealmTime} 三档相位播过渡乐——昼→昏 {@link ModSoundEvents#MUSIC_DUSK}�?
     * 昏→夜无声，夜→�?{@link ModSoundEvents#MUSIC_DAWN}�?
     */
    private static void tickDayNightStingers(Minecraft mc, Level level) {
        long dayTime = level.getDefaultClockTime();
        ConstantRealmTime.Phase now = ConstantRealmTime.phaseTheConstant(dayTime);

        if (lastTheConstantPhase == null || dayTime < lastDayTimeObserved) {
            lastTheConstantPhase = now;
            lastDayTimeObserved = dayTime;
            return;
        }
        lastDayTimeObserved = dayTime;

        ConstantRealmTime.Phase prev = lastTheConstantPhase;
        lastTheConstantPhase = now;

        if (prev == ConstantRealmTime.Phase.DAY && now == ConstantRealmTime.Phase.DUSK) {
            playStinger(mc, ModSoundEvents.MUSIC_DUSK.get());
        } else if (prev == ConstantRealmTime.Phase.DUSK && now == ConstantRealmTime.Phase.NIGHT) {
            // 黄昏进入夜晚：不播放 stinger
        } else if (prev == ConstantRealmTime.Phase.NIGHT && now == ConstantRealmTime.Phase.DAY) {
            playStinger(mc, ModSoundEvents.MUSIC_DAWN.get());
        } else if (prev == ConstantRealmTime.Phase.DAY && now == ConstantRealmTime.Phase.NIGHT) {
            // 时间跳跃跳过黄昏：只播黄�?stinger
            playStinger(mc, ModSoundEvents.MUSIC_DUSK.get());
        }
    }

    private static void playStinger(Minecraft mc, net.minecraft.sounds.SoundEvent event) {
        stopWork(mc);
        mc.getSoundManager().play(new SimpleSoundInstance(
                event,
                SoundSource.MUSIC,
                STINGER_VOLUME,
                1.0F,
                RandomSource.create(),
                0.0,
                0.0,
                0.0
        ));
    }

    private static void markCombatActivity(Minecraft mc) {
        if (!isTheConstant(mc)) return;
        stopWork(mc);

        combatGraceTicksRemaining = COMBAT_GRACE_TICKS;

        if (bossMusic != null && !bossMusic.isStopped()) {
            bossMusic.cancelFade();
            return;
        }

        if (dangerLayerMusic != null && !dangerLayerMusic.isStopped()) {
            dangerLayerMusic.cancelFade();
            return;
        }

        dangerLayerMusic = new FadingMusicSoundInstance(ModSoundEvents.MUSIC_FALL_DANGER.get(), DANGER_MUSIC_VOLUME, true);
        dangerBedMusic = new DangerBedFollowerSoundInstance(
                ModSoundEvents.MUSIC_FALL_DANGER_BED.get(),
                dangerLayerMusic,
                DANGER_MUSIC_VOLUME,
                DANGER_BED_VOLUME,
                true
        );
        mc.getSoundManager().play(dangerLayerMusic);
        mc.getSoundManager().play(dangerBedMusic);
    }

    private static void stopWork(Minecraft mc) {
        if (workMusic != null) {
            mc.getSoundManager().stop(workMusic);
            workMusic = null;
        }
    }

    private static void stopDangerLayered(Minecraft mc) {
        if (dangerLayerMusic != null) {
            mc.getSoundManager().stop(dangerLayerMusic);
            dangerLayerMusic = null;
        }
        if (dangerBedMusic != null) {
            mc.getSoundManager().stop(dangerBedMusic);
            dangerBedMusic = null;
        }
    }

    private static void stopAllManaged(Minecraft mc) {
        stopWork(mc);
        stopDangerLayered(mc);
        if (bossMusic != null) {
            mc.getSoundManager().stop(bossMusic);
            bossMusic = null;
        }
    }

    private static boolean isWorkBlock(BlockState state) {
        if (state.is(BlockTags.LOGS)) {
            return true;
        }
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE);
    }

    private static boolean isTheConstant(Minecraft mc) {
        if (mc.level == null) return false;
        return mc.level.dimension().identifier().equals(THE_CONSTANT);
    }
}
