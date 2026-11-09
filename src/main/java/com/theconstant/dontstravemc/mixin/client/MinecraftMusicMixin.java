package com.theconstant.dontstravemc.mixin.client;

import com.theconstant.dontstravemc.sound.ModSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.random.RandomGenerator;

@Mixin(Minecraft.class)
public class MinecraftMusicMixin {

    private static final Identifier THE_CONSTANT =
            Identifier.fromNamespaceAndPath("dontstravemc", "the_constant");

    private static final Music DST_MENU_MUSIC =
            new Music(ModSoundEvents.MUSIC_MENU_DST, 20, 600, true);

    /** 使用 {@link #MENU_DST_MUSIC_CHANCE} 的时机：每次进入主菜单（无世界、无玩家）时掷一次，而非每个 tick。 */
    private static final double MENU_DST_MUSIC_CHANCE = 0.3D;

    private static final RandomGenerator MENU_ROLL = RandomGenerator.getDefault();

    /** null：当前不在标题态；非 null：本会话是否使用 DST 主菜单曲。 */
    private static @Nullable Boolean titleMenuUseDstMusic;

    @Inject(method = "getSituationalMusic", at = @At("RETURN"), cancellable = true)
    private void dontstravemc$overrideSituationalMusic(CallbackInfoReturnable<Music> cir) {
        Minecraft mc = (Minecraft) (Object) this;
        if (mc.level != null && mc.level.dimension().identifier().equals(THE_CONSTANT)) {
            cir.setReturnValue(null);
            return;
        }

        boolean onTitleMenu = mc.player == null && mc.level == null;
        if (!onTitleMenu) {
            titleMenuUseDstMusic = null;
            return;
        }

        if (titleMenuUseDstMusic == null) {
            titleMenuUseDstMusic = MENU_ROLL.nextDouble() < MENU_DST_MUSIC_CHANCE;
        }
        if (Boolean.TRUE.equals(titleMenuUseDstMusic)) {
            cir.setReturnValue(DST_MENU_MUSIC);
        }
    }
}
