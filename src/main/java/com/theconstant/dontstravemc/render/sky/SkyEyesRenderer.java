package com.theconstant.dontstravemc.render.sky;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.theconstant.dontstravemc.TheConstant;
import com.theconstant.dontstravemc.attachment.ModAttachments;
import com.theconstant.dontstravemc.world.time.ConstantRealmTime;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

/**
 * 低理智时远景树林里的眼睛：在群系剪影之上，地平线附近一圈会出现、眨眼、消失的眼睛。
 * <p>
 * 纯客户端效果，规则照搬诡异之眼实体（{@code CreepyEyesSpawner} / {@code CreepyEyesEntity}）：
 * <ul>
 *   <li>只在夜里、理智低于 80%（160/200）时出现；创造/旁观模式不出现</li>
 *   <li>每 5–7 秒尝试刷新一次，同时最多 7 对；每对存活 20–30 秒，天亮立刻离开</li>
 *   <li>三种眼睛（贴图与实体共用 creepyeyes_1/2/3），理智越低越容易出现第 1 种</li>
 *   <li>动画与实体相同：0.5 秒睁开 → 每 3 秒眨一次 → 0.25 秒闭上消失</li>
 * </ul>
 * 眼睛按剪影的像素尺度画（1 纹素 = 360°/2048 ≈ 0.176°），中心在仰角 0.8°–3° 的树林高度里，
 * 跟随剪影淡入淡出；没有剪影的群系不出现。用原版天体管线（叠加混合）绘制，暗处发亮。
 */
final class SkyEyesRenderer {

    private static final Identifier[] TEXTURES = {
            Identifier.fromNamespaceAndPath(TheConstant.MODID, "textures/entity/creepyeyes/creepyeyes_1.png"),
            Identifier.fromNamespaceAndPath(TheConstant.MODID, "textures/entity/creepyeyes/creepyeyes_2.png"),
            Identifier.fromNamespaceAndPath(TheConstant.MODID, "textures/entity/creepyeyes/creepyeye_3.png"),
    };

    // 与 CreepyEyesSpawner / CreepyEyesSpawnListener 保持一致
    private static final float SANITY_THRESHOLD = 160.0F;
    private static final int MAX_EYES = 7;
    private static final int MIN_INTERVAL = 100;
    private static final int MAX_INTERVAL = 140;
    private static final int MIN_LIFETIME = 400;
    private static final int MAX_LIFETIME = 600;
    // 与 CreepyEyesEntity / creepeyes.animation.json 保持一致（单位：tick）
    private static final float APPEAR_TICKS = 10.0F;
    private static final float DISAPPEAR_TICKS = 5.0F;
    private static final float BLINK_LOOP = 60.0F;

    /** 贴图里眼睛所在区域：与实体模型的 UV 框相同，14×9 纹素（贴图 32×32）。 */
    private static final float REGION_W = 14.0F;
    private static final float REGION_H = 9.0F;
    private static final float TEX_SIZE = 32.0F;
    /** 与剪影同一像素尺度：360° / 2048。 */
    private static final float TEXEL_DEG = 360.0F / 2048.0F;
    private static final float DISTANCE = 100.0F;
    private static final float MIN_ELEV_DEG = 0.8F;
    private static final float MAX_ELEV_DEG = 3.0F;
    /** 两对眼睛之间至少相隔的方位角。 */
    private static final float MIN_SEPARATION_DEG = 10.0F;

    private static final class Eye {
        float azimuth;
        float elevation;
        int type;
        float age;
        float lifetime;
        float leaveAge = -1.0F;
    }

    private final List<Eye> eyes = new ArrayList<>();
    private final RandomSource random = RandomSource.create();
    private float cooldown;
    private double lastTime = Double.NaN;
    private @Nullable ClientLevel lastLevel;
    private @Nullable GpuBuffer quad;

