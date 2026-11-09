package com.theconstant.dontstravemc.mixin;

import com.theconstant.dontstravemc.item.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFoliageArmorMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"), order = 100)
    private void dontstravemc$foliageChestDurabilityFirst(
            ServerLevel level,
            DamageSource source,
            float amount,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (amount <= 0.0F) return;
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) return;
        if (self.isInvulnerableTo(level, source)) return;
        if (self instanceof Player player) {
            if (player.isSpectator() || player.getAbilities().invulnerable) return;
        }
        ItemStack chest = self.getItemBySlot(EquipmentSlot.CHEST);
        float armorFrac = ModItems.foliageArmorDamageFraction(chest);
        if (armorFrac < 0.0F) return;
        int armorLoss = Mth.ceil(amount * armorFrac);
        if (armorLoss <= 0 || !chest.isDamageableItem()) return;
        chest.hurtAndBreak(armorLoss, self, EquipmentSlot.CHEST);
    }

    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, ordinal = 0, order = 200)
    private float dontstravemc$foliageChestScalePlayerDamage(float amount, ServerLevel level, DamageSource source) {
        if (amount <= 0.0F) return amount;
        LivingEntity self = (LivingEntity) (Object) this;
        ItemStack chest = self.getItemBySlot(EquipmentSlot.CHEST);
        float playerFrac = ModItems.foliagePlayerDamageFraction(chest);
        if (playerFrac < 0.0F) return amount;
        return amount * playerFrac;
    }

    @ModifyVariable(method = "doHurtEquipment", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private EquipmentSlot[] dontstravemc$skipVanillaChestDurabilityForFoliage(
            EquipmentSlot[] slots,
            DamageSource source,
            float amount
    ) {
        if (slots == null || slots.length == 0) return slots;
        LivingEntity self = (LivingEntity) (Object) this;
        ItemStack chest = self.getItemBySlot(EquipmentSlot.CHEST);
        if (!ModItems.isFoliageChestplate(chest)) return slots;
        boolean hasChest = false;
        for (EquipmentSlot slot : slots) {
            if (slot == EquipmentSlot.CHEST) {
                hasChest = true;
                break;
            }
        }
        if (!hasChest) return slots;
        int kept = 0;
        for (EquipmentSlot slot : slots) {
            if (slot != EquipmentSlot.CHEST) kept++;
        }
        if (kept == slots.length) return slots;
        EquipmentSlot[] out = new EquipmentSlot[kept];
        int i = 0;
        for (EquipmentSlot slot : slots) {
            if (slot != EquipmentSlot.CHEST) out[i++] = slot;
        }
        return out;
    }
}
