package com.theconstant.dontstravemc.block.blockentity.custom;

import com.theconstant.dontstravemc.block.blockentity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Containers;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jetbrains.annotations.Nullable;

/**
 * 单格容器 + 战利品表（结构生成写入）；玩家与漏斗等不可放入物品，仅战利品填充。
 */
public class UrnBlockEntity extends BlockEntity implements RandomizableContainer, ContainerSingleItem.BlockContainerSingleItem {

    private ItemStack item = ItemStack.EMPTY;
    @Nullable
    protected ResourceKey<LootTable> lootTable;
    protected long lootTableSeed;

    public UrnBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.URN_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        if (!this.trySaveLootTable(valueOutput) && !this.item.isEmpty()) {
            valueOutput.store("item", ItemStack.CODEC, this.item);
        }
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        if (!this.tryLoadLootTable(valueInput)) {
            this.item = valueInput.read("item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        } else {
            this.item = ItemStack.EMPTY;
        }
    }

    @Override
    public void setRemoved() {
        if (this.level != null && !this.level.isClientSide()) {
            this.unpackLootTable(null);
            ItemStack stack = this.removeTheItem();
            if (!stack.isEmpty()) {
                Containers.dropItemStack(
                        this.level,
                        this.worldPosition.getX() + 0.5,
                        this.worldPosition.getY(),
                        this.worldPosition.getZ() + 0.5,
                        stack
                );
            }
        }
        super.setRemoved();
    }

    @Nullable
    @Override
    public ResourceKey<LootTable> getLootTable() {
        return this.lootTable;
    }

    @Override
    public void setLootTable(@Nullable ResourceKey<LootTable> lootTable) {
        this.lootTable = lootTable;
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    @Override
    public void setLootTableSeed(long seed) {
        this.lootTableSeed = seed;
    }

    @Override
    public ItemStack getTheItem() {
        this.unpackLootTable(null);
        return this.item;
    }

    @Override
    public ItemStack splitTheItem(int amount) {
        this.unpackLootTable(null);
        ItemStack out = this.item.split(amount);
        if (this.item.isEmpty()) {
            this.item = ItemStack.EMPTY;
        }
        this.setChanged();
        return out;
    }

    @Override
    public void setTheItem(ItemStack stack) {
        this.unpackLootTable(null);
        this.item = stack;
        this.setChanged();
    }

    @Override
    public BlockEntity getContainerBlockEntity() {
        return this;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
