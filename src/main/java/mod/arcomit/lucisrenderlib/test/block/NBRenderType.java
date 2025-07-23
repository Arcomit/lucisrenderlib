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
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(MAIN_TARGET)
                .createCompositeState(false);
        return RenderType.create("lightning233", DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.TRIANGLES, 256, false, true, state);
    }
}
