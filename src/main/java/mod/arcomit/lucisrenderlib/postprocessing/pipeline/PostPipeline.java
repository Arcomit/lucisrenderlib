package mod.arcomit.lucisrenderlib.postprocessing.pipeline;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import mod.arcomit.lucisrenderlib.postprocessing.init.PostPasses;
import mod.arcomit.lucisrenderlib.postprocessing.target.TargetManager;
import net.minecraft.resources.ResourceLocation;

import static net.minecraft.client.Minecraft.ON_OSX;

public abstract class PostPipeline implements Comparable<PostPipeline> {
    public final ResourceLocation name; // 管线唯一标识
    protected RenderTarget opaqueTarget;
    public boolean useOpaqueTarget = false;
    protected RenderTarget translucentTarget;
    public boolean useTranslucentTarget = false;
    public int priority = 0;

    public PostPipeline(ResourceLocation name) {
        this.name = name;
        PostPipelineManager.PostPipelineSet.add(this);
    }

    // 不透明
    public void bindOpaqueWrite(){
        if (opaqueTarget == null) {
            opaqueTarget = TargetManager.getTarget(name.toString() + "_opaque");
            opaqueTarget.clear(ON_OSX);
        }
        opaqueTarget.copyDepthFrom(TargetManager.getActiveRenderTarget());
        opaqueTarget.bindWrite(false);
        if (!useOpaqueTarget) useOpaqueTarget = true;
    }

    public void unbindOpaqueWrite(){
        opaqueTarget.unbindWrite();
        opaqueTarget.unbindRead();

        RenderTarget mainRenderTarget = TargetManager.getActiveRenderTarget();
        mainRenderTarget.copyDepthFrom(opaqueTarget);
        mainRenderTarget.bindWrite(false);
    }

    // 半透明
    public void bindTranslucentWrite(){
        if (translucentTarget == null) {
            translucentTarget = TargetManager.getTarget(name.toString() + "_translucent");
            translucentTarget.clear(ON_OSX);
        }
        translucentTarget.copyDepthFrom(TargetManager.getActiveRenderTarget());
        translucentTarget.bindWrite(false);
        if (!useTranslucentTarget) useTranslucentTarget = true;
    }

    public void unbindTranslucentWrite(){
        translucentTarget.unbindWrite();
        translucentTarget.unbindRead();

        RenderTarget mainRenderTarget = TargetManager.getActiveRenderTarget();
        mainRenderTarget.copyDepthFrom(translucentTarget);
        mainRenderTarget.bindWrite(false);
    }

    public void HandleOpaque(){
        depthCull(opaqueTarget,PostPipelineManager.globaDepthTarget);
        HandlePostProcessing(opaqueTarget);
        opaqueTarget = null;
    }
    protected static final ResourceLocation depth_cull_temp = Lucisrenderlib.prefix("depth_cull_temp");
    public static void depthCull(RenderTarget inTarget, RenderTarget globaDepthTarget) {
        RenderTarget tempTarget = TargetManager.getTarget(depth_cull_temp);
        PostPasses.depth_cull.process(inTarget, globaDepthTarget, tempTarget);
        PostPasses.blit.process(tempTarget,inTarget);
        TargetManager.releaseTarget(depth_cull_temp);
    }

    public void HandleTranslucent(){
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        HandlePostProcessing(translucentTarget);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        translucentTarget = null;
    }

    // 执行后处理
    public abstract void HandlePostProcessing(RenderTarget inTarget);

    public boolean isActive(){
        return useOpaqueTarget || useTranslucentTarget;
    }

    @Override
    public int compareTo(PostPipeline otherPipeline) {
        // 优先级比较：数值大的优先执行
        return Integer.compare(otherPipeline.priority, this.priority);
    }
}
