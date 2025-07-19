package mod.arcomit.lucisrenderlib.postprocessing.particle.rendertype;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import mod.arcomit.lucisrenderlib.postprocessing.pipeline.PostPipeline;
import net.minecraft.client.renderer.texture.TextureManager;

public class OpaqueParticleRenderType extends PostParticleRenderType{
    @Override
    public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
        if (getPipeline() != null){
            getPipeline().bindOpaqueWrite();
        }
    }

    @Override
    public void end(Tesselator tesselator) {
        tesselator.end();
        if (getPipeline() != null){
            getPipeline().unbindOpaqueWrite();
        }
    }

    @Override
    public PostPipeline getPipeline() {
        return null;
    }
}
