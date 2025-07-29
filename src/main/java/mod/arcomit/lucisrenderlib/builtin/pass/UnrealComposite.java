package mod.arcomit.lucisrenderlib.builtin.pass;

import com.mojang.blaze3d.pipeline.RenderTarget;
import mod.arcomit.lucisrenderlib.core.pass.PassBase;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public class UnrealComposite extends PassBase {

    public UnrealComposite(EffectInstance effect) {
        super(effect);
    }

    public UnrealComposite(ResourceLocation shaderLocation, ResourceManager resourceManager) throws IOException {
        super(shaderLocation, resourceManager);
    }

    public void process(RenderTarget inTarget, RenderTarget outTarget,
                        RenderTarget blurTexture1,
                        RenderTarget blurTexture2,
                        RenderTarget blurTexture3
    ) {
        this.process(inTarget, outTarget, blurTexture1, blurTexture2, blurTexture3, 1.0f, 2f);
    }

    public void process(RenderTarget inTarget, RenderTarget outTarget,
                        RenderTarget blurTexture1,
                        RenderTarget blurTexture2,
                        RenderTarget blurTexture3,
                        float bloomRadius,
                        float bloomIntensive
    ) {
        super.process(inTarget,outTarget, effectInstance -> {
            effectInstance.setSampler("BlurTexture1", blurTexture1::getColorTextureId);
            effectInstance.setSampler("BlurTexture2",  blurTexture2::getColorTextureId);
            effectInstance.setSampler("BlurTexture3",  blurTexture3::getColorTextureId);
            effectInstance.safeGetUniform("BloomRadius").set(bloomRadius);
            effectInstance.safeGetUniform("BloomIntensive").set(bloomIntensive);
        });
    }
}
