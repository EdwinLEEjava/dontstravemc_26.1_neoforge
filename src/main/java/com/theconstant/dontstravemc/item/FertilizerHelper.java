package com.theconstant.dontstravemc.item;

import com.theconstant.dontstravemc.TheConstant;
import com.theconstant.dontstravemc.block.Fertilizable;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Universal fertilizer system.
 * Register any item as a fertilizer, and it will automatically work
 * on any block implementing the Fertilizable interface.
 */
@EventBusSubscriber(modid = TheConstant.MODID)
public final class FertilizerHelper {

    private static final Set<net.minecraft.world.item.Item> FERTILIZERS = new HashSet<>();

    private FertilizerHelper() {
    }

    public static void registerFertilizer(net.minecraft.world.item.Item item) {
        FERTILIZERS.add(item);
    }

    public static boolean isFertilizer(ItemStack stack) {
        return !stack.isEmpty() && FERTILIZERS.contains(stack.getItem());
    }

    public static void registerFertilizers() {
        registerFertilizer(Items.BONE_MEAL);
        registerFertilizer(ModItems.SPOILED_FOOD.get());
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        if (hand == null) {
            return;
        }
        ItemStack heldItem = player.getItemInHand(hand);
        if (!isFertilizer(heldItem)) {
            return;
        }

        BlockState state = event.getLevel().getBlockState(event.getPos());
        Block block = state.getBlock();

        if (block instanceof Fertilizable fertilizable) {
            if (fertilizable.canBeFertilized(state, event.getLevel(), event.getPos())) {
                fertilizable.fertilize(state, event.getLevel(), event.getPos(), player);

                if (!player.isCreative()) {
                    heldItem.shrink(1);
                }

                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }
}
