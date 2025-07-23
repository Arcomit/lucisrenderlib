package mod.arcomit.lucisrenderlib.mixin.oculus;

import com.google.common.collect.Sets;
import mod.arcomit.lucisrenderlib.core.postprocessing.particle.rendertype.PostParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

// copy from EpicACG : https://github.com/dfdyz/EpicACG-1.20 (感谢东非大野猪佬开源QwQ)
@Mixin(ParticleEngine.class)
public abstract class MixinParticleEngineA {
    @Redirect(
            method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V"},
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;keySet()Ljava/util/Set;"
            ),
            remap = false
    )
    private Set<ParticleRenderType> lucisrenderlib$selectParticlesToRender(Map<ParticleRenderType, Queue<Particle>> instance) {
        Set<ParticleRenderType> keySet = instance.keySet();
        return Sets.filter(keySet, (type) -> !(type instanceof PostParticleRenderType));
    }
}
