#version 330

#moj_import <minecraft:dynamictransforms.glsl>

// 永恒领域天空：渐变天穹 + 水彩斑块 + 刷痕 + 星星 + 太阳光晕 + 纸纹（太阳本体用原版贴图）。
// 参数打包在 TextureMat 里，见 ConstantSkyRenderer：
//   [0] = 天顶 rgb, 时间(秒)
//   [1] = 地平线 rgb, 纸纹强度
//   [2] = 地平线以下 rgb, 水彩斑块强度
//   [3] = 地平线辉光, 星星可见度, 太阳方向 x, 太阳方向 y
// ModelOffset = 太阳可见度, 太阳方向 z, 云量（平滑后的雨量 0–1）
// ColorModulator = 月亮方向 xyz, 月亮可见度 × 月光强度（新月为 0，不再用作颜色乘数）
//
// 性能：值噪声全部来自 Sampler0 的 256×256 噪声贴图（线性过滤 + 重复寻址），
// 一次 3D 值噪声 = 1 次贴图采样，取代原先每次 8 个算术哈希。

uniform sampler2D Sampler0;
uniform sampler2D Sampler1; // 远层云带 clouds_far
uniform sampler2D Sampler2; // 近层云带 clouds_near
uniform sampler2D Sampler3; // 雨天压顶云层 clouds_storm

in vec3 skyPos;

out vec4 fragColor;

// ---------- 噪声 ----------
// 只给星星用：需要逐格离散的随机数，贴图噪声在格子上不够随机
float hash13(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.zyx + 31.32);
    return fract((p.x + p.y) * p.z);
}

// 3D 值噪声：z 方向的每个整数层在贴图上平移 (37,17)。
// 贴图 R = N(x,y)，G = N(x+37,y+17)，所以一次采样同时拿到第 z 层和第 z+1 层，再按 f.z 插值。
float vnoise(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    vec2 uv = i.xy + vec2(37.0, 17.0) * i.z + f.xy;
    vec2 rg = textureLod(Sampler0, (uv + 0.5) / 256.0, 0.0).rg;
    return mix(rg.r, rg.g, f.z);
}

// 3 个八度足够：低频浓淡上更高的八度在 0.2 左右的强度下看不出来
float fbm3(vec3 p) {
    float sum = 0.5 * vnoise(p);
    p = p * 2.03 + vec3(17.1, 9.2, 4.7);
    sum += 0.25 * vnoise(p);
    p = p * 2.03 + vec3(17.1, 9.2, 4.7);
    sum += 0.125 * vnoise(p);
    return sum / 0.875;
}


// 手绘云带贴图（见 docs/sky-art-asset-spec.md）：
//   R = 明暗；A = 形状；
//   B = 出现阈值：云量 >= B 时出现，B = 0 是晴天就有的云。同一阶段的像素阈值按离旧轮廓的远近铺开，
//       所以云是一圈圈慢慢鼓出来的，而不是整圈突现；
//   G = 描边，数值是描边的消失阈值：云量 < G 时显示（255 = 一直显示），
//       云长大后被压在里面的旧轮廓会自动熄灭。
// 每个像素在越过阈值后的 CLOUD_FADE 云量内淡入/淡出（云量 1/30 每秒，约 1.5 秒），
// 只影响过渡中的像素，稳定后仍是 0/1 的硬边像素画。
// 贴图横向绕一整圈（u = 方位角），纵向整张对应仰角 [0°, topDeg]；超出范围不采样。
// 必须最近邻采样，否则 B/G 阈值在像素边缘被插值，会出现闪烁的碎点。
const float CLOUD_FADE = 0.05;

void applyCloudBand(inout vec3 col, sampler2D tex, float az01, float elevDeg, float topDeg, float drift,
                    float cover, vec3 shade, vec3 body, vec3 rim) {
    float v = 1.0 - elevDeg / topDeg;
    if (v <= 0.0 || v >= 1.0) {
        return;
    }
    vec4 c = texture(tex, vec2(az01 + drift, v));
    if (c.a <= 0.0 || c.b > cover) {
        return;
    }
    // 晴天的云（B = 0）不淡入，否则云量 0 时会是透明的
    float appear = c.b < 0.002 ? 1.0 : clamp((cover - c.b) / CLOUD_FADE, 0.0, 1.0);
    // R 的有效范围约 100–245（4 档），拉伸到 0–1 再在暗面与亮面之间插值
    float lit = clamp((c.r - 0.39) / 0.57, 0.0, 1.0);
    vec3 cloud = mix(shade, body, lit);
    // 描边：被新长出的云盖住时淡出；阴雨天没有逆光，整体随云量减弱
    float rimOn = c.g < 0.002 ? 0.0 : (c.g > 0.998 ? 1.0 : clamp((c.g - cover) / CLOUD_FADE, 0.0, 1.0));
    cloud = mix(cloud, rim, rimOn * (1.0 - 0.7 * cover));
    col = mix(col, cloud, c.a * appear);
}

