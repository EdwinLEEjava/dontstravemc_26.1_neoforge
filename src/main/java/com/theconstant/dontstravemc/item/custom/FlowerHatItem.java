package com.theconstant.dontstravemc.item.custom;

import com.theconstant.dontstravemc.item.ModArmorMaterials;
import com.theconstant.dontstravemc.sound.ModSoundEvents;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.Equippable;

/**
 * 花环：头盔槽装备，理智加成见 {@link com.theconstant.dontstravemc.status.sanity.SanityLogicHandler}。
 */
public class FlowerHatItem extends Item {

    public FlowerHatItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties(Properties base) {
        Holder<net.minecraft.sounds.SoundEvent> equipSound = ModSoundEvents.PICKUP_ITEM;
        Equippable equippable = new Equippable(
                EquipmentSlot.HEAD,
                equipSound,
                Optional.of(ModArmorMaterials.FLOWER_HAT_ASSET),
                Optional.empty(),
                Optional.empty(),
                true,
                true,
                false,
                false,
                false,
                equipSound
        );
        return base.stacksTo(1)
                .enchantable(15)
                .component(DataComponents.EQUIPPABLE, equippable);
    }
}
