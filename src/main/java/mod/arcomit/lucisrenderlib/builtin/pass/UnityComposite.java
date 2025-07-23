package mod.arcomit.lucisrenderlib.builtin.pass;

import com.mojang.blaze3d.pipeline.RenderTarget;
import mod.arcomit.lucisrenderlib.core.pass.PassBase;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public class UnityComposite extends PassBase {

    public UnityComposite(EffectInstance effect) {
        super(effect);
    }

    public UnityComposite(ResourceLocation shaderLocation, ResourceManager resourceManager) throws IOException {
        super(shaderLocation, resourceManager);
    }

    public void process(RenderTarget inTarget, RenderTarget outTarget, RenderTarget downTexture, RenderTarget backGround) {
        super.process(inTarget,outTarget, effectInstance -> {
            effectInstance.setSampler("DownTexture", downTexture::getColorTextureId);
            effectInstance.setSampler("Background",  backGround::getColorTextureId);
        });
    }
}
