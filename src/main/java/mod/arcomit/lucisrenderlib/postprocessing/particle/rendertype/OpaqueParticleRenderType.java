package mod.arcomit.lucisrenderlib.postprocessing.particle.rendertype;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import mod.arcomit.lucisrenderlib.postprocessing.pipeline.PostPipeline;
import net.minecraft.client.renderer.texture.TextureManager;

// 不透明粒子，默认深度写入
public class OpaqueParticleRenderType extends PostParticleRenderType{
    @Override
    public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.setShader(this::getShader);
        if (this.getPipeline() != null){
            this.getPipeline().bindOpaqueWrite();
        }
    }

    @Override
    public void end(Tesselator tesselator) {
        tesselator.end();
        if (this.getPipeline() != null){
            this.getPipeline().unbindOpaqueWrite();
        }
    }

    @Override
    public PostPipeline getPipeline() {
        return null;
    }
}
