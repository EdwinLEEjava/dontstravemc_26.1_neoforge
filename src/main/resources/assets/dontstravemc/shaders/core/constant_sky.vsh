#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;

// 相机空间之前的立方体坐标；片元里归一化后就是视线方向（只随转头变化，不随走动变化）
out vec3 skyPos;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    skyPos = Position;
}
