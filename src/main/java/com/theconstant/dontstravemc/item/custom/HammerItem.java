package com.theconstant.dontstravemc.item.custom;

import net.minecraft.world.item.Item;

/**
 * 锤子：挖掘规则与耐久由 {@link net.minecraft.world.item.Item.Properties#tool} 与
 * {@code dontstravemc:tags/block/mineable/hammer.json} 数据包驱动。
 */
public class HammerItem extends Item {

    public HammerItem(Item.Properties properties) {
        super(properties);
    }
}
