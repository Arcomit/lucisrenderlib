package mod.arcomit.lucisrenderlib.mixin;

import mod.arcomit.lucisrenderlib.test.block.RenderTest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.VertexBuffer;

@Mixin(VertexBuffer.class)
public class MixinVertexBuffer {

    @Inject(method = "draw()V", at = @At("TAIL"))
    private void afterDraw(CallbackInfo ci) {
        //RenderTest.RenderTest();
    }
}
