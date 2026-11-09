package com.theconstant.dontstravemc.render.sky;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.theconstant.dontstravemc.TheConstant;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

/**
 * 群系远景剪影：地平线上一圈远/近两层手绘剪影，在天空、太阳、月亮之后绘制（会挡住贴近地平线的日月）。
 * <p>
 * 贴图命名 {@code textures/environment/sky/silhouette_<群系id>_far.png / _near.png}，8192×256，
 * 第 224 行 = 地平线，往上 224 行对应仰角 0°–9.84°，往下 32 行对应 0° 到 -1.41°（横纵纹素同为 0.044°，像素是方的）（见 docs/sky-art-asset-spec.md）。
 * <p>
 * 视差：两层看作半径不同的圆柱（远 {@value #FAR_RADIUS}、近 {@value #NEAR_RADIUS} 格），
 * 玩家相对“进入群系时的位置”的横向位移会让近层转得比远层快。
 * 切换群系时先淡出旧剪影，再淡入新剪影。
 * <pre>
 * TextureMat col0 = 地平线 rgb, 淡入淡出 alpha
 * TextureMat col1 = 雾色 rgb, 0
 * TextureMat col2 = 天顶 rgb, 0
 * TextureMat col3 = 远层位移 x/R, z/R, 近层位移 x/R, z/R
 * ColorModulator   = 走动小生物（见 {@link Walker}；沼泽触手 {@link Popper}、墓地鬼魂 {@link Ghost} 也走这一路）
 * ModelOffset      = 盘旋飞鸟：盘旋中心 u（远层坐标）, 可见度（白天 1，夜里淡到 0）, 循环时间（秒，0–{@value #FLYER_LOOP}）
 * </pre>
 */
