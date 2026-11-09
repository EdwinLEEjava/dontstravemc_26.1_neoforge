package com.theconstant.dontstravemc.render.renderer;

import com.theconstant.dontstravemc.block.blockentity.custom.ConstantPortalBlockEntity;
import com.theconstant.dontstravemc.block.custom.ConstantPortalBlock;
import com.theconstant.dontstravemc.render.ConstantPortalRenderType;
import com.theconstant.dontstravemc.render.state.ConstantPortalRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ConstantPortalRenderer implements BlockEntityRenderer<ConstantPortalBlockEntity, ConstantPortalRenderState> {
    private static final float SURFACE_Y = ConstantPortalBlock.SURFACE_HEIGHT;

    public ConstantPortalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public ConstantPortalRenderState createRenderState() {
        return new ConstantPortalRenderState();
    }

    @Override
    public void extractRenderState(
            ConstantPortalBlockEntity blockEntity,
            ConstantPortalRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }
        ConstantPortalBlock.GroupBounds bounds = ConstantPortalBlock.findGroup(level, blockEntity.getBlockPos());
        state.centerX = bounds.centerX();
        state.centerZ = bounds.centerZ();
        state.radius = bounds.radius();
    }

    @Override
    public void submit(ConstantPortalRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        int blockX = state.blockPos.getX();
        int blockZ = state.blockPos.getZ();
        submitNodeCollector.submitCustomGeometry(poseStack, ConstantPortalRenderType.RENDER_TYPE, (pose, buffer) -> {
            vertex(pose, buffer, state, blockX, blockZ, 0.0F, 0.0F);
            vertex(pose, buffer, state, blockX, blockZ, 1.0F, 0.0F);
            vertex(pose, buffer, state, blockX, blockZ, 1.0F, 1.0F);
            vertex(pose, buffer, state, blockX, blockZ, 0.0F, 1.0F);
        });
    }

    private static void vertex(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            ConstantPortalRenderState state,
            int blockX,
            int blockZ,
            float localX,
            float localZ
    ) {
        float u = blockX + localX - state.centerX;
        float v = blockZ + localZ - state.centerZ;
        int pixelRadius = Math.max(1, Math.round(state.radius * 16.0F));
        buffer.addVertex(pose, localX, SURFACE_Y, localZ)
                .setUv(u, v)
                .setColor(pixelRadius & 0xFF, (pixelRadius >> 8) & 0xFF, groupPhase(state), 255);
    }

    /** 由整组中心算出的相位（0-255），同组方块一致，不同传送门错开转动。 */
    private static int groupPhase(ConstantPortalRenderState state) {
        int cx = Math.round(state.centerX * 2.0F);
        int cz = Math.round(state.centerZ * 2.0F);
        int h = cx * 0x45D9F3B ^ cz * 0x119DE1F3;
        h ^= h >>> 16;
        h *= 0x45D9F3B;
        h ^= h >>> 16;
        return h & 0xFF;
    }
}
