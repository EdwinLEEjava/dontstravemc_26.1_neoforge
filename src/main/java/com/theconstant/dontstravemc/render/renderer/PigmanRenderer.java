package com.theconstant.dontstravemc.render.renderer;

import com.theconstant.dontstravemc.entity.neutral.PigmanEntity;
import com.theconstant.dontstravemc.model.PigmanModel;
import com.theconstant.dontstravemc.render.state.PigmanRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.ItemArmorGeoLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;

import java.util.List;

/**
 * Humanoid equipment via {@link HumanoidMobRenderer#extractHumanoidRenderState};
 * {@link ItemArmorGeoLayer} renders vanilla equippables on the {@code hat} bone.
 */
public class PigmanRenderer extends GeoEntityRenderer<PigmanEntity, PigmanRenderState> {

    private static final Identifier BUBBLE_TEXTURE = Identifier.fromNamespaceAndPath(
            "dontstravemc", "textures/gui/misc/pigman_talk.png");

    private static final float BUBBLE_Y_OFFSET = 2.55F;
    private static final float BUBBLE_SCALE = 0.025F;
    private static final float BUBBLE_HEIGHT = 16.0F;
    private static final float LEFT_CAP_W = 8.0F;
    private static final float CENTER_BRIDGE_W = 16.0F;
    private static final float RIGHT_CAP_W = 8.0F;
    private static final float MIN_SIDE_STRETCH_W = 8.0F;
    private static final float TAIL_W = 16.0F;
    private static final float TAIL_H = 8.0F;
    private static final float TEX_W = 64.0F;
    private static final float TEX_H = 32.0F;
    private static final float TEXT_PADDING = 6.0F;
    private static final int TEXT_RENDER_ORDER = 1;
    private static final boolean RENDER_SPEECH_BUBBLE = true;

    public PigmanRenderer(EntityRendererProvider.Context context) {
        super(context, new PigmanModel());
        this.shadowRadius = 0.55F;
        this.withRenderLayer(new PigmanEquipmentLayer(this, context));
    }

    @Override
    public PigmanRenderState createRenderState(PigmanEntity animatable, Void relatedObject) {
        return new PigmanRenderState();
    }

