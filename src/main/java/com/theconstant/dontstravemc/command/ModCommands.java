package com.theconstant.dontstravemc.command;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class ModCommands {

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ModCommands::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        SanityCommand.register(dispatcher);
    }
}