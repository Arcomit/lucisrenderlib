package mod.arcomit.lucisrenderlib.builtin.pipeline;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.arcomit.lucisrenderlib.core.postprocessing.pipeline.LevelPostPipeline;
import mod.arcomit.lucisrenderlib.builtin.init.Passes;
import mod.arcomit.lucisrenderlib.core.postprocessing.target.ScaledTarget;
import mod.arcomit.lucisrenderlib.core.postprocessing.target.TargetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import static net.minecraft.client.Minecraft.ON_OSX;

public class BloomPipeline extends LevelPostPipeline {
    public BloomPipeline(ResourceLocation name) {
        super(name);
    }

    RenderTarget[] blur;   // 下采样链（分辨率递减）
    RenderTarget[] blur_;  // 上采样链（分辨率递增，混合结果）
    RenderTarget temp;     // 临时渲染目标

    void initTargets(RenderTarget inTarget) {
        int cnt = 5; // 模糊层级数量（5级）

        // 初始化下采样目标链
        if (blur == null) {
            blur = new RenderTarget[cnt];
            float scale = 1.0f; // 初始缩放因子

            // 创建各级下采样目标
            for (int i = 0; i < blur.length; i++) {
                scale /= 2; // 每级分辨率减半
                // 创建缩放渲染目标
                blur[i] = new ScaledTarget(scale, scale, inTarget.width, inTarget.height, false, ON_OSX);
                blur[i].setClearColor(0.0F, 0.0F, 0.0F, 0.0F); // 透明背景
                blur[i].clear(ON_OSX); // 清除目标
                // 如果主目标启用模板缓冲，则当前目标也启用
                if (inTarget.isStencilEnabled())
                    blur[i].enableStencil();
            }
        }

        // 初始化上采样目标链（比下采样少一级）
        if (blur_ == null) {
            blur_ = new RenderTarget[cnt - 1];
            float scale = 1.0f;
            for (int i = 0; i < blur_.length; i++) {
                scale /= 2;
                blur_[i] = new ScaledTarget(scale, scale, inTarget.width, inTarget.height, false, ON_OSX);
                blur_[i].setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                blur_[i].clear(ON_OSX);
                if (inTarget.isStencilEnabled())
                    blur_[i].enableStencil();
            }
        }

        // 初始化临时渲染目标
        if (temp == null) {
            temp = TargetManager.createTempTarget(inTarget); // 创建与主目标相同尺寸的临时目标
        }

        // 窗口大小变化时调整所有目标尺寸
        if (temp.width != inTarget.width || temp.height != inTarget.height) {
            // 调整下采样目标尺寸
            for (int i = 0; i < blur.length; i++) {
                blur[i].resize(inTarget.width, inTarget.height, ON_OSX);
            }
            // 调整上采样目标尺寸
            for (int i = 0; i < blur_.length; i++) {
                blur_[i].resize(inTarget.width, inTarget.height, ON_OSX);
            }
            // 调整临时目标尺寸
            temp.resize(inTarget.width, inTarget.height, ON_OSX);
        }
    }

    void handlePasses(RenderTarget inTarget) {
        // 设置纹理参数（边缘处理与过滤）
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL12.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL12.GL_LINEAR);

        // === 下采样链：逐步降低分辨率（创建模糊层级）===
        // 原始 -> 1/2分辨率
        Passes.downSampling.process(inTarget, blur[0]);    // 2倍下采样
        // 1/2 -> 1/4分辨率
        Passes.downSampling.process(blur[0], blur[1]); // 4倍下采样
        // 1/4 -> 1/8分辨率
        Passes.downSampling.process(blur[1], blur[2]); // 8倍下采样
        // 1/8 -> 1/16分辨率
        Passes.downSampling.process(blur[2], blur[3]); // 16倍下采样
        // 1/16 -> 1/32分辨率
        Passes.downSampling.process(blur[3], blur[4]); // 32倍下采样

        // === 上采样链：逐步恢复分辨率并混合模糊效果 ===
        // 32倍 -> 16倍（混合原始16倍图像）
        Passes.upSampling.process(blur[4], blur_[3], blur[3]);  // 32倍下采样 + 原始16倍 -> 混合16倍
        // 16倍 -> 8倍（混合原始8倍图像）
        Passes.upSampling.process(blur_[3], blur_[2], blur[2]);   // 混合16倍 + 原始8倍 -> 混合8倍
        // 8倍 -> 4倍（混合原始4倍图像）
        Passes.upSampling.process(blur_[2], blur_[1], blur[1]);  // 混合8倍 + 原始4倍 -> 混合4倍
        // 4倍 -> 2倍（混合原始2倍图像）
        Passes.upSampling.process(blur_[1], blur_[0], blur[0]);  // 混合4倍 + 原始2倍 -> 混合2倍

        // 最终合成：混合2倍模糊 + 原始图像 + 临时目标 -> 主渲染目标
        Passes.unityComposite.process(
                blur_[0],      // 最终模糊结果（2倍混合）
                temp,          // 临时渲染目标（中间存储）
                inTarget,           // 原始图像
                Minecraft.getInstance().getMainRenderTarget() // 主渲染目标
        );

        // 将结果从临时目标复制到主渲染目标
        Passes.blit.process(temp, Minecraft.getInstance().getMainRenderTarget());
    }
    
    @Override
    public void HandlePostProcessing(RenderTarget inTarget) {
        initTargets(inTarget);
        handlePasses(inTarget);
    }
}
