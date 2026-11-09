package com.theconstant.dontstravemc.event.charlieattack;

import com.theconstant.dontstravemc.world.ConstantDimensions;
import com.theconstant.dontstravemc.world.time.ConstantRealmTime;
import com.theconstant.dontstravemc.damagesource.ModDamageTypes;
import com.theconstant.dontstravemc.sound.ModSoundEvents;
import com.theconstant.dontstravemc.attachment.ModAttachments;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.LightLayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CharlieAttackListener {

    private static final int FIRST_MIN_COOLDOWN = 100; // 5s
    private static final int FIRST_MAX_COOLDOWN = 200; // 10s
    private static final int LOOP_MIN_COOLDOWN = 100;  // 5s
    private static final int LOOP_MAX_COOLDOWN = 220;  // 11s

    private static final float DAMAGE_AMOUNT = 13.5f;
    private static final float SANITY_PENALTY = 20.0f;

    private static final Map<UUID, AttackState> STATES = new HashMap<>();

    private CharlieAttackListener() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(CharlieAttackListener::onServerTickPost);
    }

    private static void onServerTickPost(ServerTickEvent.Post event) {
        var server = event.getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.getAbilities().instabuild || player.isSpectator()) {
                    STATES.remove(player.getUUID());
                    continue;
                }

                AttackState state = STATES.computeIfAbsent(player.getUUID(), id -> new AttackState());
                boolean dangerous = isCharlieDanger(player);

                if (!dangerous) {
                    if (state.wasDangerousLastTick) {
                        player.sendOverlayMessage(Component.translatable("message.dontstravemc.charlie.safe_again"));
                    }
                    state.resetRound();
                    state.wasDangerousLastTick = false;
                    continue;
                }

                state.wasDangerousLastTick = true;

                if (state.remainingTicks <= 0) {
                    setupNewRound(state, player, state.hasCompletedFirstAttack);
                }

                state.remainingTicks--;

                if (!state.warnPlayed && state.remainingTicks <= state.warningTriggerRemainingTicks) {
                    playCharlieSound(player, ModSoundEvents.CHARLIE_WARN.get());
                    state.warnPlayed = true;
                    state.whatIsThatTicks = 20;
                }

                if (state.whatIsThatTicks > 0) {
                    state.whatIsThatTicks--;
                    if (state.whatIsThatTicks == 0) {
                        player.sendOverlayMessage(Component.translatable("message.dontstravemc.charlie.what_is_that"));
                    }
                }

                if (state.remainingTicks <= 0) {
                    playCharlieSound(player, ModSoundEvents.CHARLIE_BITE.get());
                    player.hurt(player.level().damageSources().source(ModDamageTypes.DARKNESS_ATTACK), DAMAGE_AMOUNT);
                    ModAttachments.getSanity(player).addSanity(-SANITY_PENALTY);
                    player.sendOverlayMessage(Component.translatable("message.dontstravemc.charlie.bitten"));

                    state.hasCompletedFirstAttack = true;
                    setupNewRound(state, player, true);
                }
            }

        STATES.keySet().removeIf(uuid -> server.getPlayerList().getPlayer(uuid) == null);
    }

    /**
     * 使用 {@code playSound(null, ...)}，避�?{@link ServerPlayer#playSound} 把本人从广播中排除导致单机听不到�?     */
    private static void playCharlieSound(ServerPlayer player, SoundEvent event) {
        ServerLevel level = (ServerLevel) player.level();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), event, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static boolean isCharlieDanger(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return false;
        }

        if (level.dimension() != ConstantDimensions.THE_CONSTANT) {
            return false;
        }

        int skyLight = level.getBrightness(LightLayer.SKY, player.blockPosition());
        int blockLight = level.getBrightness(LightLayer.BLOCK, player.blockPosition());

        if (skyLight == 0) {
            return blockLight == 0;
        }

        boolean night = ConstantRealmTime.isNight(level, level.getDefaultClockTime());
        if (!night || blockLight != 0) {
            return false;
        }
        // 满月：月光照到的地方（能看到天光）Charlie 不会出现；洞穴等天光为 0 的地方在上面已按黑暗处理
        MoonPhase phase = level.environmentAttributes().getValue(EnvironmentAttributes.MOON_PHASE, player.position());
        return !ConstantRealmTime.isFullMoon(phase);
    }

    private static void setupNewRound(AttackState state, ServerPlayer player, boolean loopRound) {
        int min = loopRound ? LOOP_MIN_COOLDOWN : FIRST_MIN_COOLDOWN;
        int max = loopRound ? LOOP_MAX_COOLDOWN : FIRST_MAX_COOLDOWN;

        int cooldown = min + player.getRandom().nextInt(max - min + 1);
        float warningRatio = 0.4f + player.getRandom().nextFloat() * 0.4f;

        int elapsedForWarning = Mth.floor(cooldown * warningRatio);
        state.remainingTicks = cooldown;
        state.warningTriggerRemainingTicks = Math.max(1, cooldown - elapsedForWarning);
        state.warnPlayed = false;
        state.whatIsThatTicks = 0;
    }

    private static final class AttackState {
        int remainingTicks;
        int warningTriggerRemainingTicks;
        int whatIsThatTicks;
        boolean warnPlayed;
        boolean hasCompletedFirstAttack;
        boolean wasDangerousLastTick;

        void resetRound() {
            this.remainingTicks = 0;
            this.warningTriggerRemainingTicks = 0;
            this.whatIsThatTicks = 0;
            this.warnPlayed = false;
        }
    }
}
