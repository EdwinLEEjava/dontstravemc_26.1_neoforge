package com.theconstant.dontstravemc.entity.ambient;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Per-player independent timer: spawns a CreepyEyes every 5-7 seconds
 * when conditions are met (sanity < 80%, night, dark spot within 10-15 blocks).
 */
public class CreepyEyesSpawnListener {

    /** Remaining ticks until next spawn check for each player. */
    private static final Map<UUID, Integer> cooldowns = new HashMap<>();

    private static final int MIN_INTERVAL = 100; // 5 s
    private static final int MAX_INTERVAL = 140; // 7 s

    public static void register() {
        NeoForge.EVENT_BUS.addListener(CreepyEyesSpawnListener::onServerTickPost);
    }

    private static void onServerTickPost(ServerTickEvent.Post event) {
        var server = event.getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.getAbilities().instabuild || player.isSpectator()) {
                cooldowns.remove(player.getUUID());
                continue;
            }

            UUID id = player.getUUID();
            int remaining = cooldowns.getOrDefault(id, 0) - 1;

            if (remaining <= 0) {
                // Reset with a fresh random interval before attempting spawn
                int nextInterval = MIN_INTERVAL +
                        player.getRandom().nextInt(MAX_INTERVAL - MIN_INTERVAL + 1);
                cooldowns.put(id, nextInterval);

                CreepyEyesSpawner.checkAndSpawn(player);
            } else {
                cooldowns.put(id, remaining);
            }
        }

        // Clean up offline players
        cooldowns.keySet().removeIf(
                uuid -> server.getPlayerList().getPlayer(uuid) == null
        );
    }
}
