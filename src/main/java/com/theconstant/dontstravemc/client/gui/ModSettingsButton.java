package com.theconstant.dontstravemc.client.gui;

import com.theconstant.dontstravemc.TheConstant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

/**
 * 在原版「选项」界面左下角加一个「永恒领域设置」按钮，直接打开本模组的配置界面
 * （与「模组列表 → 配置」是同一个界面）。
 */
@EventBusSubscriber(modid = TheConstant.MODID, value = Dist.CLIENT)
public final class ModSettingsButton {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 20;
    private static final int MARGIN = 6;

    private ModSettingsButton() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof OptionsScreen screen)) {
            return;
        }
        event.addListener(Button.builder(Component.translatable("options.dontstravemc.settings"), button -> open(screen))
                .bounds(MARGIN, screen.height - HEIGHT - MARGIN, WIDTH, HEIGHT)
                .build());
    }

    private static void open(Screen parent) {
        ModList.get().getModContainerById(TheConstant.MODID)
                .ifPresent(container -> Minecraft.getInstance().setScreen(new ConfigurationScreen(container, parent)));
    }
}
