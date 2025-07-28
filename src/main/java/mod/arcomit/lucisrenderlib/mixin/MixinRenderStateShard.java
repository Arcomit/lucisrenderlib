package mod.arcomit.lucisrenderlib.mixin;

import mod.arcomit.lucisrenderlib.test.block.RenderTest;
import mod.arcomit.lucisrenderlib.test.block.RenderTest2;
import mod.arcomit.lucisrenderlib.test.block.RenderTest3;
import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderStateShard.class)
public class MixinRenderStateShard {

    @Inject(method = "setupRenderState()V", at = @At("TAIL"))
    private void afterSetupRenderState(CallbackInfo ci) {
        //System.out.println("欧克这是" + this);
        RenderTest2.AfterSetupRenderState((RenderStateShard)(Object)this);
    }
}
