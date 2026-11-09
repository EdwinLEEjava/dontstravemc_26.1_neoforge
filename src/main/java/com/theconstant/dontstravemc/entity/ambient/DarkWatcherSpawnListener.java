package com.theconstant.dontstravemc.entity.ambient;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DarkWatcherSpawnListener {

    private static final Map<UUID, Integer> playerSpawnTimers = new HashMap<>();
    private static final int SPAWN_CHECK_INTERVAL = 1200; // 60 seconds

    public static void register() {
        NeoForge.EVENT_BUS.addListener(DarkWatcherSpawnListener::onServerTickPost);
    }

    private static void onServerTickPost(ServerTickEvent.Post event) {
        var server = event.getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            // 跳过创造模式和旁观者模式的玩家
            if (player.getAbilities().instabuild || player.isSpectator()) {
                playerSpawnTimers.remove(player.getUUID());
                continue;
            }

            UUID playerId = player.getUUID();
            int currentTicks = playerSpawnTimers.getOrDefault(playerId, 0);

            currentTicks++;
            playerSpawnTimers.put(playerId, currentTicks);

            // 每分钟检查一次
            if (currentTicks >= SPAWN_CHECK_INTERVAL) {
                playerSpawnTimers.put(playerId, 0);
                DarkWatcherSpawner.checkAndSpawn(player);
            }
        }

        // 清理已离线玩家的计时器
        playerSpawnTimers.keySet().removeIf(uuid ->
                server.getPlayerList().getPlayer(uuid) == null
        );
    }
}
