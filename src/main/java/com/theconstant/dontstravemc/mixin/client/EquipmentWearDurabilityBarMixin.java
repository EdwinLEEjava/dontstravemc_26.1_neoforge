package com.theconstant.dontstravemc.mixin.client;

import com.theconstant.dontstravemc.equipmentwear.EquipmentWearBar;
import com.theconstant.dontstravemc.equipmentwear.EquipmentWearRegistry;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 为 {@link EquipmentWearRegistry} 中的物品显示与原版耐久条同色、同宽算法的进度条。
 */
@Mixin(ItemStack.class)
public class EquipmentWearDurabilityBarMixin {

    @Inject(method = "isBarVisible", at = @At("RETURN"), cancellable = true)
    private void dontstravemc$wearBarVisible(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            return;
        }
        ItemStack self = (ItemStack) (Object) this;
        if (!EquipmentWearRegistry.wearsOn(self.getItem())) {
            return;
        }
        if (EquipmentWearBar.shouldShowBar(self)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getBarWidth", at = @At("RETURN"), cancellable = true)
    private void dontstravemc$wearBarWidth(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (!EquipmentWearRegistry.wearsOn(self.getItem())) {
            return;
        }
        if (self.isDamageableItem()) {
            return;
        }
        cir.setReturnValue(EquipmentWearBar.barWidth(self));
    }

    @Inject(method = "getBarColor", at = @At("RETURN"), cancellable = true)
    private void dontstravemc$wearBarColor(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (!EquipmentWearRegistry.wearsOn(self.getItem())) {
            return;
        }
        if (self.isDamageableItem()) {
            return;
        }
        cir.setReturnValue(EquipmentWearBar.barColor(self));
    }
}
