package com.theconstant.dontstravemc.event;

import com.theconstant.dontstravemc.attachment.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import com.theconstant.dontstravemc.TheConstant;

/**
 * 玩家死亡后重生时理智恢复至 50%（100/200）。在 {@link PlayerEvent.Clone} 中写入新实体，与 {@code copyOnDeath} 复制后再调整一致。
 */
@EventBusSubscriber(modid = TheConstant.MODID)
public final class PlayerDeathHandler {
    private PlayerDeathHandler() {}

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || !(event.getEntity() instanceof ServerPlayer newPlayer)) {
            return;
        }
        ModAttachments.getSanity(newPlayer).setSanity(100f);
        ModAttachments.syncSanity(newPlayer);
    }
}
