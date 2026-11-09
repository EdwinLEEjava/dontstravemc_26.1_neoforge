package com.theconstant.dontstravemc;

import com.theconstant.dontstravemc.attachment.ModAttachments;
import com.theconstant.dontstravemc.block.ModBlocks;
import com.theconstant.dontstravemc.block.blockentity.ModBlockEntities;
import com.theconstant.dontstravemc.command.ModCommands;
import com.theconstant.dontstravemc.entity.ModEntities;
import com.theconstant.dontstravemc.entity.ambient.CreepyEyesSpawnListener;
import com.theconstant.dontstravemc.entity.ambient.DarkWatcherSpawnListener;
import com.theconstant.dontstravemc.entity.effect.MoleWormBurrowCrackManager;
import com.theconstant.dontstravemc.equipmentwear.EquipmentWearItems;
import com.theconstant.dontstravemc.equipmentwear.EquipmentWearTickHandler;
import com.theconstant.dontstravemc.item.FertilizerHelper;
import com.theconstant.dontstravemc.item.ModItemGroups;
import com.theconstant.dontstravemc.item.ModItems;
import com.theconstant.dontstravemc.item.custom.ModCompostables;
import com.theconstant.dontstravemc.item.custom.ModFuelItem;
import com.theconstant.dontstravemc.item.custom.TorchLightHandler;
import com.theconstant.dontstravemc.sound.ModSoundEvents;
import com.theconstant.dontstravemc.event.charlieattack.CharlieAttackListener;
import com.theconstant.dontstravemc.status.sanity.SanityLogicHandler;
import com.theconstant.dontstravemc.world.gen.ModFeatures;
import com.theconstant.dontstravemc.world.gen.island.IslandWorldgen;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TheConstant.MODID)
public class TheConstant {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "dontstravemc";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "dontstravemc" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "dontstravemc" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "dontstravemc" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, MODID);

    static {
        try {
            Class.forName("com.theconstant.dontstravemc.block.ModBlocks", true, TheConstant.class.getClassLoader());
            Class.forName("com.theconstant.dontstravemc.entity.ModEntities", true, TheConstant.class.getClassLoader());
            Class.forName("com.theconstant.dontstravemc.block.blockentity.ModBlockEntities", true, TheConstant.class.getClassLoader());
            Class.forName("com.theconstant.dontstravemc.item.ModItems", true, TheConstant.class.getClassLoader());
            // 将创造模式标签登记进 CREATIVE_MODE_TABS，须在 CREATIVE_MODE_TABS.register(modEventBus) 之前完成
            Class.forName("com.theconstant.dontstravemc.item.ModItemGroups", true, TheConstant.class.getClassLoader());
            Class.forName("com.theconstant.dontstravemc.world.gen.ModFeatures", true, TheConstant.class.getClassLoader());
            Class.forName("com.theconstant.dontstravemc.world.gen.island.IslandWorldgen", true, TheConstant.class.getClassLoader());
            Class.forName("com.theconstant.dontstravemc.world.gen.structure.ModStructures", true, TheConstant.class.getClassLoader());
            ModSoundEvents.registerSounds();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public TheConstant(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        FEATURES.register(modEventBus);
        com.theconstant.dontstravemc.particle.ModParticles.PARTICLE_TYPES.register(modEventBus);
        IslandWorldgen.BIOME_SOURCES.register(modEventBus);
        IslandWorldgen.CHUNK_GENERATORS.register(modEventBus);
        com.theconstant.dontstravemc.world.gen.structure.ModStructures.STRUCTURE_TYPES.register(modEventBus);
        com.theconstant.dontstravemc.world.gen.structure.ModStructures.STRUCTURE_PIECES.register(modEventBus);
        ModSoundEvents.SOUND_EVENTS.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (THECONSTANT) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        bootstrapModContent();
    }

    /** 模组内容注册与事件订阅入口（方块/物品/实体/世界生成与装备损耗等）。 */
    private static void bootstrapModContent() {
        ModItems.registerModItems();
        ModBlocks.registerModBlocks();
        ModBlockEntities.registerBlockEntities();
        ModItemGroups.buildTabs();
        ModItemGroups.init();
        ModEntities.register();
        ModFeatures.registerModFeatures();
        IslandWorldgen.register();
        com.theconstant.dontstravemc.world.CaveTransitions.register();
        com.theconstant.dontstravemc.world.gen.structure.ModStructures.register();
        ModFuelItem.registerFuels();
        ModCompostables.registerCompostables();
        ModCommands.register();
        SanityLogicHandler.register();
        CharlieAttackListener.register();
        EquipmentWearItems.register();
        EquipmentWearTickHandler.register();
        MoleWormBurrowCrackManager.register();
        FertilizerHelper.registerFertilizers();
        TorchLightHandler.register();
        DarkWatcherSpawnListener.register();
        CreepyEyesSpawnListener.register();

        LOGGER.info("Hello The Constant!");
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
