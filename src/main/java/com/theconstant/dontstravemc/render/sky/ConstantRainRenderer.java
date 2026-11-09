package com.theconstant.dontstravemc.render.sky;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.theconstant.dontstravemc.TheConstant;
import com.theconstant.dontstravemc.config.ClientConfig;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import com.theconstant.dontstravemc.particle.ModParticles;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.CustomWeatherEffectRenderer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * 永恒领域的雨：灰白细雨丝 + 统一斜向（风），替代原版蓝色竖直雨。
 * <p>
 * 雨柱数据仍由原版 {@link WeatherEffectRenderer#extractRenderState} 收集，这里只换绘制：
 * 自己的贴图、斜切的四边形、偏冷灰的颜色。雪沿用原版贴图。
 * 由 dimension_type 的 {@code "neoforge:custom_weather_effects": "dontstravemc:constant_rain"} 启用；
 * 关闭手绘天空设置时退回原版雨。
 */
public final class ConstantRainRenderer implements CustomWeatherEffectRenderer {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(TheConstant.MODID, "constant_rain");

    private static final Identifier RAIN_TEXTURE = Identifier.fromNamespaceAndPath(TheConstant.MODID, "textures/environment/constant_rain.png");
    private static final Identifier SNOW_TEXTURE = Identifier.withDefaultNamespace("textures/environment/snow.png");

    /** 每升高 1 格，雨丝向 +X 偏移多少格（风向）。 */
    private static final float WIND_SHEAR_X = 0.28F;
    private static final float WIND_SHEAR_Z = 0.10F;
    /** 雨丝颜色（乘在贴图上），略偏冷灰。 */
    private static final float RAIN_TINT = 0.92F;

    private final float[] columnSizeX = new float[1024];
    private final float[] columnSizeZ = new float[1024];

    public ConstantRainRenderer() {
        // 与原版相同：每根雨柱的面朝向相机所在的格子中心
        for (int z = 0; z < 32; z++) {
            for (int x = 0; x < 32; x++) {
                float dx = x - 16;
                float dz = z - 16;
                float len = Mth.length(dx, dz);
                this.columnSizeX[z * 32 + x] = len == 0 ? 1F : -dz / len;
                this.columnSizeZ[z * 32 + x] = len == 0 ? 0F : dx / len;
            }
        }
    }

    private int rainSoundTime;

    /**
     * 与原版 {@code WeatherEffectRenderer.tickRainParticles} 相同的落地水花与雨声逻辑，
     * 只把蓝色的 {@code minecraft:rain} 粒子换成灰白的 {@code dontstravemc:rain_splash}。
     */
    @Override
    public boolean tickRain(ClientLevel level, int ticks, Camera camera) {
        if (!ClientConfig.customSky()) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        float rainLevel = level.getRainLevel(1.0F);
        if (rainLevel <= 0.0F) {
            return true;
        }
        ParticleStatus particleStatus = mc.options.particles().get();
        int weatherRadius = mc.options.weatherRadius().get();
        RandomSource random = RandomSource.createThreadLocalInstance(ticks * 312987231L);
        BlockPos cameraPos = BlockPos.containing(camera.position());
        BlockPos lastSplash = null;
        int diameter = 2 * weatherRadius + 1;
        int count = (int) (0.225F * diameter * diameter * rainLevel * rainLevel) / (particleStatus == ParticleStatus.DECREASED ? 2 : 1);

        for (int i = 0; i < count; i++) {
            int dx = random.nextInt(diameter) - weatherRadius;
            int dz = random.nextInt(diameter) - weatherRadius;
            BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, cameraPos.offset(dx, 0, dz));
            if (top.getY() <= level.getMinY() || top.getY() > cameraPos.getY() + 10 || top.getY() < cameraPos.getY() - 10
                    || precipitationAt(level, top) != Biome.Precipitation.RAIN) {
                continue;
            }
            lastSplash = top.below();
            if (particleStatus == ParticleStatus.MINIMAL) {
                break;
            }
            double bx = random.nextDouble();
            double bz = random.nextDouble();
            BlockState block = level.getBlockState(lastSplash);
            FluidState fluid = level.getFluidState(lastSplash);
            double blockTop = block.getCollisionShape(level, lastSplash).max(Direction.Axis.Y, bx, bz);
            double fluidTop = fluid.getHeight(level, lastSplash);
            double y = Math.max(blockTop, fluidTop);
            ParticleOptions type = !fluid.is(FluidTags.LAVA) && !block.is(Blocks.MAGMA_BLOCK) && !CampfireBlock.isLitCampfire(block)
                    ? ModParticles.RAIN_SPLASH.get()
                    : ParticleTypes.SMOKE;
            level.addParticle(type, lastSplash.getX() + bx, lastSplash.getY() + y, lastSplash.getZ() + bz, 0.0, 0.0, 0.0);
        }

        if (lastSplash != null && random.nextInt(3) < this.rainSoundTime++) {
            this.rainSoundTime = 0;
            if (lastSplash.getY() > cameraPos.getY() + 1
                    && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, cameraPos).getY() > Mth.floor((float) cameraPos.getY())) {
                level.playLocalSound(lastSplash, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1F, 0.5F, false);
            } else {
                level.playLocalSound(lastSplash, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2F, 1.0F, false);
            }
        }
        return true;
    }

    private static Biome.Precipitation precipitationAt(Level level, BlockPos pos) {
        if (!level.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()))) {
            return Biome.Precipitation.NONE;
        }
        return level.getBiome(pos).value().getPrecipitationAt(pos, level.getSeaLevel());
    }

    @Override
    public boolean renderSnowAndRain(LevelRenderState levelRenderState, WeatherRenderState state, MultiBufferSource bufferSource, Vec3 camPos) {
        if (!ClientConfig.customSky()) {
            return false;
        }
        int columnCount = state.rainColumns.size() + state.snowColumns.size();
        if (columnCount == 0) {
            return true;
        }
        TextureManager textures = Minecraft.getInstance().getTextureManager();
        AbstractTexture rainTexture = textures.getTexture(RAIN_TEXTURE);
        AbstractTexture snowTexture = textures.getTexture(SNOW_TEXTURE);
        RenderTarget target = OutputTarget.WEATHER_TARGET.getRenderTarget();
        RenderPipeline pipeline = Minecraft.useShaderTransparency() ? RenderPipelines.WEATHER_DEPTH_WRITE : RenderPipelines.WEATHER_NO_DEPTH_WRITE;

        GpuBuffer vertexBuffer;
        GpuBuffer indexBuffer;
        VertexFormat.IndexType indexType;
        try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(columnCount * DefaultVertexFormat.PARTICLE.getVertexSize() * 4)) {
            BufferBuilder builder = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            this.addColumns(builder, state.rainColumns, camPos, 0.85F, state.radius, state.intensity, true);
            this.addColumns(builder, state.snowColumns, camPos, 0.8F, state.radius, state.intensity, false);
            try (MeshData mesh = builder.buildOrThrow()) {
                vertexBuffer = pipeline.getVertexFormat().uploadImmediateVertexBuffer(mesh.vertexBuffer());
                RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(mesh.drawState().mode());
                indexBuffer = autoIndices.getBuffer(mesh.drawState().indexCount());
                indexType = autoIndices.type();
            }
        }

        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(RAIN_TINT, RAIN_TINT, RAIN_TINT * 0.97F, 1.0F), new Vector3f(), new Matrix4f());
        try (RenderPass pass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "Constant weather", target.getColorTextureView(), OptionalInt.empty(), target.getDepthTextureView(), OptionalDouble.empty())) {
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.bindTexture("Sampler2", Minecraft.getInstance().gameRenderer.lightmap(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
            pass.setIndexBuffer(indexBuffer, indexType);
            pass.setVertexBuffer(0, vertexBuffer);
            int rainCount = state.rainColumns.size();
            if (rainCount > 0) {
                pass.bindTexture("Sampler0", rainTexture.getTextureView(), rainTexture.getSampler());
                pass.drawIndexed(0, 0, rainCount * 6, 1);
            }
            if (!state.snowColumns.isEmpty()) {
                pass.bindTexture("Sampler0", snowTexture.getTextureView(), snowTexture.getSampler());
                pass.drawIndexed(0, rainCount * 6, state.snowColumns.size() * 6, 1);
            }
        }
        return true;
    }

    private void addColumns(VertexConsumer builder, List<WeatherEffectRenderer.ColumnInstance> columns, Vec3 cam,
                            float maxAlpha, int radius, float intensity, boolean slanted) {
        float radiusSq = Math.max(1, radius * radius);
        float shearX = slanted ? WIND_SHEAR_X : 0F;
        float shearZ = slanted ? WIND_SHEAR_Z : 0F;
        for (WeatherEffectRenderer.ColumnInstance column : columns) {
            float rx = (float) (column.x() + 0.5 - cam.x);
            float rz = (float) (column.z() + 0.5 - cam.z);
            float distSq = rx * rx + rz * rz;
            float alpha = Mth.lerp(Math.min(distSq / radiusSq, 1.0F), maxAlpha, 0.35F) * intensity;
            int color = ARGB.white(alpha);
            int index = (column.z() - Mth.floor(cam.z) + 16) * 32 + column.x() - Mth.floor(cam.x) + 16;
            index = Mth.clamp(index, 0, 1023);
            float hx = this.columnSizeX[index] / 2.0F;
            float hz = this.columnSizeZ[index] / 2.0F;
            float y1 = (float) (column.topY() - cam.y);
            float y0 = (float) (column.bottomY() - cam.y);
            // 以相机高度为轴斜切：越高越往风向偏，整片雨朝同一方向倾斜
            float topX = y1 * shearX;
            float topZ = y1 * shearZ;
            float botX = y0 * shearX;
            float botZ = y0 * shearZ;
            float u0 = column.uOffset();
            float u1 = column.uOffset() + 1.0F;
            float v0 = column.bottomY() * 0.25F + column.vOffset();
            float v1 = column.topY() * 0.25F + column.vOffset();
            int light = column.lightCoords();
            builder.addVertex(rx - hx + topX, y1, rz - hz + topZ).setUv(u0, v0).setColor(color).setLight(light);
            builder.addVertex(rx + hx + topX, y1, rz + hz + topZ).setUv(u1, v0).setColor(color).setLight(light);
            builder.addVertex(rx + hx + botX, y0, rz + hz + botZ).setUv(u1, v1).setColor(color).setLight(light);
            builder.addVertex(rx - hx + botX, y0, rz - hz + botZ).setUv(u0, v1).setColor(color).setLight(light);
        }
    }
}