    @Override
    public void extractRenderState(PigmanEntity entity, PigmanRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTick, this.itemModelResolver);
        state.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        state.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        state.isSleeping = entity.isAsleep();
        state.useSmileTexture = entity.isIdleHappyAnimationPlaying();
        state.speechText = entity.getSpeechText();
        state.speechTicks = entity.getSpeechTicks();
        state.speechSequence = state.speechText == null || state.speechText.isEmpty()
                ? FormattedCharSequence.EMPTY
                : Component.translatable(state.speechText).getVisualOrderText();
    }

    /**
     * Matches {@link GeoEntityRenderer#submit}: delegates to {@code performRenderPass}, then draws speech bubble
     * using {@link net.minecraft.client.renderer.entity.state.EntityRenderState#lightCoords}.
     */
    @Override
    public void submit(PigmanRenderState renderState, PoseStack poseStack, SubmitNodeCollector renderTasks, CameraRenderState cameraState) {
        super.submit(renderState, poseStack, renderTasks, cameraState);

        if (renderState.speechText == null || renderState.speechText.isEmpty() || renderState.speechTicks <= 0) {
            return;
        }

        renderSpeechBubble(renderState, poseStack, renderTasks, cameraState, renderState.lightCoords);
    }

    private void renderSpeechBubble(PigmanRenderState renderState, PoseStack poseStack,
                                    SubmitNodeCollector renderTasks, CameraRenderState cameraState,
                                    int packedLight) {
        Font font = Minecraft.getInstance().font;
        FormattedCharSequence orderedText = renderState.speechSequence;
        int textWidth = font.width(orderedText);
        float contentW = Math.max(textWidth + TEXT_PADDING * 2, CENTER_BRIDGE_W + MIN_SIDE_STRETCH_W * 2.0F);
        float stretchTotalW = contentW - CENTER_BRIDGE_W;
        float leftStretchW = stretchTotalW / 2.0F;
        float rightStretchW = stretchTotalW - leftStretchW;
        float totalW = LEFT_CAP_W + leftStretchW + CENTER_BRIDGE_W + rightStretchW + RIGHT_CAP_W;

        float alpha = renderState.speechTicks < 10 ? renderState.speechTicks / 10.0F : 1.0F;
        if (alpha <= 0.01F) return;

        poseStack.pushPose();

        poseStack.translate(0.0F, BUBBLE_Y_OFFSET, 0.0F);
        poseStack.mulPose(cameraState.orientation);
        poseStack.scale(BUBBLE_SCALE, -BUBBLE_SCALE, BUBBLE_SCALE);

        float halfW = totalW / 2.0F;

        if (RENDER_SPEECH_BUBBLE) {
            RenderType bubbleRenderType = RenderTypes.entityTranslucent(BUBBLE_TEXTURE);

            renderTasks.submitCustomGeometry(poseStack, bubbleRenderType, (pose, vertexConsumer) -> {
                int a = (int) (alpha * 255) << 24;
                int color = 0xFFFFFF | a;

                float x0 = -halfW;
                renderQuad(pose, vertexConsumer, packedLight, color,
                        x0, 0, LEFT_CAP_W, BUBBLE_HEIGHT,
                        0, 0, LEFT_CAP_W / TEX_W, BUBBLE_HEIGHT / TEX_H);

                float mx0 = x0 + LEFT_CAP_W;
                renderQuad(pose, vertexConsumer, packedLight, color,
                        mx0, 0, leftStretchW, BUBBLE_HEIGHT,
                        8.0F / TEX_W, 0, 24.0F / TEX_W, BUBBLE_HEIGHT / TEX_H);

                float cx0 = mx0 + leftStretchW;
                renderQuad(pose, vertexConsumer, packedLight, color,
                        cx0, 0, CENTER_BRIDGE_W, BUBBLE_HEIGHT,
                        24.0F / TEX_W, 0, 40.0F / TEX_W, BUBBLE_HEIGHT / TEX_H);

                float sx0 = cx0 + CENTER_BRIDGE_W;
                renderQuad(pose, vertexConsumer, packedLight, color,
                        sx0, 0, rightStretchW, BUBBLE_HEIGHT,
                        40.0F / TEX_W, 0, 56.0F / TEX_W, BUBBLE_HEIGHT / TEX_H);

                float rx0 = sx0 + rightStretchW;
                renderQuad(pose, vertexConsumer, packedLight, color,
                        rx0, 0, RIGHT_CAP_W, BUBBLE_HEIGHT,
                        56.0F / TEX_W, 0, 1.0F, BUBBLE_HEIGHT / TEX_H);

                float tailX = cx0 + (CENTER_BRIDGE_W - TAIL_W) / 2.0F;
                renderQuad(pose, vertexConsumer, packedLight, color,
                        tailX, BUBBLE_HEIGHT, TAIL_W, TAIL_H,
                        24.0F / TEX_W, 16.0F / TEX_H, 40.0F / TEX_W, 24.0F / TEX_H);
            });
        }

        float textX = -textWidth / 2.0F;
        float textY = (BUBBLE_HEIGHT - 8.0F) / 2.0F;
        int textAlpha = (int) (alpha * 255) << 24;
        int textColor = textAlpha;
        int bgColor = 0;
        int lightCoords = LightCoordsUtil.lightCoordsWithEmission(packedLight, 2);

        poseStack.translate(0.0F, 0.0F, 0.1F);
        renderTasks.order(TEXT_RENDER_ORDER).submitText(poseStack, textX, textY, orderedText, false,
                Font.DisplayMode.POLYGON_OFFSET, lightCoords, textColor, bgColor, 0);

        poseStack.popPose();
    }

    private static class PigmanEquipmentLayer extends ItemArmorGeoLayer<PigmanEntity, Void, PigmanRenderState> {
        private static final List<RenderData> HEAD = List.of(RenderData.head("hat"));

        PigmanEquipmentLayer(PigmanRenderer renderer, EntityRendererProvider.Context context) {
            super(renderer, context);
        }

        @Override
        protected List<RenderData> getRelevantBones(RenderPassInfo<PigmanRenderState> renderPassInfo) {
            return HEAD;
        }
    }

    private static void renderQuad(PoseStack.Pose pose, VertexConsumer consumer, int packedLight, int color,
                                   float x, float y, float w, float h,
                                   float u0, float v0, float u1, float v1) {
        consumer.addVertex(pose, x, y, 0).setColor(color).setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 0, 1);
        consumer.addVertex(pose, x, y + h, 0).setColor(color).setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 0, 1);
        consumer.addVertex(pose, x + w, y + h, 0).setColor(color).setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 0, 1);
        consumer.addVertex(pose, x + w, y, 0).setColor(color).setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 0, 1);
    }
}
