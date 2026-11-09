package com.theconstant.dontstravemc.entity.ai.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * 不把活板门路径类型（{@link PathType#TRAPDOOR}、{@link PathType#ON_TOP_OF_TRAPDOOR}）当作有效路径节点，避免猪人把窗户当通道顶墙。
 */
public class PigmanGroundPathNavigation extends GroundPathNavigation {

    public PigmanGroundPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected boolean hasValidPathType(PathType type) {
        if (type == PathType.TRAPDOOR || type == PathType.ON_TOP_OF_TRAPDOOR) {
            return false;
        }
        return super.hasValidPathType(type);
    }
}
