package com.theconstant.dontstravemc.render.state;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jetbrains.annotations.Nullable;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;

import java.util.Map;

public class DarkWatcherRenderState extends LivingEntityRenderState implements GeoRenderState {
    private final Map<DataTicket<?>, Object> dataMap = new Reference2ObjectOpenHashMap<>();

    @Override
    public <D> void addGeckolibData(DataTicket<D> dataTicket, @Nullable D data) {
        this.dataMap.put(dataTicket, data);
    }

    @Override
    public boolean hasGeckolibData(DataTicket<?> dataTicket) {
        return this.dataMap.containsKey(dataTicket);
    }

    @Nullable
    @Override
    public <D> D getGeckolibData(DataTicket<D> dataTicket) {
        return (D) this.dataMap.get(dataTicket);
    }

    @Override
    public Map<DataTicket<?>, Object> getDataMap() {
        return this.dataMap;
    }
}
