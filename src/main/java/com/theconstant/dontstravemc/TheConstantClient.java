package com.theconstant.dontstravemc;

import com.theconstant.dontstravemc.block.blockentity.ModBlockEntities;
import com.theconstant.dontstravemc.effect.client.MoleWormParticleManager;
import com.theconstant.dontstravemc.entity.ModEntities;
import com.theconstant.dontstravemc.entity.animal.MoleWormEntity;
import com.theconstant.dontstravemc.render.ConstantBirchLeavesColors;
import com.theconstant.dontstravemc.render.ConstantPortalRenderType;
import com.theconstant.dontstravemc.render.ConstantGrassBlockColors;
import com.theconstant.dontstravemc.render.ConstantGrassColors;
import com.theconstant.dontstravemc.render.UmbelledRockJasmineColors;
import com.theconstant.dontstravemc.render.renderer.BirchFruitSpriteRenderer;
import com.theconstant.dontstravemc.render.renderer.ButterflyRenderer;
import com.theconstant.dontstravemc.render.renderer.ConstantPortalRenderer;
import com.theconstant.dontstravemc.render.renderer.CreepyEyesRenderer;
import com.theconstant.dontstravemc.render.renderer.DarkWatcherRenderer;
import com.theconstant.dontstravemc.render.renderer.MoleWormRenderer;
import com.theconstant.dontstravemc.render.renderer.MonsterSpiderRenderer;
import com.theconstant.dontstravemc.render.renderer.PigmanRenderer;
import com.theconstant.dontstravemc.render.renderer.RabbitRenderer;
import com.theconstant.dontstravemc.sound.client.ConstantRealmMusicManager;
import com.theconstant.dontstravemc.sound.client.MoleWormSoundManager;
import com.theconstant.dontstravemc.sound.client.MoleWormTravelLoopSoundInstance;
import com.theconstant.dontstravemc.status.SanityHudOverlay;
import com.theconstant.dontstravemc.status.SanityVisualHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod(value = TheConstant.MODID, dist = Dist.CLIENT)
public class TheConstantClient {

    private static final Set<UUID> MOLE_WORM_TRAVEL_PLAYING = new HashSet<>();

    public static final ModelLayerLocation MONSTER_SPIDER_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("dontstravemc", "monster_spider"), "main");

    public TheConstantClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(TheConstantClient::onRegisterRenderers);
        modEventBus.addListener(ConstantPortalRenderType::registerPipeline);
        modEventBus.addListener(com.theconstant.dontstravemc.render.sky.ConstantSkyRenderer::registerPipeline);
        modEventBus.addListener(com.theconstant.dontstravemc.render.sky.ConstantSkyRenderer::registerSkybox);
        modEventBus.addListener((net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent event) ->
                event.registerSpriteSet(com.theconstant.dontstravemc.particle.ModParticles.PORTAL_WHIRL.get(),
                        com.theconstant.dontstravemc.particle.client.PortalWhirlParticle.Provider::new));
        modEventBus.addListener((net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent event) ->
                event.registerSpriteSet(com.theconstant.dontstravemc.particle.ModParticles.RAIN_SPLASH.get(),
                        net.minecraft.client.particle.WaterDropParticle.Provider::new));
        modEventBus.addListener(TheConstantClient::onRegisterLayerDefinitions);
        modEventBus.addListener(TheConstantClient::onRegisterBlockColors);
        modEventBus.addListener(TheConstantClient::onClientSetup);
        NeoForge.EVENT_BUS.addListener(TheConstantClient::onClientTickPost);
        NeoForge.EVENT_BUS.addListener(TheConstantClient::onItemTooltip);
        NeoForge.EVENT_BUS.addListener(TheConstantClient::onRenderGuiPost);
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT, com.theconstant.dontstravemc.config.ClientConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // butterfly now renders via GeckoLib (geckolib/models/entity/butterfly.geo.json); no vanilla layer needed
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MONSTER_SPIDER.get(), MonsterSpiderRenderer::new);
        event.registerEntityRenderer(ModEntities.BUTTERFLY.get(), ButterflyRenderer::new);
        event.registerEntityRenderer(ModEntities.RABBIT.get(), RabbitRenderer::new);
        event.registerEntityRenderer(ModEntities.PIGMAN.get(), PigmanRenderer::new);
        event.registerEntityRenderer(ModEntities.TRAP.get(), com.theconstant.dontstravemc.render.renderer.TrapRenderer::new);
        event.registerEntityRenderer(ModEntities.MOLE_WORM.get(), MoleWormRenderer::new);
        event.registerEntityRenderer(ModEntities.DARK_WATCHER.get(), DarkWatcherRenderer::new);
        event.registerEntityRenderer(ModEntities.CREEPY_EYES.get(), CreepyEyesRenderer::new);
        event.registerEntityRenderer(ModEntities.BIRCH_FRUIT_SPRITE.get(), BirchFruitSpriteRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CONSTANT_PORTAL.get(), ConstantPortalRenderer::new);
    }

    private static void onRegisterBlockColors(RegisterColorHandlersEvent.BlockTintSources event) {
        ConstantBirchLeavesColors.register(event);
        ConstantGrassColors.register(event);
        ConstantGrassBlockColors.register(event);
        UmbelledRockJasmineColors.register(event);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            TheConstant.LOGGER.info("HELLO FROM CLIENT SETUP");
            TheConstant.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        });
    }

    private static void onItemTooltip(ItemTooltipEvent event) {
        var id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (id != null && id.getNamespace().equals("dontstravemc")) {
            event.getToolTip().add(Component.translatable("tooltip." + id.getNamespace() + "." + id.getPath()));
        }
    }

    private static void onRenderGuiPost(RenderGuiEvent.Post event) {
        SanityHudOverlay.INSTANCE.onHudRender(event.getGuiGraphics(), Minecraft.getInstance().getDeltaTracker());
    }

    private static void onClientTickPost(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        SanityVisualHandler.update();
        ConstantRealmMusicManager.tick(client);

        if (client.level == null || client.player == null || client.getSoundManager() == null) {
            MOLE_WORM_TRAVEL_PLAYING.clear();
            return;
        }

        Set<UUID> aliveAndLooping = new HashSet<>();
        for (MoleWormEntity mole : client.level.getEntitiesOfClass(MoleWormEntity.class, client.player.getBoundingBox().inflate(96.0D))) {
            UUID id = mole.getUUID();
            if (mole.isInWalkLoopAnim()) {
                aliveAndLooping.add(id);
                if (!MOLE_WORM_TRAVEL_PLAYING.contains(id)) {
                    client.getSoundManager().play(new MoleWormTravelLoopSoundInstance(mole));
                    MOLE_WORM_TRAVEL_PLAYING.add(id);
                }
            }
            MoleWormParticleManager.tick(mole);
            MoleWormSoundManager.tick(mole);
        }

        MOLE_WORM_TRAVEL_PLAYING.retainAll(aliveAndLooping);
    }
}
