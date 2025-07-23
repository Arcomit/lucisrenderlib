package mod.arcomit.lucisrenderlib.builtin.pass;

import com.mojang.blaze3d.pipeline.RenderTarget;
import mod.arcomit.lucisrenderlib.core.pass.PassBase;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public class UpSampling extends PassBase {

    public UpSampling(EffectInstance effect) {
        super(effect);
    }

    public UpSampling(ResourceLocation shaderLocation, ResourceManager resourceManager) throws IOException {
        super(shaderLocation, resourceManager);
    }

    public void process(RenderTarget inTarget, RenderTarget outTarget, RenderTarget downTexture) {
        super.process(inTarget,outTarget, effectInstance -> {
            effectInstance.setSampler("DownTexture", downTexture::getColorTextureId);
        });
    }
}
