package com.theconstant.dontstravemc.item.custom;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * 火腿棒：原版耐久（约 95 次命中耗尽）；攻击随剩余耐久比例下降。
 */
public class HamBatItem extends Item {

    /** 每低于满耐久对应「新鲜度」1%，攻击加成减少该数值（加法修正） */
    public static final float ATTACK_LOSS_PER_FRESHNESS_PERCENT = 0.04f;

    /** 与命中次数相当的耐久上限（贴近燧石矛量级） */
    public static final int MAX_USES = 95;

    public HamBatItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties(Properties base) {
        HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        return base.stacksTo(1)
                .durability(MAX_USES)
                .enchantable(5)
                .component(
                        DataComponents.TOOL,
                        new Tool(
                                List.of(
                                        Tool.Rule.minesAndDrops(HolderSet.direct(Blocks.COBWEB.builtInRegistryHolder()), 15.0F),
                                        Tool.Rule.overrideSpeed(blocks.getOrThrow(BlockTags.SWORD_INSTANTLY_MINES), Float.MAX_VALUE),
                                        Tool.Rule.overrideSpeed(blocks.getOrThrow(BlockTags.SWORD_EFFICIENT), 1.5F)
                                ),
                                1.0F,
                                2,
                                false
                        ))
                .attributes(
                        ItemAttributeModifiers.builder()
                                .add(
                                        Attributes.ATTACK_DAMAGE,
                                        new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 13.0, AttributeModifier.Operation.ADD_VALUE),
                                        EquipmentSlotGroup.MAINHAND
                                )
                                .add(
                                        Attributes.ATTACK_SPEED,
                                        new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE),
                                        EquipmentSlotGroup.MAINHAND
                                )
                                .build()
                )
                .component(DataComponents.WEAPON, new Weapon(0));
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (attacker.level().isClientSide()) {
            return;
        }
        if (attacker instanceof Player player) {
            InteractionHand hand = player.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            stack.hurtAndBreak(1, player, hand);
        }
    }
}
