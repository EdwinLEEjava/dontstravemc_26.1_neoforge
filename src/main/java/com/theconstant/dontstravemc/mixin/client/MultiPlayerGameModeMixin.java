package com.theconstant.dontstravemc.mixin.client;

import com.theconstant.dontstravemc.sound.client.ConstantRealmMusicManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Shadow
    private GameType localPlayerMode;

    @Unique
    private BlockState dontstravemc$capturedBreakState;

    @Inject(method = "attack", at = @At("RETURN"))
    private void dontstravemc$afterAttack(Player player, Entity entity, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != player || this.localPlayerMode == GameType.SPECTATOR) {
            return;
        }
        ConstantRealmMusicManager.onPlayerAttackEntity();
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void dontstravemc$captureBreakState(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            this.dontstravemc$capturedBreakState = mc.level.getBlockState(blockPos);
        } else {
            this.dontstravemc$capturedBreakState = null;
        }
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void dontstravemc$afterBreak(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(cir.getReturnValue()) && this.dontstravemc$capturedBreakState != null) {
            ConstantRealmMusicManager.onClientBlockBroken(this.dontstravemc$capturedBreakState);
        }
        this.dontstravemc$capturedBreakState = null;
    }
}
