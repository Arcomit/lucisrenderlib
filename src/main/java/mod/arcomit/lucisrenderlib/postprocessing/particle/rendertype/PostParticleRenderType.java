package mod.arcomit.lucisrenderlib.postprocessing.particle.rendertype;

import mod.arcomit.lucisrenderlib.postprocessing.pipeline.PostPipeline;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;

public abstract class PostParticleRenderType implements ParticleRenderType {
    public abstract PostPipeline getPipeline();

    // fuck iris,fuck oculus
    protected ShaderInstance getShader() {
        return GameRenderer.positionColorTexLightmapShader;
    }
}
