package com.theconstant.dontstravemc.sound;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 模组全部自定义 SoundEvent 的唯一定义与注册处。
 */
public final class ModSoundEvents {

    public static final String MOD_ID = "dontstravemc";

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, MOD_ID);

    // --- 怪物：蜘蛛 ---

    public static final DeferredHolder<SoundEvent, SoundEvent> MONSTER_SPIDER_ATTACK = register("monster_spider_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> MONSTER_SPIDER_STEP = register("monster_spider_step");
    public static final DeferredHolder<SoundEvent, SoundEvent> MONSTER_SPIDER_DEATH = register("monster_spider_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> MONSTER_SPIDER_SCREAM = register("monster_spider_scream");
    public static final DeferredHolder<SoundEvent, SoundEvent> MONSTER_SPIDER_EAT = register("monster_spider_eat");
    public static final DeferredHolder<SoundEvent, SoundEvent> MONSTER_SPIDER_SLEEPING = register("monster_spider_sleeping");
    public static final DeferredHolder<SoundEvent, SoundEvent> MONSTER_SPIDER_JUMP = register("monster_spider_jump");
    public static final DeferredHolder<SoundEvent, SoundEvent> MONSTER_SPIDER_WAKEUP = register("monster_spider_wakeup");
    public static final DeferredHolder<SoundEvent, SoundEvent> MONSTER_SPIDER_HURT = register("monster_spider_hurt");

    // --- 猪人 ---

    public static final DeferredHolder<SoundEvent, SoundEvent> PIGMAN_AMBIENT = register("pigman_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIGMAN_EAT = register("pigman_eat");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIGMAN_ATTACK = register("pigman_attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIGMAN_DEATH = register("pigman_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIGMAN_SLEEP = register("pigman_sleep");

    // --- 兔子 ---

    public static final DeferredHolder<SoundEvent, SoundEvent> RABBIT_SCREAM = register("rabbit_scream");
    public static final DeferredHolder<SoundEvent, SoundEvent> RABBIT_HOP = register("rabbit_hop");

    // --- 鼹鼠虫 ---

    public static final DeferredHolder<SoundEvent, SoundEvent> MOLE_WORM_DEATH = register("mole_worm_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLE_WORM_DIG = register("mole_worm_dig");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLE_WORM_EMERGE = register("mole_worm_emerge");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLE_WORM_HURT = register("mole_worm_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLE_WORM_JUMP = register("mole_worm_jump");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLE_WORM_PICKUP = register("mole_worm_pickup");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLE_WORM_SNIFF = register("mole_worm_sniff");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOLE_WORM_TRAVEL = register("mole_worm_travel");

    // --- 理智 / Charlie ---

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DUSK_GONECRAZIER = register("music_dusk_gonecrazier");
    public static final DeferredHolder<SoundEvent, SoundEvent> SANITY_LOOP1 = register("sanity_loop1");
    public static final DeferredHolder<SoundEvent, SoundEvent> SANITY_LOOP2 = register("sanity_loop2");
    public static final DeferredHolder<SoundEvent, SoundEvent> SANITY_RANDOM = register("sanity_random");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAZY_EXTRA = register("crazy_extra");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHARLIE_WARN = register("charlie_warn");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHARLIE_BITE = register("charlie_bite");

    // --- 永恒领域环境 / 战斗 / 菜单音乐 ---

    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DAWN = register("music_dawn");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_DUSK = register("music_dusk");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_FALL_BOSS = register("music_fall_boss");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_FALL_DANGER = register("music_fall_danger");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_FALL_DANGER_BED = register("music_fall_danger_bed");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_FALL_WORK = register("music_fall_work");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSIC_MENU_DST = register("music_menu_dst");

    // --- 玩家通用（拾取、装备等）---

    public static final DeferredHolder<SoundEvent, SoundEvent> PICKUP_ITEM = register("player_pickup_item");

    private ModSoundEvents() {
    }

    public static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    /**
     * 供 TheConstant 模组主类静态块显式加载本类，使上述静态字段完成向 {@link #SOUND_EVENTS} 的登记。
     */
    public static void registerSounds() {
    }
}
