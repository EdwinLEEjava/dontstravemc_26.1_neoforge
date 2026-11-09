package com.theconstant.dontstravemc.item.custom;

import net.minecraft.world.item.Item;

/**
 * 自定义火把物品。此前通过 {@code Item#allowComponentsUpdateAnimation} 在仅数据组件变化时避免重置手持动画；
 * 该方法在 26.1 中已移除，行为与原版火把一致。
 */
public class TorchItem extends Item {

    public TorchItem(Properties properties) {
        super(properties);
    }
}
