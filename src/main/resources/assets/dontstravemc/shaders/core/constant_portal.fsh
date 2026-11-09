#version 330

#moj_import <minecraft:globals.glsl>

in vec2 spiralPos;
in vec4 vertexColor;

out vec4 fragColor;

// ---- 体素：每格 16 像素，所有计算都在像素中心取样 ----
const float PIXELS_PER_BLOCK = 16.0;
const float TAU = 6.2831853;

// ---- 饥荒：一拍二式的步进动画（12 帧/秒），3 秒一圈 ----
const float FPS = 12.0;
const float SECONDS_PER_TURN = 3.0;
// GameTime 每 24000 tick（1200 秒）从 0 回到 1。
// 1200 * 12 = 14400 帧，14400 / (12 * 3) = 400 整圈，所以回绕时不会跳帧。

// ---- 限定调色板：羊皮纸 + 墨色，和饥荒动画的旋涡一致 ----
const vec3 PAPER       = vec3(0.93, 0.87, 0.70);
const vec3 PAPER_SHADE = vec3(0.76, 0.66, 0.50);
const vec3 INK         = vec3(0.13, 0.04, 0.06);
const vec3 INK_DEEP    = vec3(0.02, 0.01, 0.02);

const float BAYER[16] = float[16](
     0.0,  8.0,  2.0, 10.0,
    12.0,  4.0, 14.0,  6.0,
     3.0, 11.0,  1.0,  9.0,
    15.0,  7.0, 13.0,  5.0);

float bayer4(vec2 p) {
    ivec2 i = ivec2(mod(p, 4.0));
    return (BAYER[i.y * 4 + i.x] + 0.5) / 16.0;
}

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

void main() {
    // 顶点色 R、G 拼出整组半径（像素）。B 作为每个传送门的相位，让多个传送门不同步。
    float pixelRadius = max(dot(vertexColor.rg, vec2(255.0, 65280.0)), 8.0);
    float phase = vertexColor.b;

    vec2 pixel = floor(spiralPos * PIXELS_PER_BLOCK);
    vec2 c = pixel + 0.5;
    float r = length(c);
    float rNorm = r / pixelRadius;

    // 步进时间：转动按帧跳，不做平滑插值
    float frame = floor(GameTime * 1200.0 * FPS);
    float turns = frame / (FPS * SECONDS_PER_TURN) + phase;

    float ang = atan(c.y, c.x);

    // 外缘：固定的正圆，外圈一到两像素墨线
    float rim = pixelRadius;
    if (r > rim) {
        discard;
    }
    if (r > rim - 1.6) {
        fragColor = vec4(INK, 1.0);
        return;
    }

    // 螺旋：pow 让条纹越靠中心越密，产生往里吸的纵深感
    float spiral = pow(rNorm, 0.6) * 3.0 + (ang / TAU - turns);
    float f = fract(spiral);

    vec3 color;
    if (f >= 0.5) {
        color = INK;
    } else {
        // 纸色条纹靠近后缘的一侧用有序抖动过渡到阴影色，像手绘上的暗面
        float shade = smoothstep(0.15, 0.48, f);
        color = shade > bayer4(pixel) ? PAPER_SHADE : PAPER;
    }


    fragColor = vec4(color, 1.0);
}
