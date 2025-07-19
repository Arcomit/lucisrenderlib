package mod.arcomit.lucisrenderlib.postprocessing.particle.rendertype;

import mod.arcomit.lucisrenderlib.postprocessing.pipeline.PostPipeline;
import net.minecraft.client.particle.ParticleRenderType;

public abstract class PostParticleRenderType implements ParticleRenderType {
    public abstract PostPipeline getPipeline();
}
