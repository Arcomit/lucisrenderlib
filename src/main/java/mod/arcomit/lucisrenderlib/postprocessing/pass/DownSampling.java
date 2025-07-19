package mod.arcomit.lucisrenderlib.postprocessing.pass;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public class DownSampling extends PostPassBase{

    public DownSampling(EffectInstance effect) {
        super(effect);
    }

    public DownSampling(ResourceLocation shaderLocation, ResourceManager resourceManager) throws IOException {
        super(shaderLocation, resourceManager);
    }

    @Override
    public void process(RenderTarget inTarget, RenderTarget outTarget) {
        super.process(inTarget,outTarget, effectInstance -> {
            effectInstance.safeGetUniform("InSize").set((float) inTarget.width, (float) inTarget.height);
        });
    }
}
