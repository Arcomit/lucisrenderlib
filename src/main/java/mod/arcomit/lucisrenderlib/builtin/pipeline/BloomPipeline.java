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
        int minDimension = Math.min(inTarget.width, inTarget.height);
        int maxLevels = (int)(Math.log(minDimension) / Math.log(2)) - 2;
        int cnt = Math.max(3, Math.min(8, maxLevels)); // 3-8级模糊层级，根据分辨率动态调整

        // 初始化下采样目标链
        if (blur == null) {
            blur = new RenderTarget[cnt];
            float scale = 1.0f; // 初始缩放因子

            // 创建各级下采样目标
            for (int i = 0; i < blur.length; i++) {
                scale /= 2; // 每级分辨率减半
                // 创建缩放渲染目标
                blur[i] = new ScaledTarget(scale, scale, inTarget.width, inTarget.height, false, ON_OSX);
                blur[i].setFilterMode(GL11.GL_LINEAR);
                blur[i].clear(ON_OSX);
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
                blur_[i].setFilterMode(GL11.GL_LINEAR);
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
        // 设置纹理参数
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL12.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL12.GL_LINEAR);

        // === 动态下采样链 ===
        // 首级下采样：从原始图像到第一级模糊
        if (blur.length > 0) {
            Passes.downSampling.process(inTarget, blur[0]);
        }

        // 后续下采样：逐级缩小
        for (int i = 1; i < blur.length; i++) {
            Passes.downSampling.process(blur[i - 1], blur[i]);
        }

        // === 动态上采样链 ===
        // 从最底层开始向上混合（注意：blur_.length = blur.length - 1）
        for (int i = blur.length - 1; i > 0; i--) {
            if (i == blur.length - 1) {
                // 最底层特殊处理：直接使用最底层模糊图
                Passes.upSampling.process(blur[i], blur_[i - 1], blur[i - 1]);
            } else {
                // 混合当前上采样结果与上一级原始模糊图
                Passes.upSampling.process(blur_[i], blur_[i - 1], blur[i - 1]);
            }
        }

        // === 最终合成 ===
        if (blur_.length > 0) {
            // 使用混合后的最小模糊图（blur_[0]）
            Passes.unityComposite.process(
                    blur_[0],      // 最终模糊结果
                    temp,          // 临时目标
                    inTarget,      // 原始图像
                    Minecraft.getInstance().getMainRenderTarget()
            );
        } else {
            // 如果只有一级模糊（blur_.length=0），则使用原始模糊图
            Passes.unityComposite.process(
                    blur[0],
                    temp,
                    inTarget,
                    Minecraft.getInstance().getMainRenderTarget()
            );
        }

        // 将结果复制到主渲染目标
        Passes.blit.process(temp, Minecraft.getInstance().getMainRenderTarget());
    }
    
    @Override
    public void HandlePostProcessing(RenderTarget inTarget) {
        initTargets(inTarget);
        handlePasses(inTarget);
    }
}
