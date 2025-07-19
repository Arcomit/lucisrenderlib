package mod.arcomit.lucisrenderlib.mixin;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import mod.arcomit.lucisrenderlib.postprocessing.pipeline.LevelPostPipeline;
import mod.arcomit.lucisrenderlib.postprocessing.pipeline.PostPipeline;
import mod.arcomit.lucisrenderlib.postprocessing.pipeline.PostPipelineManager;
import mod.arcomit.lucisrenderlib.postprocessing.target.TargetManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.PriorityQueue;

@Mixin(value = GameRenderer.class, priority = -1000)
public abstract class MixinGameRenderer {

    @Unique
    private static final PriorityQueue<PostPipeline> PostPipelineQueue = Queues.newPriorityQueue();

    @Inject(method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V",
                    shift = At.Shift.AFTER, //之后
                    ordinal = 0
            ))
    private void PostRender(float pt, long startTime, boolean tick, CallbackInfo cbi){
        PostPipelineQueue.clear();
        PostPipelineManager.PostPipelineSet.stream()
                .filter(p -> p.isActive() && !(p instanceof LevelPostPipeline))
                .forEach(PostPipelineQueue::add);

        //渲染后处理
        PostPipeline postPipeline;
        while (!PostPipelineQueue.isEmpty()) {
            postPipeline = PostPipelineQueue.poll(); // 获取最高优先级管线
            // 执行后处理
            if (postPipeline.useOpaqueTarget) {
                postPipeline.HandleOpaque();
                postPipeline.useOpaqueTarget = false;
            }
            if (postPipeline.useTranslucentTarget) {
                postPipeline.HandleTranslucent();
                postPipeline.useTranslucentTarget = false;
            }
        }

        TargetManager.releaseAll();
    }

}
