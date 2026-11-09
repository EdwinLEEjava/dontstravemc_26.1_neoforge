package com.theconstant.dontstravemc.item.custom;

import com.theconstant.dontstravemc.item.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TorchLightHandler {

    private static final int TORCH_LIGHT_LEVEL = 14;
    private static final Map<UUID, BlockPos> lastLightPos = new HashMap<>();

    public static void register() {
        NeoForge.EVENT_BUS.addListener(TorchLightHandler::onServerTickPost);
    }

    private static void onServerTickPost(ServerTickEvent.Post event) {
        var server = event.getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator()) continue;
            if (!(player.level() instanceof ServerLevel level)) continue;

            InteractionHand torchHand = getTorchHand(player);
            UUID uuid = player.getUUID();
            BlockPos lightPos = player.blockPosition().above();
            BlockPos lastPos = lastLightPos.get(uuid);

            if (torchHand != null) {
                ItemStack torch = player.getItemInHand(torchHand);

                // 每 tick 消耗耐久度
                if (torch.isDamageableItem()) {
                    torch.hurtAndBreak(1, player, torchHand);
                }

                if (lastPos == null || !lastPos.equals(lightPos)) {
                    if (lastPos != null) removeLightAt(level, lastPos);
                    placeLightAt(level, lightPos);
                    lastLightPos.put(uuid, lightPos);
                }
            } else {
                if (lastPos != null) {
                    removeLightAt(level, lastPos);
                    lastLightPos.remove(uuid);
                }
            }
        }
    }

    private static InteractionHand getTorchHand(ServerPlayer player) {
        if (player.getMainHandItem().getItem() == ModItems.TORCH.get()) return InteractionHand.MAIN_HAND;
        if (player.getOffhandItem().getItem() == ModItems.TORCH.get()) return InteractionHand.OFF_HAND;
        return null;
    }

    private static void placeLightAt(ServerLevel level, BlockPos pos) {
        BlockState existing = level.getBlockState(pos);
        // 只在空气或已有的 light 方块位置放置，不覆盖任何其他方块
        if (existing.isAir() || existing.getBlock() == Blocks.LIGHT) {
            BlockState lightState = Blocks.LIGHT.defaultBlockState()
                    .setValue(LightBlock.LEVEL, TORCH_LIGHT_LEVEL);
            level.setBlock(pos, lightState, 2);
        }
    }

    private static void removeLightAt(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() == Blocks.LIGHT) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }
}
