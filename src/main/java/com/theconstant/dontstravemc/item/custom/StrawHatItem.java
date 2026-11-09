package com.theconstant.dontstravemc.item.custom;

import com.theconstant.dontstravemc.item.ModArmorMaterials;
import com.theconstant.dontstravemc.model.StrawHatGeoModel;
import com.theconstant.dontstravemc.sound.ModSoundEvents;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.util.GeckoLibUtil;

/**
 * 草帽：头盔槽 GeckoLib 模型；佩戴磨损由 {@link com.theconstant.dontstravemc.equipmentwear.EquipmentWearRegistry} 驱动（仅戴在头上计时，约 5 游戏日后消失）。
 */
public class StrawHatItem extends Item implements GeoItem {

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public StrawHatItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties(Properties base) {
        Holder<net.minecraft.sounds.SoundEvent> equipSound = ModSoundEvents.PICKUP_ITEM;
        Equippable equippable = new Equippable(
                EquipmentSlot.HEAD,
                equipSound,
                Optional.of(ModArmorMaterials.STRAW_HAT_ASSET),
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

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            @Nullable
            private GeoArmorRenderer<StrawHatItem, ?> renderer;

            @Override
            public GeoArmorRenderer<?, ?> getGeoArmorRenderer(ItemStack stack, EquipmentSlot slot) {
                if (slot != EquipmentSlot.HEAD) {
                    return null;
                }
                if (this.renderer == null) {
                    this.renderer = new GeoArmorRenderer<>(new StrawHatGeoModel());
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