    void render(Minecraft mc, ClientLevel level, Matrix4fc modelViewMatrix, float partialTick, float silhouetteAlpha,
                GpuTextureView colorTexture, GpuTextureView depthTexture) {
        this.update(mc, level, partialTick, silhouetteAlpha);
        if (this.eyes.isEmpty() || silhouetteAlpha <= 0.001F) {
            return;
        }

        float halfW = (float) Math.tan(Math.toRadians(REGION_W * 0.5F * TEXEL_DEG)) * DISTANCE;
        float halfH = (float) Math.tan(Math.toRadians(REGION_H * 0.5F * TEXEL_DEG)) * DISTANCE;
        GpuBuffer quad = this.getQuad();
        RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = autoIndices.getBuffer(6);

        for (Eye eye : this.eyes) {
            float open = openness(eye);
            if (open <= 0.001F) {
                continue;
            }
            // 在打开 RenderPass 之前取贴图（首次 getTexture 会上传）
            AbstractTexture texture = mc.getTextureManager().getTexture(TEXTURES[eye.type]);
            // +Y 转到眼睛方向：先抬到仰角，再转到方位角；四边形在 XZ 平面，Z 是竖直方向
            Matrix4f pose = new Matrix4f(modelViewMatrix)
                    .rotateY(eye.azimuth)
                    .rotateX(Mth.HALF_PI - eye.elevation)
                    .translate(0.0F, DISTANCE, 0.0F)
                    .scale(halfW, 1.0F, halfH * open);
            Vector4f tint = new Vector4f(1.0F, 1.0F, 1.0F, silhouetteAlpha);
            GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
                    .writeTransform(pose, tint, new Vector3f(), new Matrix4f());
            try (RenderPass pass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(() -> "Constant sky eyes", colorTexture, OptionalInt.empty(), depthTexture, OptionalDouble.empty())) {
                pass.setPipeline(RenderPipelines.CELESTIAL);
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("DynamicTransforms", transforms);
                pass.bindTexture("Sampler0", texture.getTextureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST));
                pass.setVertexBuffer(0, quad);
                pass.setIndexBuffer(indexBuffer, autoIndices.type());
                pass.drawIndexed(0, 0, 6, 1);
            }
        }
    }

    // -------------------------------------------------------------------------

    private void update(Minecraft mc, ClientLevel level, float partialTick, float silhouetteAlpha) {
        double now = level.getGameTime() + partialTick;
        if (level != this.lastLevel) {
            this.eyes.clear();
            this.lastLevel = level;
            this.lastTime = Double.NaN;
        }
        float dt = Double.isNaN(this.lastTime) ? 0.0F : (float) (now - this.lastTime);
        this.lastTime = now;
        if (dt < 0.0F || dt > 40.0F) {
            dt = 0.0F;      // 时间回拨或长时间卡顿：这一帧不推进
        }

        long dayTime = level.getDefaultClockTime();
        boolean day = ConstantRealmTime.isDay(level, dayTime);

        this.cooldown -= dt;
        if (this.cooldown <= 0.0F) {
            this.cooldown = MIN_INTERVAL + this.random.nextInt(MAX_INTERVAL - MIN_INTERVAL + 1);
            LocalPlayer player = mc.player;
            if (player != null && silhouetteAlpha > 0.5F && this.eyes.size() < MAX_EYES
                    && !player.getAbilities().instabuild && !player.isSpectator()
                    && ConstantRealmTime.isNight(level, dayTime)) {
                float sanity = ModAttachments.getSanity(player).getSanity();
                if (sanity < SANITY_THRESHOLD) {
                    this.spawn(sanity);
                }
            }
        }

        for (int i = this.eyes.size() - 1; i >= 0; i--) {
            Eye eye = this.eyes.get(i);
            eye.age += dt;
            if (eye.leaveAge < 0.0F && (eye.age >= eye.lifetime || day)) {
                eye.leaveAge = eye.age;
            }
            if (eye.leaveAge >= 0.0F && eye.age - eye.leaveAge >= DISAPPEAR_TICKS) {
                this.eyes.remove(i);
            }
        }
    }

    private void spawn(float sanity) {
        float azimuth = -1.0F;
        for (int attempt = 0; attempt < 10 && azimuth < 0.0F; attempt++) {
            float candidate = this.random.nextFloat() * Mth.TWO_PI;
            boolean clear = true;
            for (Eye other : this.eyes) {
                float diff = Math.abs(Mth.wrapDegrees((candidate - other.azimuth) * Mth.RAD_TO_DEG));
                if (diff < MIN_SEPARATION_DEG) {
                    clear = false;
                    break;
                }
            }
            if (clear) {
                azimuth = candidate;
            }
        }
        if (azimuth < 0.0F) {
            return;
        }
        Eye eye = new Eye();
        eye.azimuth = azimuth;
        eye.elevation = (MIN_ELEV_DEG + this.random.nextFloat() * (MAX_ELEV_DEG - MIN_ELEV_DEG)) * Mth.DEG_TO_RAD;
        eye.type = this.pickType(sanity);
        eye.lifetime = MIN_LIFETIME + this.random.nextInt(MAX_LIFETIME - MIN_LIFETIME + 1);
        this.eyes.add(eye);
    }

    /** 同 CreepyEyesSpawner.pickTextureIndex：理智 0 → 80/15/5%，理智 160 → 33/33/34%。返回 0–2。 */
    private int pickType(float sanity) {
        float r = Mth.clamp(sanity / SANITY_THRESHOLD, 0.0F, 1.0F);
        float w1 = 0.80F - r * (0.80F - 0.33F);
        float w2 = 0.15F + r * (0.33F - 0.15F);
        float roll = this.random.nextFloat();
        if (roll < w1) {
            return 0;
        }
        return roll < w1 + w2 ? 1 : 2;
    }

    /** 竖直方向的缩放：睁开、眨眼循环、闭上消失，关键帧同 creepeyes.animation.json。 */
    private static float openness(Eye eye) {
        if (eye.leaveAge >= 0.0F) {
            float t = (eye.age - eye.leaveAge) / DISAPPEAR_TICKS;
            float start = eye.leaveAge < APPEAR_TICKS ? eye.leaveAge / APPEAR_TICKS : 1.0F;
            return start * Math.max(0.0F, 1.0F - t);
        }
        if (eye.age < APPEAR_TICKS) {
            return eye.age / APPEAR_TICKS;
        }
        float t = (eye.age - APPEAR_TICKS) % BLINK_LOOP;
        if (t < 25.0F || t >= 35.0F) {
            return 1.0F;
        }
        return t < 30.0F ? 1.0F - (t - 25.0F) / 5.0F : (t - 30.0F) / 5.0F;
    }

    /** 单位四边形，UV 只取贴图左上角 14×9 的眼睛区域；左右翻转，让贴图从天球内侧看是正的。 */
    private GpuBuffer getQuad() {
        if (this.quad == null) {
            float u1 = REGION_W / TEX_SIZE;
            float v1 = REGION_H / TEX_SIZE;
            VertexFormat format = DefaultVertexFormat.POSITION_TEX;
            try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(4 * format.getVertexSize())) {
                BufferBuilder builder = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, format);
                builder.addVertex(-1.0F, 0.0F, -1.0F).setUv(u1, 0.0F);
                builder.addVertex(1.0F, 0.0F, -1.0F).setUv(0.0F, 0.0F);
                builder.addVertex(1.0F, 0.0F, 1.0F).setUv(0.0F, v1);
                builder.addVertex(-1.0F, 0.0F, 1.0F).setUv(u1, v1);
                try (MeshData mesh = builder.buildOrThrow()) {
                    this.quad = RenderSystem.getDevice().createBuffer(() -> "Constant sky eyes quad", 32, mesh.vertexBuffer());
                }
            }
        }
        return this.quad;
    }
}
