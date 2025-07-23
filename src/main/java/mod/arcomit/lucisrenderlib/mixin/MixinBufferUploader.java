package mod.arcomit.lucisrenderlib.mixin;

import mod.arcomit.lucisrenderlib.test.block.RenderTest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;

@Mixin(BufferUploader.class)
public class MixinBufferUploader {

    @Inject(method = "_drawWithShader(Lcom/mojang/blaze3d/vertex/BufferBuilder$RenderedBuffer;)V", at = @At("RETURN"))
    private static void before_drawWithShader(BufferBuilder.RenderedBuffer renderedBuffer, CallbackInfo ci) {
        RenderTest.RenderTest();
    }
}
