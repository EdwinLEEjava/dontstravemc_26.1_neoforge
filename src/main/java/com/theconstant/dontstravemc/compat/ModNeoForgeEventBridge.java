package com.theconstant.dontstravemc.compat;

import com.theconstant.dontstravemc.TheConstant;
import com.theconstant.dontstravemc.block.ModBlocks;
import com.theconstant.dontstravemc.block.custom.FlintBlock;
import com.theconstant.dontstravemc.item.ModItems;
import com.theconstant.dontstravemc.item.custom.HamBatItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

/**
 * 将部分原版类 Mixin 改为 NeoForge 总线事件，降低与其它 Mod 的 Mixin 冲突面。
 * <p>
 * 燧石：{@link UseItemOnBlockEvent} 在
 * {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#useItemOn} 的 BLOCK
 * 阶段，对应方块侧 {@code useItemOn} 路径（见 NeoForge 对 {@code BlockStateBase#useItemOn} 的注入）。
 * </p>
 */
@EventBusSubscriber(modid = TheConstant.MODID)
public final class ModNeoForgeEventBridge {

    private static final Identifier FRESHNESS_ATTACK_MODIFIER = Identifier.fromNamespaceAndPath("dontstravemc", "hambat_freshness_attack");

    private ModNeoForgeEventBridge() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onUseItemOnBlock(UseItemOnBlockEvent event) {
        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.BLOCK) {
            return;
        }
        if (!event.getItemStack().is(Items.FLINT)) {
            return;
        }
        var ctx = event.getUseOnContext();
        if (ctx.getClickedFace() != Direction.UP) {
            return;
        }
        Level level = ctx.getLevel();
        BlockPos placePos = ctx.getClickedPos().relative(Direction.UP);
        Player player = ctx.getPlayer();
        if (level.isClientSide()) {
            // 与原 mixin 一致：客户端预测成功，不执行放置
            event.cancelWithResult(InteractionResult.SUCCESS);
            return;
        }
        if (placePos.getY() < level.getMinY() || placePos.getY() > level.getMaxY()) {
            return;
        }
        if (level.getBlockState(placePos).canBeReplaced() && player != null) {
            Direction playerFacing = ctx.getHorizontalDirection().getOpposite();
            BlockState flintState = ModBlocks.FLINT.get().defaultBlockState()
                    .setValue(FlintBlock.FACING, playerFacing);
            level.setBlock(placePos, flintState, 3);
            level.playSound(null, placePos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (!player.isCreative()) {
                // BLOCK 相UseOnContext 中物品可为副本，须扣减手上真实格
                InteractionHand hand = event.getHand();
                player.getItemInHand(hand).shrink(1);
            }
            event.cancelWithResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ModItems.HAM_BAT.get())) {
            return;
        }
        float freshness = 100f;
        if (stack.isDamageableItem()) {
            int max = stack.getMaxDamage();
            int dmg = stack.getDamageValue();
            if (max > 0) {
                freshness = 100f * (max - dmg) / (float) max;
            }
        }
        double delta = -HamBatItem.ATTACK_LOSS_PER_FRESHNESS_PERCENT * (100.0 - freshness);
        if (delta == 0.0) {
            return;
        }
        var modifier = new AttributeModifier(FRESHNESS_ATTACK_MODIFIER, delta, AttributeModifier.Operation.ADD_VALUE);
        event.addModifier(Attributes.ATTACK_DAMAGE, modifier, EquipmentSlotGroup.MAINHAND);
    }
}
