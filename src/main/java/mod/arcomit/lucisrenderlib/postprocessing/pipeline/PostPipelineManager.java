package mod.arcomit.lucisrenderlib.postprocessing.pipeline;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.pipeline.RenderTarget;
import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import mod.arcomit.lucisrenderlib.postprocessing.target.TargetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Lucisrenderlib.MODID)
public class PostPipelineManager {
    public static final Set<PostPipeline> PostPipelineSet = new HashSet<>();// 全部的后处理管线
    public static final PriorityQueue<PostPipeline> LevelPostPipelineQueue = Queues.newPriorityQueue();// 优先级排序用
    private static final ResourceLocation globaDepthTargetID = Lucisrenderlib.prefix("globa_depth");
    public static RenderTarget globaDepthTarget;

    @SubscribeEvent
    public static void onRenderPost(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;

        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
        globaDepthTarget = TargetManager.getTarget(globaDepthTargetID);
        globaDepthTarget.copyDepthFrom(mainTarget);
        globaDepthTarget.unbindWrite();

        LevelPostPipelineQueue.clear();
        PostPipelineSet.stream()
                .filter(p -> p.isActive() && p instanceof LevelPostPipeline)
                .forEach(LevelPostPipelineQueue::add);

        //渲染后处理
        PostPipeline postPipeline;
        while (!LevelPostPipelineQueue.isEmpty()) {
            postPipeline = LevelPostPipelineQueue.poll(); // 获取最高优先级管线
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

        mainTarget.copyDepthFrom(globaDepthTarget);
        mainTarget.bindWrite(false);

        //TargetManager.releaseAll();
    }
}
