package com.theconstant.dontstravemc.render.sky;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
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
import com.theconstant.dontstravemc.config.ClientConfig;
import com.theconstant.dontstravemc.world.time.ConstantRealmTime;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.client.CloudStatus;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.CustomCloudsRenderer;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;
import net.neoforged.neoforge.client.event.RegisterCustomEnvironmentEffectRendererEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

/**
 * 永恒领域的手绘风天空：渐变天穹、水彩斑块、纸纹、方形星星与太阳。
 * <p>
 * 通过 NeoForge 的 {@code neoforge:custom_skybox} 环境属性启用：dimension_type 里写
 * {@code "neoforge:custom_skybox": "dontstravemc:constant_sky"}。返回 true 时原版的日月星和天空盘都不再绘制。
 * <p>
 * 几何是一个包住相机的立方体，片元着色器用插值后的顶点位置当作视线方向，所以与立方体形状无关。
 * 每帧参数塞进 DynamicTransforms 的 {@code TextureMat}（天空不用纹理矩阵），免去自定义 UBO：
 * <pre>
 * col0 = 天顶 rgb, 时间(秒)
 * col1 = 地平线 rgb, 纸纹强度
 * col2 = 地平线以下 rgb, 水彩斑块强度
 * col3 = 地平线辉光, 星星可见度, 太阳方向 x, 太阳方向 y
 * ModelOffset = 太阳可见度, 太阳方向 z, 云量（平滑后的雨量）；ColorModulator = 月亮方向 xyz, 月亮可见度；
 * Sampler0 = 噪声贴图；Sampler1/2 = 远/近云带；Sampler3 = 雨天压顶云层
 * 太阳、月亮本体用原版贴图另行绘制并染色，见 renderSun / renderMoon
 * </pre>
 */
