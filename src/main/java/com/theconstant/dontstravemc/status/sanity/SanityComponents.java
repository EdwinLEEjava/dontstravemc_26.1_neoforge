package com.theconstant.dontstravemc.status.sanity;

/**
 * 玩家理智逻辑接口（由 {@link com.theconstant.dontstravemc.attachment.SanityAttachmentData} 实现）。
 */
public interface SanityComponents {
    float getSanity();

    void setSanity(float value);

    void addSanity(float value);

    float getEnvironmentalRate();

    void setEnvironmentalRate(float rate);

    void applyContinuousChange();
}
