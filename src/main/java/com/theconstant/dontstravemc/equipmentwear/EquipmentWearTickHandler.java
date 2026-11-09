package com.theconstant.dontstravemc.equipmentwear;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class EquipmentWearTickHandler {

    private EquipmentWearTickHandler() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EquipmentWearTickHandler::onServerTickPost);
    }

    private static void onServerTickPost(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (Player player : level.players()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    tickPlayerWear(serverPlayer);
                }
            }
        }
    }

    private static void tickPlayerWear(ServerPlayer player) {
        if (player.getAbilities().instabuild) {
            return;
        }
        ItemStack equipped = player.getItemBySlot(EquipmentSlot.HEAD);
        if (equipped.isEmpty()) {
            return;
        }
        EquipmentWearRegistry.WearConfig config = EquipmentWearRegistry.get(equipped.getItem());
        if (config == null || config.slot() != EquipmentSlot.HEAD) {
            return;
        }
        int max = config.maxWearTicks();
        int worn = EquipmentWearData.getTicksWorn(equipped);
        if (worn >= max) {
            return;
        }
        Item item = equipped.getItem();
        worn++;
        EquipmentWearData.setTicksWorn(equipped, worn);
        if (worn >= max) {
            player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            player.onEquippedItemBroken(item, EquipmentSlot.HEAD);
            player.getInventory().setChanged();
        }
    }
}