void main() {
    vec3 dir = normalize(skyPos);
    float h = dir.y;

    vec3 zenith  = TextureMat[0].rgb;
    vec3 horizon = TextureMat[1].rgb;
    vec3 ground  = TextureMat[2].rgb;
    float time   = TextureMat[0].a;
    float grain  = TextureMat[1].a;
    float blotch = TextureMat[2].a;
    float glow   = TextureMat[3].x;
    float starVis = TextureMat[3].y;
    vec2 sunXY   = TextureMat[3].zw;
    float sunVis = ModelOffset.x;

    // ---------- 渐变天穹 ----------
    // 地平线附近略带扰动，像手绘时没刷齐的边（只有地平线一带看得出，其余高度省掉）
    float wobble = 0.0;
    if (abs(h) < 0.4) {
        wobble = (fbm3(dir * 3.0 + vec3(0.0, 0.0, time * 0.002)) - 0.5) * 0.06;
    }
    float hh = h + wobble;
    float up = smoothstep(-0.02, 0.75, hh);
    vec3 col = mix(horizon, zenith, pow(up, 0.75));
    col = mix(col, ground, smoothstep(0.0, -0.35, hh));
    // 地平线辉光：雾色 = 地平线色 * (1 + glow)，两者在此衔接
    col *= 1.0 + glow * exp(-abs(hh - 0.02) * 14.0);

    // ---------- 水彩斑块 ----------
    // 设置里水彩强度为 0 时整段跳过
    if (blotch > 0.001) {
        vec3 drift = vec3(time * 0.0015, 0.0, time * 0.0009);
        float wash = fbm3(dir * vec3(1.6, 2.4, 1.6) + drift);
        float washC = smoothstep(0.30, 0.70, wash);
        float pool = smoothstep(0.44, 0.50, wash) - smoothstep(0.50, 0.58, wash);
        // 第二层只取单个八度：它只负责细碎起伏
        float wash2 = vnoise(dir * 4.5 - drift * 1.7 + vec3(3.3));
        col *= 1.0 + (washC - 0.5) * blotch * 0.7;
        // 颜料边缘只留极淡的一圈，太重会像污渍
        col *= 1.0 - pool * blotch * 0.2;
        col *= 1.0 + (wash2 - 0.5) * blotch * 0.3;
        // 淡色雾块：把部分区域往地平线色提亮，形成参考图里的灰白水迹
        float mist = smoothstep(0.58, 0.82, fbm3(dir * 2.8 + vec3(7.1) + drift * 0.6));
        col = mix(col, horizon * 1.05, mist * blotch * 0.5 * smoothstep(-0.05, 0.5, h));

        // ---------- 刷痕 ----------
        // 按方位角拉长的噪声，形成近乎竖直的笔触。方位角在天顶处奇异，只在地平线附近一段出现。
        float streakBand = smoothstep(-0.1, 0.1, h) * (1.0 - smoothstep(0.25, 0.55, h));
        if (streakBand > 0.0) {
            float az = atan(dir.z, dir.x);
            float streak = vnoise(vec3(az * 40.0, h * 3.0, 1.7)) * 0.6 + vnoise(vec3(az * 110.0, h * 6.0, 8.3)) * 0.4;
            col *= 1.0 + (streak - 0.5) * blotch * 0.3 * streakBand;
        }
    }

    // ---------- 星星 ----------
    // 把方向空间切成 3D 网格，每格最多一颗星，位置/大小/闪烁相位都由格子哈希决定，
    // 所以星星固定在天上，不会随转头闪动。
    if (starVis > 0.001 && h > 0.0) {
        vec3 sp = dir * 70.0;
        vec3 cell = floor(sp);
        float pick = hash13(cell + 11.7);
        if (pick > 0.93) {
            vec3 starPos = cell + 0.25 + 0.5 * vec3(hash13(cell + 1.3), hash13(cell + 5.1), hash13(cell + 9.4));
            // 方形星星：把偏移投影到与视线垂直的平面（一轴保持水平），取切比雪夫距离
            vec3 off = sp - starPos;
            vec3 st1 = normalize(cross(vec3(0.0, 1.0, 0.0), dir) + vec3(1e-4, 0.0, 0.0));
            vec3 st2 = cross(dir, st1);
            float d = max(abs(dot(off, st1)), abs(dot(off, st2)));
            // 少数大星，多数小点，像随手点上去的
            float size = mix(0.06, 0.14, pow(hash13(cell + 3.9), 3.0));
            float aa = fwidth(d) * 1.2;
            float star = 1.0 - smoothstep(size - aa, size + aa, d);
            float twinkle = 0.65 + 0.35 * sin(time * (1.5 + 2.5 * hash13(cell + 7.7)) + pick * 40.0);
            float horizonFade = smoothstep(0.04, 0.25, h);
            vec3 starCol = mix(vec3(0.95, 0.93, 0.85), vec3(0.80, 0.85, 1.0), hash13(cell + 2.2));
            col = mix(col, starCol, star * twinkle * horizonFade * starVis);
        }
    }

    // ---------- 太阳光晕 ----------
    // 太阳本体是原版方形贴图（渲染器里另画并染色），这里只铺一片暖色光晕；没入地平线后隐藏
    float sunMask = smoothstep(-0.03, 0.02, h) * sunVis;
    if (sunMask > 0.001) {
        vec3 sunDir = normalize(vec3(sunXY, ModelOffset.y));
        float cosA = dot(dir, sunDir);
        // 弦长近似夹角，省掉 acos
        float ang = sqrt(max(2.0 - 2.0 * cosA, 0.0));
        vec3 sunWarm = mix(vec3(1.0, 0.55, 0.30), vec3(1.0, 0.86, 0.62), smoothstep(-0.05, 0.45, sunDir.y));
        col += sunWarm * (exp(-ang * 4.0) * 0.18 + exp(-ang * 18.0) * 0.22) * sunMask;
    }

    // ---------- 月亮光晕 ----------
    // 月亮本体是原版贴图（染成冰蓝），这里铺一圈深蓝光晕，把周围夜空提亮成参考图里的蓝色
    float moonMask = smoothstep(-0.03, 0.02, h) * ColorModulator.w;
    if (moonMask > 0.001) {
        vec3 moonDir = normalize(ColorModulator.xyz);
        float mAng = sqrt(max(2.0 - 2.0 * dot(dir, moonDir), 0.0));
        vec3 moonGlow = vec3(0.22, 0.42, 0.85);
        col += moonGlow * (exp(-mAng * 3.0) * 0.10 + exp(-mAng * 12.0) * 0.22) * moonMask;
    }

    // ---------- 云带 ----------
    // 远层慢、近层快，形成纵深；颜色取自当前时段的天空配色，所以白天、黄昏、夜晚自动变色
    {
        float az01 = atan(dir.z, dir.x) / 6.2831853 + 0.5;
        float elevDeg = degrees(asin(clamp(h, -1.0, 1.0)));
        vec3 cloudShade = mix(zenith, horizon, 0.55) * 0.95;
        vec3 cloudBody = min(horizon * 1.20 + 0.03, vec3(1.0));
        vec3 cloudRim = min(horizon * 1.50 + 0.08, vec3(1.0));
        // 云量：平滑后的雨量。雨越大云越厚，亮面往暗面压（漂移速度不随云量变，否则 time 很大时云会横跳）
        float cover = ModelOffset.z;
        cloudBody = mix(cloudBody, cloudShade * 1.1, cover * 0.55);
        applyCloudBand(col, Sampler1, az01, elevDeg, 20.0, time * 0.0004, cover, cloudShade * 1.05, mix(cloudBody, horizon, 0.3), cloudRim);
        // 压顶云层只在下雨时出现，一排排从上往下垂下来，画在远层和近层之间
        if (cover > 0.4) {  // 压顶云层阈值从 0.4 起
            applyCloudBand(col, Sampler3, az01, elevDeg, 45.0, time * 0.0006, cover, cloudShade * 0.85, mix(cloudShade, cloudBody, 0.6), cloudRim * 0.9);
        }
        applyCloudBand(col, Sampler2, az01, elevDeg, 36.0, time * 0.0008, cover, cloudShade, cloudBody, cloudRim);
    }

    // ---------- 纸纹 ----------
    // 固定在天空方向上，转头时颗粒不会"贴着屏幕"滑动
    if (grain > 0.001) {
        float paper = vnoise(dir * 160.0) * 0.6 + vnoise(dir * 420.0) * 0.4;
        // 纤维沿 y 拉长，天顶附近会被拉成放射状，同样随高度淡出
        float fiberFade = smoothstep(0.5, 0.85, h);
        float fiber = fiberFade < 1.0 ? vnoise(vec3(dir.x * 60.0, dir.y * 600.0, dir.z * 60.0)) : 0.5;
        fiber = mix(fiber, 0.5, fiberFade);
        col *= 1.0 - grain * (paper - 0.5) - grain * 0.4 * (fiber - 0.5);
    }

    // 天顶略压暗，增加画面重量感
    col *= mix(1.0, 0.85, smoothstep(0.6, 1.0, h));

    fragColor = vec4(clamp(col, 0.0, 1.0), 1.0);
}
