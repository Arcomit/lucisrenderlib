package mod.arcomit.lucisrenderlib.postprocessing.pass;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import java.io.IOException;

public class DepthCull extends PostPassBase {

    public DepthCull(EffectInstance effect) {
        super(effect);
    }

    public DepthCull(ResourceLocation resourceLocation, ResourceManager resmgr) throws IOException {
        super(resourceLocation, resmgr);
    }

    public void process(RenderTarget inTarget, RenderTarget globaDepthTarget, RenderTarget outTarget) {
        super.process(inTarget,outTarget, effectInstance -> {
            effectInstance.setSampler("SourceDepth", inTarget::getDepthTextureId);
            effectInstance.setSampler("GlobalDepth", globaDepthTarget::getDepthTextureId);
        });
    }
}
