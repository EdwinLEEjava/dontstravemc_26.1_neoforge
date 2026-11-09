package com.theconstant.dontstravemc.item.custom;

import com.theconstant.dontstravemc.entity.ModEntities;
import com.theconstant.dontstravemc.entity.trap.TrapEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TrapItem extends Item {
    
    public TrapItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        
        if (!level.isClientSide()) {
            BlockPos pos = context.getClickedPos();
            Direction direction = context.getClickedFace();
            BlockPos placePos = pos.relative(direction);

            TrapEntity trap = new TrapEntity(ModEntities.TRAP.get(), level);
            trap.setPos(Vec3.atBottomCenterOf(placePos));

            ItemStack stack = context.getItemInHand();
            int damage = stack.getDamageValue();
            int usesLeft = 8 - damage;
            trap.setUsesLeft(usesLeft);
            
            // 朝向放置陷阱的玩家
            if (context.getPlayer() != null) {
                trap.lookAt(context.getPlayer().getEyePosition());
            }
            
            level.addFreshEntity(trap);

            if (context.getPlayer() != null) {
                context.getPlayer().swing(context.getHand());
            }
            
            // Consume item
            if (!context.getPlayer().isCreative()) {
                stack.shrink(1);
            }
            
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.SUCCESS;
    }
}
