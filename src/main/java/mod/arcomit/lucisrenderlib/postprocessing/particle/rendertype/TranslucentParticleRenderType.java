package mod.arcomit.lucisrenderlib.postprocessing.particle.rendertype;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import mod.arcomit.lucisrenderlib.postprocessing.pipeline.PostPipeline;
import net.minecraft.client.renderer.texture.TextureManager;

// 半透明粒子，默认开启混合，深度不写入
public class TranslucentParticleRenderType extends PostParticleRenderType{
    @Override
    public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(this::getShader);
        if (this.getPipeline() != null){
            this.getPipeline().bindTranslucentWrite();
        }
    }

    @Override
    public void end(Tesselator tesselator) {
        tesselator.end();
        if (this.getPipeline() != null){
            this.getPipeline().unbindTranslucentWrite();
        }
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    @Override
    public PostPipeline getPipeline() {
        return null;
    }

}
