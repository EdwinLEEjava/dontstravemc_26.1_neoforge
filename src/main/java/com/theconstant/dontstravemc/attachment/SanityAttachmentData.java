package com.theconstant.dontstravemc.attachment;

import com.theconstant.dontstravemc.status.sanity.SanityComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * 玩家理智数据（NeoForge Attachment）。磁盘格式与旧 CCA 一致：仅持久化 {@code sanity_value}；环境变化率不落盘。
 */
public final class SanityAttachmentData implements SanityComponents, ValueIOSerializable {
    public static final StreamCodec<RegistryFriendlyByteBuf, SanityAttachmentData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            SanityAttachmentData::getSanity,
            ByteBufCodecs.FLOAT,
            SanityAttachmentData::getEnvironmentalRate,
            SanityAttachmentData::new);

    private float sanity = 200.0f;
    private float environmentalRate = 0.0f;

    public SanityAttachmentData() {}

    public SanityAttachmentData(float sanity, float environmentalRate) {
        this.sanity = Mth.clamp(sanity, 0.0f, 200.0f);
        this.environmentalRate = environmentalRate;
    }

    @Override
    public float getSanity() {
        return this.sanity;
    }

    @Override
    public float getEnvironmentalRate() {
        return this.environmentalRate;
    }

    @Override
    public void setSanity(float value) {
        this.sanity = Mth.clamp(value, 0.0f, 200.0f);
    }

    @Override
    public void addSanity(float value) {
        setSanity(this.sanity + value);
    }

    @Override
    public void setEnvironmentalRate(float rate) {
        this.environmentalRate = rate;
    }

    @Override
    public void applyContinuousChange() {
        if (this.environmentalRate != 0) {
            addSanity(this.environmentalRate / 20.0f);
        }
    }

    @Override
    public void serialize(ValueOutput output) {
        if (output instanceof TagValueOutput tagOutput) {
            tagOutput.putFloat("sanity_value", this.sanity);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        if (input instanceof TagValueInput tagInput) {
            this.sanity = tagInput.getFloatOr("sanity_value", 200.0f);
            this.environmentalRate = 0.0f;
        }
    }
}
