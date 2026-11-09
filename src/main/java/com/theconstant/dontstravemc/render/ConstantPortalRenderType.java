package com.theconstant.dontstravemc.render;

import com.theconstant.dontstravemc.TheConstant;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public final class ConstantPortalRenderType {
    public static final RenderPipeline PIPELINE = RenderPipeline.builder(
                    RenderPipelines.MATRICES_PROJECTION_SNIPPET,
                    RenderPipelines.GLOBALS_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(TheConstant.MODID, "pipeline/constant_portal"))
            .withVertexShader(Identifier.fromNamespaceAndPath(TheConstant.MODID, "core/constant_portal"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(TheConstant.MODID, "core/constant_portal"))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withCull(false)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .build();

    public static final RenderType RENDER_TYPE = RenderType.create(
            "dontstravemc_constant_portal",
            RenderSetup.builder(PIPELINE).createRenderSetup()
    );

    private ConstantPortalRenderType() {
    }

    public static void registerPipeline(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(PIPELINE);
    }
}