public final class ConstantSkyRenderer implements CustomSkyboxRenderer {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(TheConstant.MODID, "constant_sky");
    /** dimension_type 里 {@code "neoforge:custom_clouds": "dontstravemc:no_clouds"}。 */
    public static final Identifier NO_CLOUDS_ID = Identifier.fromNamespaceAndPath(TheConstant.MODID, "no_clouds");

    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(TheConstant.MODID, "pipeline/constant_sky"))
            .withVertexShader(Identifier.fromNamespaceAndPath(TheConstant.MODID, "core/constant_sky"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(TheConstant.MODID, "core/constant_sky"))
            .withSampler("Sampler0")
            .withSampler("Sampler1")
            .withSampler("Sampler2")
            .withSampler("Sampler3")
            .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
            .withCull(false)
            .build();

    /** 着色器的值噪声来源：R = N(x,y)，G = N(x+37,y+17)，需线性过滤 + 重复寻址。 */
    private static final Identifier NOISE_TEXTURE = Identifier.fromNamespaceAndPath(TheConstant.MODID, "textures/environment/sky_noise.png");

    /** 手绘云带：远层 / 近层，规格见 docs/sky-art-asset-spec.md。像素画，用最近邻采样。 */
    private static final Identifier CLOUDS_FAR = Identifier.fromNamespaceAndPath(TheConstant.MODID, "textures/environment/sky/clouds_far.png");
    private static final Identifier CLOUDS_NEAR = Identifier.fromNamespaceAndPath(TheConstant.MODID, "textures/environment/sky/clouds_near.png");
    /** 雨天才出现的压顶云层（下垂的乳状云），B 通道阈值从 0.45 开始。 */
    private static final Identifier CLOUDS_STORM = Identifier.fromNamespaceAndPath(TheConstant.MODID, "textures/environment/sky/clouds_storm.png");
    /** 云量从 0 涨到 1 所需的秒数。原版雨量约 5 秒就到位，云按 B 通道阈值逐层长出，需要更慢才自然。 */
    private static final float CLOUD_RAIN_SECONDS = 30.0F;

    private static final float CUBE_HALF = 100.0F;
    private static final int CUBE_INDEX_COUNT = 36;

    private static final Identifier SUN_SPRITE = Identifier.withDefaultNamespace("sun");
    /** 太阳贴近地平线时的染色（橙）与高空时的染色（奶油色）。 */
    private static final Vector3f SUN_LOW = new Vector3f(1.0F, 0.62F, 0.38F);
    private static final Vector3f SUN_HIGH = new Vector3f(1.0F, 0.93F, 0.78F);
    /** 月亮染色：偏青的冰蓝，亮部接近白。 */
    private static final Vector3f MOON_TINT = new Vector3f(0.78F, 0.90F, 1.0F);
    private static final float MOON_SIZE = 24.0F;

    private final ConstantSkyPalette.Frame frame = new ConstantSkyPalette.Frame();
    private final CelestialQuad sunQuad = new CelestialQuad();
    private final CelestialQuad moonQuad = new CelestialQuad();
    private final SkySilhouetteRenderer silhouettes = new SkySilhouetteRenderer();
    private final SkyEyesRenderer skyEyes = new SkyEyesRenderer();
    private @Nullable GpuBuffer cubeBuffer;
    /** 平滑后的雨量，驱动云带的阈值生长；-1 表示尚未初始化（进入世界时直接取当前雨量）。 */
    private float cloudRain = -1.0F;
    private long lastCloudNanos;

    public static void registerPipeline(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(PIPELINE);
        SkySilhouetteRenderer.registerPipeline(event);
    }

    public static void registerSkybox(RegisterCustomEnvironmentEffectRendererEvent event) {
        event.registerSkyboxRenderer(ID, new ConstantSkyRenderer());
        event.registerWeatherEffectRenderer(ConstantRainRenderer.ID, new ConstantRainRenderer());
        // 原版方块云与手绘天空风格冲突：恒定维度先不画云，后续阶段用手绘云带替代
        event.registerCloudRenderer(NO_CLOUDS_ID, new CustomCloudsRenderer() {
            @Override
            public boolean renderClouds(LevelRenderState levelRenderState, Vec3 camPos, CloudStatus cloudStatus, int cloudColor,
                                        float cloudHeight, int cloudRange, Matrix4fc modelViewMatrix) {
                // 关闭自定义天空时恢复原版云
                return ClientConfig.customSky();
            }
        });
    }

    /** 当前帧的天空参数，供雾色等其它客户端逻辑读取，确保与天空一致。 */
    public static void computeFrame(ClientLevel level, float partialTick, ConstantSkyPalette.Frame out) {
        float timeOfDay = (float) (level.getDefaultClockTime() % 24000L) + partialTick;
        float moon = ConstantRealmTime.moonBrightness(currentMoonPhase(partialTick));
        ConstantSkyPalette.compute(timeOfDay, level.getRainLevel(partialTick), moon, out);
    }

    /** 客户端当前月相（原版 visual/moon_phase 环境属性，与原版月亮贴图一致）。 */
    public static MoonPhase currentMoonPhase(float partialTick) {
        return Minecraft.getInstance().gameRenderer.getMainCamera().attributeProbe()
                .getValue(EnvironmentAttributes.MOON_PHASE, partialTick);
    }

    @Override
    public boolean renderSky(LevelRenderState levelRenderState, SkyRenderState skyRenderState, Matrix4fc modelViewMatrix, Runnable setupFog) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || !ClientConfig.customSky()) {
            return false;
        }
        setupFog.run();

        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        computeFrame(level, partialTick, this.frame);
        // 取模保证 float 精度；水彩漂移极慢，回绕时的跳变不可见
        float seconds = ((level.getGameTime() % 1_200_000L) + partialTick) / 20.0F;

        float timeOfDay = (float) (level.getDefaultClockTime() % 24000L) + partialTick;
        float rain = level.getRainLevel(partialTick);
        // 太阳方向与原版一致（原版太阳贴图就画在这个方向上），着色器的暖色光晕跟着它走
        Matrix4f sunRotation = new Matrix4f().rotateY(-Mth.HALF_PI).rotateX(skyRenderState.sunAngle);
        Vector3f sunDir = sunRotation.transformDirection(new Vector3f(0.0F, 1.0F, 0.0F));
        Matrix4f moonRotation = new Matrix4f().rotateY(-Mth.HALF_PI).rotateX(skyRenderState.moonAngle);
        Vector3f moonDir = moonRotation.transformDirection(new Vector3f(0.0F, 1.0F, 0.0F));
        // 新月夜没有任何星星
        MoonPhase phase = skyRenderState.moonPhase;
        float moonLight = ConstantRealmTime.moonBrightness(phase);
        float starVis = ConstantRealmTime.isNewMoon(phase) ? 0.0F : ConstantSkyPalette.nightFactor(timeOfDay) * (1.0F - rain);
        float sunVis = 1.0F - rain * 0.95F;
        float clouds = this.updateCloudRain(rain);

        Vector3f z = this.frame.zenith;
        Vector3f h = this.frame.horizon;
        Vector3f g = this.frame.ground;
        Matrix4f params = new Matrix4f(
                z.x, z.y, z.z, seconds,
                h.x, h.y, h.z, ClientConfig.paperGrain(),
                g.x, g.y, g.z, this.frame.blotch * ClientConfig.watercolorStrength(),
                ConstantSkyPalette.HORIZON_GLOW, starVis, sunDir.x, sunDir.y);

        GpuBuffer vertices = this.getCubeBuffer();
        RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = autoIndices.getBuffer(CUBE_INDEX_COUNT);
        GpuTextureView colorTexture = mc.getMainRenderTarget().getColorTextureView();
        GpuTextureView depthTexture = mc.getMainRenderTarget().getDepthTextureView();
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(modelViewMatrix, new Vector4f(moonDir.x, moonDir.y, moonDir.z, skyRenderState.rainBrightness * moonLight),
                        new Vector3f(sunVis, sunDir.z, clouds), params);

        // 必须在打开 RenderPass 之前取贴图：首次 getTexture 会上传贴图，渲染通道开着时上传会崩溃
        AbstractTexture noiseTexture = mc.getTextureManager().getTexture(NOISE_TEXTURE);
        AbstractTexture cloudsFar = mc.getTextureManager().getTexture(CLOUDS_FAR);
        AbstractTexture cloudsNear = mc.getTextureManager().getTexture(CLOUDS_NEAR);
        AbstractTexture cloudsStorm = mc.getTextureManager().getTexture(CLOUDS_STORM);

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "Constant sky", colorTexture, OptionalInt.empty(), depthTexture, OptionalDouble.empty())) {
            renderPass.setPipeline(PIPELINE);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.bindTexture("Sampler0", noiseTexture.getTextureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR));
            renderPass.bindTexture("Sampler1", cloudsFar.getTextureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST));
            renderPass.bindTexture("Sampler2", cloudsNear.getTextureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST));
            renderPass.bindTexture("Sampler3", cloudsStorm.getTextureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST));
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indexBuffer, autoIndices.type());
            renderPass.drawIndexed(0, 0, CUBE_INDEX_COUNT, 1);
        }

        float celestialAlpha = skyRenderState.rainBrightness;
        this.renderSun(mc, modelViewMatrix, sunRotation, sunDir, sunVis * celestialAlpha, colorTexture, depthTexture);
        this.renderMoon(mc, modelViewMatrix, moonRotation, moonDir, skyRenderState.moonPhase, celestialAlpha, colorTexture, depthTexture);
        // 群系远景剪影最后画：挡住沉到地平线附近的日月
        this.silhouettes.render(mc, level, modelViewMatrix, this.frame, vertices, colorTexture, depthTexture);
        // 低理智时剪影树林里的眼睛（规则同诡异之眼实体），画在剪影之上
        this.skyEyes.render(mc, level, modelViewMatrix, partialTick, this.silhouettes.visibleAlpha(), colorTexture, depthTexture);
        return true;
    }

    /**
     * 云量以恒定速度追赶当前雨量（满量程 {@link #CLOUD_RAIN_SECONDS} 秒），用真实时间计时，暂停时不动。
     * 首帧或长时间没渲染（切维度、读档）时直接对齐，避免进世界后云再慢慢长出来。
     */
    private float updateCloudRain(float rain) {
        long now = System.nanoTime();
        float dt = (now - this.lastCloudNanos) / 1.0E9F;
        this.lastCloudNanos = now;
        if (this.cloudRain < 0.0F || dt > 5.0F) {
            this.cloudRain = rain;
        } else if (!Minecraft.getInstance().isPaused()) {
            float step = dt / CLOUD_RAIN_SECONDS;
            this.cloudRain += Mth.clamp(rain - this.cloudRain, -step, step);
        }
        return this.cloudRain;
    }

    /**
     * 原版方形太阳贴图（celestials 图集里的 {@code sun}），几何与原版相同（距离 100、半边长 30），
     * 按太阳高度染色：贴近地平线偏橙，高空偏奶油色。原版把颜色写死为白色，所以这里自己绘制。
     */
    private void renderSun(Minecraft mc, Matrix4fc modelViewMatrix, Matrix4f sunRotation, Vector3f sunDir, float alpha,
                           GpuTextureView colorTexture, GpuTextureView depthTexture) {
        if (alpha <= 0.001F || sunDir.y < -0.15F) {
            return;
        }
        TextureAtlas atlas = mc.getAtlasManager().getAtlasOrThrow(AtlasIds.CELESTIALS);
        float warm = Mth.clamp((sunDir.y + 0.05F) / 0.5F, 0.0F, 1.0F);
        warm = warm * warm * (3.0F - 2.0F * warm);
        Vector4f tint = new Vector4f(
                Mth.lerp(warm, SUN_LOW.x, SUN_HIGH.x),
                Mth.lerp(warm, SUN_LOW.y, SUN_HIGH.y),
                Mth.lerp(warm, SUN_LOW.z, SUN_HIGH.z),
                alpha);
        GpuBuffer quad = this.sunQuad.get(atlas.getSprite(SUN_SPRITE), false, "Constant sun quad");
        drawCelestial(atlas, quad, modelViewMatrix, sunRotation, 30.0F, tint, "Constant sun", colorTexture, depthTexture);
    }

    /**
     * 原版月亮贴图（含月相），染成冷冽的冰蓝色；着色器另在月亮周围铺一圈深蓝光晕。
     * 尺寸比原版（20）略大，接近参考图里月亮的分量。
     */
    private void renderMoon(Minecraft mc, Matrix4fc modelViewMatrix, Matrix4f moonRotation, Vector3f moonDir, MoonPhase phase,
                            float alpha, GpuTextureView colorTexture, GpuTextureView depthTexture) {
        if (alpha <= 0.001F || moonDir.y < -0.15F) {
            return;
        }
        TextureAtlas atlas = mc.getAtlasManager().getAtlasOrThrow(AtlasIds.CELESTIALS);
        TextureAtlasSprite sprite = atlas.getSprite(Identifier.withDefaultNamespace("moon/" + phase.getSerializedName()));
        // 原版月亮的 UV 是翻转的，保持一致
        GpuBuffer quad = this.moonQuad.get(sprite, true, "Constant moon quad");
        Vector4f tint = new Vector4f(MOON_TINT.x, MOON_TINT.y, MOON_TINT.z, alpha);
        drawCelestial(atlas, quad, modelViewMatrix, moonRotation, MOON_SIZE, tint, "Constant moon", colorTexture, depthTexture);
    }

    private static void drawCelestial(TextureAtlas atlas, GpuBuffer quad, Matrix4fc modelViewMatrix, Matrix4f rotation, float size,
                                      Vector4f tint, String label, GpuTextureView colorTexture, GpuTextureView depthTexture) {
        Matrix4f pose = new Matrix4f(modelViewMatrix).mul(rotation).translate(0.0F, 100.0F, 0.0F).scale(size, 1.0F, size);
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(pose, tint, new Vector3f(), new Matrix4f());
        RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = autoIndices.getBuffer(6);
        try (RenderPass pass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> label, colorTexture, OptionalInt.empty(), depthTexture, OptionalDouble.empty())) {
            pass.setPipeline(RenderPipelines.CELESTIAL);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.bindTexture("Sampler0", atlas.getTextureView(), atlas.getSampler());
            pass.setVertexBuffer(0, quad);
            pass.setIndexBuffer(indexBuffer, autoIndices.type());
            pass.drawIndexed(0, 0, 6, 1);
        }
    }

    /** 单个天体四边形；图集在资源重载后 UV 可能变化，UV 变了就重建。 */
    private static final class CelestialQuad {
        private @Nullable GpuBuffer buffer;
        private final float[] uv = new float[4];

        GpuBuffer get(TextureAtlasSprite sprite, boolean flipped, String name) {
            float u0 = sprite.getU0();
            float v0 = sprite.getV0();
            float u1 = sprite.getU1();
            float v1 = sprite.getV1();
            if (this.buffer != null && u0 == this.uv[0] && v0 == this.uv[1] && u1 == this.uv[2] && v1 == this.uv[3]) {
                return this.buffer;
            }
            if (this.buffer != null) {
                this.buffer.close();
            }
            float a0 = flipped ? u1 : u0;
            float a1 = flipped ? u0 : u1;
            float b0 = flipped ? v1 : v0;
            float b1 = flipped ? v0 : v1;
            VertexFormat format = DefaultVertexFormat.POSITION_TEX;
            try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(4 * format.getVertexSize())) {
                BufferBuilder builder = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, format);
                builder.addVertex(-1.0F, 0.0F, -1.0F).setUv(a0, b0);
                builder.addVertex(1.0F, 0.0F, -1.0F).setUv(a1, b0);
                builder.addVertex(1.0F, 0.0F, 1.0F).setUv(a1, b1);
                builder.addVertex(-1.0F, 0.0F, 1.0F).setUv(a0, b1);
                try (MeshData mesh = builder.buildOrThrow()) {
                    this.buffer = RenderSystem.getDevice().createBuffer(() -> name, 32, mesh.vertexBuffer());
                }
            }
            this.uv[0] = u0;
            this.uv[1] = v0;
            this.uv[2] = u1;
            this.uv[3] = v1;
            return this.buffer;
        }
    }

    private GpuBuffer getCubeBuffer() {
        if (this.cubeBuffer == null) {
            this.cubeBuffer = buildCube();
        }
        return this.cubeBuffer;
    }

    /** 与原版末地天空相同的六面立方体，只保留位置。 */
    private static GpuBuffer buildCube() {
        try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(24 * DefaultVertexFormat.POSITION.getVertexSize())) {
            BufferBuilder builder = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            float s = CUBE_HALF;
            for (int i = 0; i < 6; i++) {
                Matrix4f pose = new Matrix4f();
                switch (i) {
                    case 1 -> pose.rotationX((float) (Math.PI / 2));
                    case 2 -> pose.rotationX((float) (-Math.PI / 2));
                    case 3 -> pose.rotationX((float) Math.PI);
                    case 4 -> pose.rotationZ((float) (Math.PI / 2));
                    case 5 -> pose.rotationZ((float) (-Math.PI / 2));
                    default -> {
                    }
                }
                builder.addVertex(pose, -s, -s, -s);
                builder.addVertex(pose, -s, -s, s);
                builder.addVertex(pose, s, -s, s);
                builder.addVertex(pose, s, -s, -s);
            }
            try (MeshData mesh = builder.buildOrThrow()) {
                return RenderSystem.getDevice().createBuffer(() -> "Constant sky cube", 40, mesh.vertexBuffer());
            }
        }
    }
}