public final class SkySilhouetteRenderer {

    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(TheConstant.MODID, "pipeline/sky_silhouette"))
            .withVertexShader(Identifier.fromNamespaceAndPath(TheConstant.MODID, "core/constant_sky"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(TheConstant.MODID, "core/sky_silhouette"))
            .withSampler("Sampler0")
            .withSampler("Sampler1")
            .withSampler("Sampler2")
            .withSampler("Sampler3")
            .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .build();

    /** 群系 → 剪影名（贴图前缀 silhouette_&lt;名&gt;）。以后的群系照此追加。 */
    private static final Map<Identifier, String> BIOME_SILHOUETTES = build();

    private static Map<Identifier, String> build() {
        java.util.HashMap<Identifier, String> map = new java.util.HashMap<>();
        // 临时：所有会生成常青树（evergreen_tree_placed / _mega_placed）的群系都先用常青树剪影；
        // 剪影名相同，所以在这些群系之间走动不会触发淡出淡入
        put(map, "evergreen_forest", "evergreen_forest", "lunar_forest", "mandrake_forest",
                "mixed_ground", "mixed_turf", "pig_village_forest", "pond_forest", "walrus_forest");
        // 临时：猪王/鼹鼠桦树林先共用桦树林剪影，以后有专属地标再拆
        put(map, "birch_forest", "birch_forest", "pigking_birch_forest", "mole_birch_forest");
        // 临时：驼鹿鹅平原、海象平原先共用草原剪影
        put(map, "grassland", "grassland", "moose_goose_plain", "walrus_plain");
        // 临时：皮弗娄牛草原先共用热带草原剪影
        put(map, "savanna", "savanna", "beefalo_savanna");
        put(map, "rockyland", "rockyland");
        put(map, "cemetery_forest", "cemetery_forest");
        // 临时：沙漠绿洲先共用沙漠剪影
        put(map, "desert", "desert", "desert_oasis");
        // 临时：鱼人沼泽先共用沼泽剪影
        put(map, "swamp", "swamp", "merm_swamp");
        put(map, "beequeen_grassland", "beequeen_grassland", "killer_bee_plain");
        return Map.copyOf(map);
    }

    private static void put(java.util.Map<Identifier, String> map, String silhouette, String... biomes) {
        for (String biome : biomes) {
            map.put(Identifier.fromNamespaceAndPath(TheConstant.MODID, biome), silhouette);
        }
    }

    /**
     * 剪影名 → 走动小生物的帧图（4 帧 16×16：站立、迈步 A、并腿、迈步 B）。
     * 小生物画在远层和近层之间，走到近层石头后面会被挡住。
     */
    private static final Map<String, Identifier> WALKERS = Map.of(
            "rockyland", Identifier.fromNamespaceAndPath(TheConstant.MODID, "textures/environment/sky/walker_tallbird.png"));

    /**
     * 剪影名 → 原地冒出的小生物帧图（10 帧 16×16：冒出 4 帧、摆动 6 帧，见 {@link Popper}）。
     * 和走动小生物共用 ColorModulator / Sampler2（同一剪影只有其中一种），着色器不用区分。
     */
    private static final Map<String, Identifier> POPPERS = Map.of(
            "swamp", Identifier.fromNamespaceAndPath(TheConstant.MODID, "textures/environment/sky/popper_tentacle.png"));

    /**
     * 剪影名 → 墓地鬼魂（帧图 8 帧 16×16，一个 1.6 秒的漂浮周期，B=255 标出发光的身体）和可以冒出鬼魂的墓碑列（艺术像素）。
     * 和走动小生物共用 ColorModulator / Sampler2（同一剪影只有其中一种），见 {@link Ghost}。
     */
    private record GhostSpec(Identifier sheet, int[] graves) {}

    private static final Map<String, GhostSpec> GHOSTS = Map.of(
            // 墓碑位置由 gen_silhouette_cemetery_forest.py 打印（相邻至少隔 24 列）
            "cemetery_forest", new GhostSpec(Identifier.fromNamespaceAndPath(TheConstant.MODID, "textures/environment/sky/walker_ghost.png"),
                    new int[] {223, 267, 392, 679, 710, 743, 861, 921, 985, 1041, 1149, 1187, 1246, 1277, 1307, 1375,
                            1529, 1592, 1643, 1667, 1755, 1877, 1983, 2047}));

    /**
     * 剪影名 → 天上盘旋的小飞鸟（帧图 3 帧 16×10：翅膀向上、平展滑翔、向下）和盘旋中心（远层 u 坐标）。
     * 三只鸟的轨迹和扇翅都由着色器按时间算，画在两层剪影之上、不被遮挡。
     */
    private record Flyer(Identifier sheet, float centerU) {}

    private static final Map<String, Flyer> FLYERS = Map.of(
            // 秃鹫盘旋在兽骨地标（艺术像素第 1720 列）上空
            "desert", new Flyer(Identifier.fromNamespaceAndPath(TheConstant.MODID, "textures/environment/sky/flyer_buzzard.png"),
                    1720.0F / 2048.0F));

    /** 飞鸟循环时间的周期（秒），必须是着色器里各盘旋周期和扇翅周期的公倍数。 */
    private static final float FLYER_LOOP = 120.0F;

    private static final float FAR_RADIUS = 2400.0F;
    private static final float NEAR_RADIUS = 600.0F;
    /** 淡入/淡出各自用时（秒）。 */
    private static final float FADE_SECONDS = 1.5F;
    private static final int CUBE_INDEX_COUNT = 36;

    private @Nullable String current;
    private float alpha;
    private Vec3 anchor = Vec3.ZERO;
    private long lastNanos = System.nanoTime();
    private final Walker walker = new Walker();
    private final Popper popper = new Popper();
    private final Ghost ghost = new Ghost();
    /** 上一帧的夜晚程度（0 白天 – 1 夜里），鬼魂按它决定出现频率和亮度。 */
    private float night;
    private float flyerTime;

    public static void registerPipeline(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(PIPELINE);
    }

    private static Identifier texture(String name, String layer) {
        return Identifier.fromNamespaceAndPath(TheConstant.MODID, "textures/environment/sky/silhouette_" + name + "_" + layer + ".png");
    }

    private static @Nullable String targetFor(ClientLevel level, Vec3 cam) {
        return level.getBiome(BlockPos.containing(cam)).unwrapKey()
                .map(k -> BIOME_SILHOUETTES.get(k.identifier()))
                .orElse(null);
    }

    private void tick(ClientLevel level, Vec3 cam) {
        long now = System.nanoTime();
        float dt = Math.min((now - this.lastNanos) / 1.0e9F, 0.25F);
        this.lastNanos = now;
        float step = dt / FADE_SECONDS;

        String target = targetFor(level, cam);
        if (target != null && target.equals(this.current)) {
            this.alpha = Math.min(1.0F, this.alpha + step);
        } else {
            this.alpha = Math.max(0.0F, this.alpha - step);
            if (this.alpha <= 0.0F) {
                if (!java.util.Objects.equals(this.current, target)) {
                    this.walker.reset(target != null && WALKERS.containsKey(target)
                            ? NearProfile.load(texture(target, "near")) : null);
                    this.popper.reset(target != null && POPPERS.containsKey(target)
                            ? NearProfile.load(texture(target, "near")) : null);
                    this.ghost.reset(target != null ? GHOSTS.get(target) : null);
                }
                this.current = target;
                this.anchor = cam;
            }
        }
        if (this.current != null && WALKERS.containsKey(this.current)) {
            this.walker.tick(dt);
        }
        if (this.current != null && POPPERS.containsKey(this.current)) {
            this.popper.tick(dt);
        }
        if (this.current != null && GHOSTS.containsKey(this.current)) {
            this.ghost.tick(dt, this.night);
        }
        this.flyerTime = (this.flyerTime + dt) % FLYER_LOOP;
    }

    /** 当前剪影的可见度（0–1，含淡入淡出）；没有剪影时为 0。 */
    public float visibleAlpha() {
        return this.current == null ? 0.0F : this.alpha;
    }

    public void render(Minecraft mc, ClientLevel level, Matrix4fc modelViewMatrix, ConstantSkyPalette.Frame frame,
                       GpuBuffer cube, GpuTextureView colorTexture, GpuTextureView depthTexture) {
        Vec3 cam = mc.gameRenderer.getMainCamera().position();
        this.night = frame.night;
        this.tick(level, cam);
        if (this.current == null || this.alpha <= 0.001F) {
            return;
        }

        float dx = (float) (cam.x - this.anchor.x);
        float dz = (float) (cam.z - this.anchor.z);
        Vector3f h = frame.horizon;
        Vector3f f = frame.fog;
        Vector3f z = frame.zenith;
        Matrix4f params = new Matrix4f(
                h.x, h.y, h.z, this.alpha,
                f.x, f.y, f.z, 0.0F,
                z.x, z.y, z.z, 0.0F,
                dx / FAR_RADIUS, dz / FAR_RADIUS, dx / NEAR_RADIUS, dz / NEAR_RADIUS);

        // 在打开 RenderPass 之前取贴图（首次 getTexture 会上传）
        AbstractTexture far = mc.getTextureManager().getTexture(texture(this.current, "far"));
        AbstractTexture near = mc.getTextureManager().getTexture(texture(this.current, "near"));
        Identifier walkerId = WALKERS.get(this.current);
        Identifier popperId = POPPERS.get(this.current);
        GhostSpec ghostSpec = GHOSTS.get(this.current);
        // 没有小生物的剪影也要给 Sampler2 绑点东西；可见度 0 时着色器不会去读
        AbstractTexture walkerTex = walkerId != null ? mc.getTextureManager().getTexture(walkerId)
                : popperId != null ? mc.getTextureManager().getTexture(popperId)
                : ghostSpec != null ? mc.getTextureManager().getTexture(ghostSpec.sheet()) : near;
        Flyer flyer = FLYERS.get(this.current);
        AbstractTexture flyerTex = flyer != null ? mc.getTextureManager().getTexture(flyer.sheet()) : near;
        Vector3f flyerParams = flyer != null
                ? new Vector3f(flyer.centerU(), 1.0F - frame.night, this.flyerTime)   // 夜里飞鸟淡出
                : new Vector3f();
        Vector4f walkerParams = walkerId != null && this.walker.active
                ? new Vector4f(this.walker.u, this.walker.frame(), this.walker.facing, this.walker.alpha())
                : popperId != null && this.popper.active
                ? new Vector4f(this.popper.u, this.popper.frame(), this.popper.facing, 1.0F)
                : ghostSpec != null && this.ghost.active
                ? new Vector4f(this.ghost.u, this.ghost.frame(), this.ghost.facing, this.ghost.alpha(frame.night))
                : new Vector4f(0.0F);

        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
                .writeTransform(modelViewMatrix, walkerParams, flyerParams, params);
        RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = autoIndices.getBuffer(CUBE_INDEX_COUNT);
        try (RenderPass pass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "Constant sky silhouette", colorTexture, OptionalInt.empty(), depthTexture, OptionalDouble.empty())) {
            pass.setPipeline(PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            // 横向重复（绕一圈），像素画用最近邻
            pass.bindTexture("Sampler0", far.getTextureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST));
            pass.bindTexture("Sampler1", near.getTextureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST));
            pass.bindTexture("Sampler2", walkerTex.getTextureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST));
            pass.bindTexture("Sampler3", flyerTex.getTextureView(), RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST));
            pass.setUniform("DynamicTransforms", transforms);
            pass.setVertexBuffer(0, cube);
            pass.setIndexBuffer(indexBuffer, autoIndices.type());
            pass.drawIndexed(0, 0, CUBE_INDEX_COUNT, 1);
        }
    }

    /**
     * 近层贴图每一列的最高不透明行（艺术像素，0–56；56 = 这一列地平线以上是空的），
     * 用来判断小生物在哪里会被近层完全挡住。
     */
    static final class NearProfile {
        static final int COLS = 2048;
        final int[] top = new int[COLS];

        static @Nullable NearProfile load(Identifier id) {
            var resource = Minecraft.getInstance().getResourceManager().getResource(id);
            if (resource.isEmpty()) {
                return null;
            }
            try (java.io.InputStream in = resource.get().open();
                 com.mojang.blaze3d.platform.NativeImage image = com.mojang.blaze3d.platform.NativeImage.read(in)) {
                NearProfile p = new NearProfile();
                int scale = image.getWidth() / COLS;                  // 8192 / 2048 = 4
                for (int x = 0; x < COLS; x++) {
                    int t = 56;
                    for (int y = 0; y < 56; y++) {
                        if ((image.getPixel(x * scale + scale / 2, y * scale + scale / 2) >>> 24) > 127) {
                            t = y;
                            break;
                        }
                    }
                    p.top[x] = t;
                }
                return p;
            } catch (java.io.IOException e) {
                return null;
            }
        }

        /** 以 cx 为中心、宽 11、高 15（第 41–55 行）的小生物是否被完全挡住。 */
        boolean hides(int cx) {
            for (int dx = -5; dx <= 5; dx++) {
                if (this.top[Math.floorMod(cx + dx, COLS)] > 41) {
                    return false;
                }
            }
            return true;
        }

        /**
         * 原地冒出的小生物可以出现的列：以它为中心宽 9 的范围内近层最高不超过第 53 行（地平线上 3 行），
         * 也就是开阔水面或平地，冒出来几乎不被挡住。
         */
        java.util.List<Integer> openSpots() {
            java.util.List<Integer> spots = new java.util.ArrayList<>();
            for (int x = 0; x < COLS; x++) {
                boolean open = true;
                for (int dx = -4; dx <= 4 && open; dx++) {
                    open = this.top[Math.floorMod(x + dx, COLS)] >= 53;
                }
                if (open) {
                    spots.add(x);
                }
            }
            return spots;
        }

        /** 所有能完全藏住小生物的位置（每段连续区间取中心）。 */
        java.util.List<Integer> hidingSpots() {
            java.util.List<Integer> spots = new java.util.ArrayList<>();
            int start = -1;
            for (int x = 0; x <= COLS; x++) {
                boolean h = x < COLS && this.hides(x);
                if (h && start < 0) {
                    start = x;
                } else if (!h && start >= 0) {
                    spots.add((start + x - 1) / 2);
                    start = -1;
                }
            }
            return spots;
        }
    }

    /**
     * 原地冒出的小生物（目前只有沼泽的触手）：每 15–45 秒在一处开阔水面/平地从地里冒出来
     * （冒出 4 帧，是同一个姿势从地下逐帧升上来，每帧 0.1 秒），原地摆动 5–10 个整周期
     * （摆动 6 帧一个周期，每秒 6 帧，每帧只动一两个像素），再倒放冒出帧缩回地里消失。
     * 摆动按整周期结束，所以缩回前正好回到冒出时的姿势，不会跳帧。
     * 画在远层和近层之间，前面的芦苇会挡住它的根部。
     * 找不到开阔处时随便挑一列（没有淡入淡出，冒出/缩回本身就是出现和消失）。
     */
    private static final class Popper {
        private static final float EMERGE_FRAME = 0.1F;
        private static final int EMERGE_FRAMES = 4;
        private static final int WAVE_FRAMES = 6;
        private static final float WAVE_FPS = 6.0F;

        private final net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create();
        private java.util.List<Integer> spots = java.util.List.of();
        boolean active;
        float u;
        float facing = 1.0F;
        private float age;
        private float wave;
        private float wait = 8.0F;

        void reset(@Nullable NearProfile profile) {
            this.spots = profile != null ? profile.openSpots() : java.util.List.of();
            this.active = false;
            this.wait = 5.0F + this.random.nextFloat() * 15.0F;
        }

        private float emergeTime() {
            return EMERGE_FRAMES * EMERGE_FRAME;
        }

        void tick(float dt) {
            if (!this.active) {
                this.wait -= dt;
                if (this.wait <= 0.0F) {
                    this.active = true;
                    this.age = 0.0F;
                    // 整数个摆动周期（每周期 WAVE_FRAMES / WAVE_FPS = 1 秒）
                    this.wave = (5 + this.random.nextInt(6)) * WAVE_FRAMES / WAVE_FPS;
                    this.facing = this.random.nextBoolean() ? 1.0F : -1.0F;
                    int col = this.spots.isEmpty()
                            ? this.random.nextInt(NearProfile.COLS)
                            : this.spots.get(this.random.nextInt(this.spots.size()));
                    this.u = (col + 0.5F) / NearProfile.COLS;
                }
                return;
            }
            this.age += dt;
            if (this.age >= this.wave + 2.0F * this.emergeTime()) {
                this.active = false;
                this.wait = 15.0F + this.random.nextFloat() * 30.0F;
            }
        }

        /** 冒出 0-1-2-3，摆动 4–9 循环，缩回 3-2-1-0。 */
        int frame() {
            float e = this.emergeTime();
            if (this.age < e) {
                return (int) (this.age / EMERGE_FRAME);
            }
            float back = this.age - e - this.wave;
            if (back >= 0.0F) {
                return Math.max(0, EMERGE_FRAMES - 1 - (int) (back / EMERGE_FRAME));
            }
            return EMERGE_FRAMES + (int) ((this.age - e) * WAVE_FPS) % WAVE_FRAMES;
        }
    }

    /**
     * 墓地鬼魂：偶尔从一块墓碑旁边浮出来（2.5 秒淡入），一边上下浮动（画在帧图里，8 帧 1.6 秒一个周期）
     * 一边缓缓横向飘移 14–24 秒，再 2.5 秒淡出；飘到墓碑、树后面时被近层挡住。
     * 半透明：夜里最多 0.75，白天约 0.35。夜里每 15–40 秒一只，白天很少（每次到点只有 15% 机会）。
     */
    private static final class Ghost {
        private static final float FADE = 2.5F;
        private static final int FRAMES = 8;
        private static final float FPS = 5.0F;

        private final net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create();
        private int[] graves = new int[0];
        boolean active;
        float u;
        float facing = 1.0F;
        private float age;
        private float life;
        private float speed;
        private float wait = 10.0F;

        void reset(@Nullable GhostSpec spec) {
            this.graves = spec != null ? spec.graves() : new int[0];
            this.active = false;
            this.wait = 5.0F + this.random.nextFloat() * 10.0F;
        }

        void tick(float dt, float night) {
            if (!this.active) {
                this.wait -= dt;
                if (this.wait > 0.0F) {
                    return;
                }
                if (night < 0.4F && this.random.nextFloat() > 0.15F) {
                    this.wait = 20.0F + this.random.nextFloat() * 40.0F;      // 白天大多不出来
                    return;
                }
                this.active = true;
                this.age = 0.0F;
                this.life = 14.0F + this.random.nextFloat() * 10.0F;
                this.speed = (0.6F + this.random.nextFloat() * 0.6F) / NearProfile.COLS;   // 每秒 0.6–1.2 艺术像素
                this.facing = this.random.nextBoolean() ? 1.0F : -1.0F;
                int col = this.graves.length == 0 ? this.random.nextInt(NearProfile.COLS)
                        : this.graves[this.random.nextInt(this.graves.length)] + (int) this.facing * 3;
                this.u = (col + 0.5F) / NearProfile.COLS;
                return;
            }
            this.age += dt;
            this.u = (this.u + this.facing * this.speed * dt + 1.0F) % 1.0F;
            if (this.age >= this.life) {
                this.active = false;
                this.wait = night >= 0.4F ? 15.0F + this.random.nextFloat() * 25.0F : 40.0F + this.random.nextFloat() * 40.0F;
            }
        }

        int frame() {
            return (int) (this.age * FPS) % FRAMES;
        }

        float alpha(float night) {
            float env = Math.min(1.0F, Math.min(this.age, this.life - this.age) / FADE);
            return Math.max(0.0F, env) * (0.35F + 0.4F * Math.min(1.0F, night));
        }
    }

    /**
     * 偶尔出现的走动小生物（目前只有礁石地的高脚鸟）：每 30–90 秒从一块大石头后面走出来，
     * 走 20–35 秒，途中偶尔停下张望、有时掉头；时间到了就走向最近的大石头，完全走到后面就消失。
     * 位置用近层贴图的 u 坐标，所以跟着近层一起视差、被近层挡住。找不到藏身处时退回淡入淡出。
     */
    private static final class Walker {
        /** 走速：每秒 3.5 个艺术像素（≈ 0.62°）。 */
        private static final float SPEED = 3.5F / 2048.0F;
        private static final float FADE = 1.0F;
        /** 离开阶段最多再走多久（秒），超时就淡出。 */
        private static final float MAX_LEAVE = 60.0F;
        private static final int[] WALK_CYCLE = {1, 2, 3, 2};

        private final net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create();
        private @Nullable NearProfile profile;
        private java.util.List<Integer> spots = java.util.List.of();
        boolean active;
        float u;
        float facing = 1.0F;
        private float age;
        private float life;
        private float pause;
        private float walkPhase;
        private float wait = 10.0F;
        private boolean emerged;        // 从藏身处出来的就不用淡入
        private boolean leaving;
        private float leaveAge;
        private boolean fadingOut;

        void reset(@Nullable NearProfile profile) {
            this.profile = profile;
            this.spots = profile != null ? profile.hidingSpots() : java.util.List.of();
            this.active = false;
            this.wait = 10.0F + this.random.nextFloat() * 20.0F;
        }

        private int col() {
            return Math.floorMod((int) Math.floor(this.u * NearProfile.COLS), NearProfile.COLS);
        }

        void tick(float dt) {
            if (!this.active) {
                this.wait -= dt;
                if (this.wait <= 0.0F) {
                    this.spawn();
                }
                return;
            }
            this.age += dt;
            if (this.fadingOut) {
                if (this.age >= this.leaveAge + FADE) {
                    this.despawn();
                }
                return;
            }
            if (!this.leaving && this.age >= this.life) {
                this.startLeaving();
                if (this.fadingOut) {
                    return;
                }
            }
            if (this.leaving) {
                if (this.profile != null && this.profile.hides(this.col())) {
                    this.despawn();                       // 走到石头后面，消失
                    return;
                }
                if (this.age - this.leaveAge > MAX_LEAVE) {
                    this.fadingOut = true;
                    this.leaveAge = this.age;
                    return;
                }
            } else if (this.pause > 0.0F) {
                this.pause -= dt;
                if (this.pause <= 0.0F && this.random.nextFloat() < 0.35F) {
                    this.facing = -this.facing;           // 张望完掉头
                }
                return;
            } else if (this.random.nextFloat() < dt * 0.08F) {
                this.pause = 1.0F + this.random.nextFloat() * 2.0F;
            }
            this.u = (this.u + this.facing * SPEED * dt + 1.0F) % 1.0F;
            this.walkPhase += dt;
        }

        private void spawn() {
            this.active = true;
            this.age = 0.0F;
            this.pause = 0.0F;
            this.walkPhase = 0.0F;
            this.leaving = false;
            this.fadingOut = false;
            this.life = 20.0F + this.random.nextFloat() * 15.0F;
            this.facing = this.random.nextBoolean() ? 1.0F : -1.0F;
            this.emerged = !this.spots.isEmpty();
            this.u = this.emerged
                    ? (this.spots.get(this.random.nextInt(this.spots.size())) + 0.5F) / NearProfile.COLS
                    : this.random.nextFloat();
        }

        /** 朝最近的藏身处走（反方向更近就掉头）；没有藏身处就淡出。 */
        private void startLeaving() {
            this.leaving = true;
            this.leaveAge = this.age;
            this.pause = 0.0F;
            if (this.spots.isEmpty()) {
                this.fadingOut = true;
                return;
            }
            int here = this.col();
            int bestAhead = Integer.MAX_VALUE;
            int bestBehind = Integer.MAX_VALUE;
            for (int spot : this.spots) {
                int ahead = Math.floorMod((spot - here) * (int) this.facing, NearProfile.COLS);
                int behind = Math.floorMod(-(spot - here) * (int) this.facing, NearProfile.COLS);
                bestAhead = Math.min(bestAhead, ahead);
                bestBehind = Math.min(bestBehind, behind);
            }
            if (bestBehind < bestAhead) {
                this.facing = -this.facing;
            }
        }

        private void despawn() {
            this.active = false;
            this.wait = 30.0F + this.random.nextFloat() * 60.0F;
        }

        /** 停下时是站立帧，走动时按 迈步A-并腿-迈步B-并腿 每秒 5 帧循环。 */
        int frame() {
            return this.pause > 0.0F && !this.leaving ? 0 : WALK_CYCLE[(int) (this.walkPhase * 5.0F) % WALK_CYCLE.length];
        }

        float alpha() {
            float in = this.emerged ? 1.0F : Math.min(1.0F, this.age / FADE);
            float out = this.fadingOut ? Math.max(0.0F, 1.0F - (this.age - this.leaveAge) / FADE) : 1.0F;
            return Math.max(0.0F, Math.min(in, out));
        }
    }
}
