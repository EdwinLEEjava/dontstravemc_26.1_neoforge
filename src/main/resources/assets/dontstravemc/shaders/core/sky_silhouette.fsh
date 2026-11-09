#version 330

#moj_import <minecraft:dynamictransforms.glsl>

// 群系远景剪影（远层 Sampler0 + 近层 Sampler1），参数见 SkySilhouetteRenderer：
//   TextureMat[0] = 地平线 rgb, 淡入淡出 alpha
//   TextureMat[1] = 雾色 rgb
//   TextureMat[2] = 天顶 rgb
//   TextureMat[3] = 远层位移/半径 xz, 近层位移/半径 xz（视差）
//   ColorModulator = 走动小生物：近层 u 坐标, 动画帧, 朝向（±1）, 可见度（0 = 不画）
//                    （原地冒出的小生物如沼泽触手、墓地鬼魂也走这一路，帧图同为 16×16、帧数不限；
//                     帧图里 B = 1 的像素是鬼魂发白的身体，按 ghostShade 上色，不走剪影明暗）
// 走动小生物（Sampler2，如 walker_tallbird.png）：每帧 16×16，1 纹素 = 1 艺术像素（剪影贴图的 4×4 块），
//   画在远层和近层之间——走到近层的石头/土包后面会被挡住。
//   ModelOffset    = 盘旋飞鸟：盘旋中心 u（远层坐标）, 可见度（0 = 不画）, 循环时间（秒，0–120）
// 盘旋飞鸟（Sampler3，如 flyer_buzzard.png）：3 帧 16×10（翅膀上 / 平展 / 下），身体在第 5 行、第 8 列，
//   三只鸟各绕中心转椭圆（远近看成左右来回 + 轻微上下），每 6 秒扇几下翅膀、其余时间滑翔；
//   画在两层剪影之上，不被遮挡。
// 贴图：R = 明暗档位，G = 顶部亮边，A = 形状。第 224 行 = 地平线。贴图 8192×256，横纵每个纹素都是 360/8192 ≈ 0.044°（方形像素）：
//   以上 224 行 = 0°–9.84°，以下 32 行 = 0° 到 -1.41°。

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform sampler2D Sampler3;

in vec3 skyPos;

out vec4 fragColor;

const float TOP_DEG = 224.0 * 360.0 / 8192.0;   // 9.84°，与横向同比例
const float BOTTOM_DEG = 32.0 * 360.0 / 8192.0;  // 1.41°
const float HORIZON_V = 224.0 / 256.0;

float rowV(float elevDeg) {
    return elevDeg >= 0.0
        ? HORIZON_V * (1.0 - elevDeg / TOP_DEG)
        : HORIZON_V + (1.0 - HORIZON_V) * (-elevDeg / BOTTOM_DEG);
}

// 把视线所在方位角换成圆柱上的命中点方位角（玩家横向位移 q = 位移/半径）
float layerU(vec2 dxz, float az01, vec2 q) {
    return az01 + (q.y * dxz.x - q.x * dxz.y) / 6.2831853;
}

// 远层/近层/小生物共用的上色：R 高 → 接近雾色，R 低 → 深色剪影；G = 顶部亮边
vec3 shade(vec4 c, vec3 dark, vec3 fog) {
    vec3 col = mix(dark, fog, c.r);
    return mix(col, min(fog * 1.15 + 0.03, vec3(1.0)), c.g * 0.55);
}

// 鬼魂身体（B = 1）：接近白色、略带冷色，混一点雾色免得白天太刺眼
vec3 ghostShade(vec3 fog) {
    return mix(vec3(0.88, 0.92, 1.0), fog, 0.2);
}

// 走动小生物在这个视线方向上的像素（a = 0 表示没有）。u 在近层坐标里，所以跟着近层一起视差。
vec4 walkerAt(float uNear, float v) {
    vec4 w = ColorModulator;
    if (w.w <= 0.0) {
        return vec4(0.0);
    }
    float du = fract(uNear - w.x + 0.5) - 0.5;          // -0.5..0.5 圈
    float ax = floor(du * 2048.0) + 8.0;                // 帧内列，第 8 列 = 中心
    float sy = floor(v * 64.0) - 40.0;                  // 帧底 = 第 55 艺术行（地平线上一行）
    if (ax < 0.0 || ax >= 16.0 || sy < 0.0 || sy >= 16.0) {
        return vec4(0.0);
    }
    float sx = w.z < 0.0 ? 15.0 - ax : ax;
    return texelFetch(Sampler2, ivec2(int(w.y) * 16 + int(sx), int(sy)), 0);
}

