package mod.arcomit.lucisrenderlib.test.block;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class NBRenderType extends RenderStateShard {

    public NBRenderType(String pName, Runnable pSetupState, Runnable pClearState) {
        super(pName, pSetupState, pClearState);
    }

    protected static final RenderStateShard.TransparencyStateShard LIGHTNING_ADDITIVE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
            "lightning_additive_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//        RenderSystem.depthMask(false);
    }, () -> {
//        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

    public static RenderType getNB() {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_LIGHTNING_SHADER)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .setTransparencyState(NO_TRANSPARENCY)
                .setOutputState(MAIN_TARGET)
                .createCompositeState(false);
        return RenderType.create("lightning233", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES, 256, false, true, state);
    }

    public static RenderType getSlashBladeBlend(ResourceLocation p_228638_0_) {

        /*
         * RenderType.CompositeState rendertype$compositestate =
         * RenderType.CompositeState.builder()
         * .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER) .setTextureState(new
         * RenderStateShard.TextureStateShard(p_173200_, false, false))
         * .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         * .setOutputState(ITEM_ENTITY_TARGET) .setLightmapState(LIGHTMAP)
         * .setOverlayState(OVERLAY)
         * .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
         * .createCompositeState(true);
         */

        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER)
                .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, false, true))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)

                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                .createCompositeState(true);

        return RenderType.create("slashblade_blend", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }


    public static RenderType getSlashBladeBlendLuminous(ResourceLocation p_228638_0_) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setCullState(RenderStateShard.NO_CULL)
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, true, true))
                .setTransparencyState(LIGHTNING_ADDITIVE_TRANSPARENCY)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(COLOR_WRITE)
                .setLayeringState(POLYGON_OFFSET_LAYERING)
                .createCompositeState(false);
        return RenderType.create("slashblade_blend_luminous", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLES, 256, false, true, state);
    }
}
