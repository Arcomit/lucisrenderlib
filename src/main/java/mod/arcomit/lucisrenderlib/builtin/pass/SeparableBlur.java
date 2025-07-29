package mod.arcomit.lucisrenderlib.builtin.pass;

import com.mojang.blaze3d.pipeline.RenderTarget;
import mod.arcomit.lucisrenderlib.core.pass.PassBase;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public class SeparableBlur extends PassBase {

    public SeparableBlur(EffectInstance effect) {
        super(effect);
    }

    public SeparableBlur(ResourceLocation shaderLocation, ResourceManager resourceManager) throws IOException {
        super(shaderLocation, resourceManager);
    }

    public void process(RenderTarget inTarget, RenderTarget outTarget, float blurDirX, float blurDirY, int radius) {
        super.process(inTarget, outTarget, effectInstance -> {
            effectInstance.safeGetUniform("BlurDir").set(blurDirX, blurDirY);
            effectInstance.safeGetUniform("Radius").set(radius);
        });
    }
}