// 盘旋飞鸟在这个视线方向上的像素（a = 0 表示没有）。art 坐标：x = 艺术像素列，row = 艺术像素行（0 = 贴图顶）。
// 周期 40/30/60 秒和扇翅周期 6 秒都整除 120，循环时间回绕时没有跳变。
vec4 flyerAt(float uFar, float v) {
    vec3 p = ModelOffset;
    if (p.y <= 0.0) {
        return vec4(0.0);
    }
    float du = (fract(uFar - p.x + 0.5) - 0.5) * 2048.0;   // 离盘旋中心的艺术像素列
    float row = v * 64.0;
    const vec3 PERIOD = vec3(40.0, 30.0, 60.0);
    const vec3 RX = vec3(26.0, 17.0, 36.0);                // 横向半径（艺术像素）
    const vec3 ROW = vec3(17.0, 21.0, 13.0);               // 身体所在行：约 6.2°、5.5°、7.6°
    const vec3 RY = vec3(1.5, 1.0, 2.0);
    const vec3 PHASE = vec3(0.0, 2.1, 4.2);
    for (int i = 0; i < 3; i++) {
        float th = 6.2831853 * p.z / PERIOD[i] + PHASE[i];
        float bx = RX[i] * cos(th);
        float by = ROW[i] + RY[i] * sin(th);
        float ax = floor(du - bx) + 8.0;
        float ay = floor(row - floor(by)) + 5.0;
        if (ax < 0.0 || ax >= 16.0 || ay < 0.0 || ay >= 10.0) {
            continue;
        }
        float facing = -sin(th) >= 0.0 ? 1.0 : -1.0;       // 朝运动方向
        float local = mod(p.z + float(i) * 1.7, 6.0);
        int frame = 1;                                      // 平展滑翔
        if (local < 1.5) {
            int k = int(local * 6.0) % 4;                    // 上-平-下-平
            frame = k == 0 ? 0 : (k == 2 ? 2 : 1);
        }
        float sx = facing < 0.0 ? 15.0 - ax : ax;
        vec4 c = texelFetch(Sampler3, ivec2(frame * 16 + int(sx), int(ay)), 0);
        if (c.a > 0.5) {
            return c;
        }
    }
    return vec4(0.0);
}

void main() {
    vec3 dir = normalize(skyPos);
    float elevDeg = degrees(asin(clamp(dir.y, -1.0, 1.0)));
    if (elevDeg > TOP_DEG) {
        discard;
    }

    vec3 horizon = TextureMat[0].rgb;
    float fade   = TextureMat[0].a;
    vec3 fog     = TextureMat[1].rgb;
    vec3 zenith  = TextureMat[2].rgb;
    vec4 shift   = TextureMat[3];

    // 两层的暗部：与天空同色相，夜里自动变暗
    vec3 dark = horizon * 0.22 + zenith * 0.10;

    vec3 col;
    float a;
    if (elevDeg < -BOTTOM_DEG) {
        // 贴图以下继续铺实心地面，避免和天空下半截之间出现缝
        col = mix(dark, fog, 0.31);
        a = 1.0;
    } else {
        vec2 dxz = normalize(dir.xz + vec2(1e-5, 0.0));
        float az01 = atan(dir.z, dir.x) / 6.2831853 + 0.5;
        float v = rowV(elevDeg);
        float uNear = layerU(dxz, az01, shift.zw);
        vec4 nearC = texture(Sampler1, vec2(uNear, v));
        float uFar = layerU(dxz, az01, shift.xy);
        vec4 farC  = texture(Sampler0, vec2(uFar, v));
        vec4 flyer = elevDeg > 0.0 ? flyerAt(uFar, v) : vec4(0.0);
        if (flyer.a > 0.5) {
            // 飞鸟在最前面；可见度只用来淡入淡出，背后若有剪影就和剪影混合
            float fw = ModelOffset.y;
            vec3 behind = nearC.a > 0.5 ? shade(nearC, dark, fog) : (farC.a > 0.5 ? shade(farC, dark, fog) : vec3(0.0));
            float ba = (nearC.a > 0.5 || farC.a > 0.5) ? 1.0 : 0.0;
            col = ba > 0.0 ? mix(behind, shade(flyer, dark, fog), fw) : shade(flyer, dark, fog);
            a = max(ba, fw);
        } else if (nearC.a > 0.5) {
            col = shade(nearC, dark, fog);
            a = 1.0;
        } else {
            // 远层在后，走动小生物在远层之前、近层之后
            vec4 walker = walkerAt(uNear, v);
            float wa = walker.a > 0.5 ? ColorModulator.w : 0.0;
            float fa = farC.a > 0.5 ? 1.0 : 0.0;
            if (wa <= 0.0 && fa <= 0.0) {
                discard;
            }
            vec3 farCol = fa > 0.0 ? shade(farC, dark, fog) : vec3(0.0);
            vec3 wCol = walker.b > 0.5 ? ghostShade(fog) : shade(walker, dark, fog);
            col = wa > 0.0 ? (fa > 0.0 ? mix(farCol, wCol, wa) : wCol) : farCol;
            a = max(fa, wa);
        }
    }

    // 地平线以下渐渐融进雾色，接缝由雾盖住
    col = mix(col, fog, smoothstep(0.0, -BOTTOM_DEG, elevDeg) * 0.35);

    fragColor = vec4(clamp(col, 0.0, 1.0), a * fade);
}
