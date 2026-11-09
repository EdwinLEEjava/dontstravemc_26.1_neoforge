package com.theconstant.dontstravemc.entity.util.stateutil;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;

public interface Eater {
    SoundEvent getEatingSound();


    default int getEatingDuration() { return 40; }

    default int getEatingSoundInterval() { return 20; }
    default int getPreEatingDuration() { return 10; }

    void setEating(boolean eating);
    void setEatingItem(ItemStack stack);
}
