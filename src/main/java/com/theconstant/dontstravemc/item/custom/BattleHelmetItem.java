package com.theconstant.dontstravemc.item.custom;

import com.theconstant.dontstravemc.model.BattleHelmetGeoModel;
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
 * 战斗头盔：只能戴在头上，没有护甲值、耐久或额外效果。
 */
public class BattleHelmetItem extends Item implements GeoItem {

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public BattleHelmetItem(Properties properties) {
        super(properties);
    }

    public static Properties createProperties(Properties base) {
        Holder<net.minecraft.sounds.SoundEvent> equipSound = ModSoundEvents.PICKUP_ITEM;
        Equippable equippable = new Equippable(
                EquipmentSlot.HEAD,
                equipSound,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                true,
                true,
                false,
                false,
                false,
                equipSound
        );
        return base.stacksTo(1).component(DataComponents.EQUIPPABLE, equippable);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            @Nullable
            private GeoArmorRenderer<BattleHelmetItem, ?> renderer;

            @Override
            public GeoArmorRenderer<?, ?> getGeoArmorRenderer(ItemStack stack, EquipmentSlot slot) {
                if (slot != EquipmentSlot.HEAD) {
                    return null;
                }
                if (this.renderer == null) {
                    this.renderer = new GeoArmorRenderer<>(new BattleHelmetGeoModel());
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
