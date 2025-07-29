package mod.arcomit.lucisrenderlib.builtin.pipeline;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.arcomit.lucisrenderlib.core.postprocessing.pipeline.LevelPostPipeline;
import mod.arcomit.lucisrenderlib.builtin.init.Passes;
import mod.arcomit.lucisrenderlib.core.postprocessing.target.ScaledTarget;
import mod.arcomit.lucisrenderlib.core.postprocessing.target.TargetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.nio.file.Path;

import static net.minecraft.client.Minecraft.ON_OSX;

public class UnrealBloomPipeline extends LevelPostPipeline {
    public UnrealBloomPipeline(ResourceLocation name) {
        super(name);
    }

    ScaledTarget[] blur;   // 下采样链（分辨率递减）
    ScaledTarget[] blur_;  // 上采样链（分辨率递增，混合结果）
    RenderTarget temp;     // 临时渲染目标

    void initTargets(RenderTarget inTarget) {
        int cnt = 3;

        // 初始化下采样目标链
        if (blur == null) {
            blur = new ScaledTarget[cnt];
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
            blur_ = new ScaledTarget[cnt];
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
                blur[i].setFilterMode(GL11.GL_LINEAR);
            }
            // 调整上采样目标尺寸
            for (int i = 0; i < blur_.length; i++) {
                blur_[i].resize(inTarget.width, inTarget.height, ON_OSX);
                blur_[i].setFilterMode(GL11.GL_LINEAR);
            }
            // 调整临时目标尺寸
            temp.resize(inTarget.width, inTarget.height, ON_OSX);
            temp.setFilterMode(GL11.GL_LINEAR);
        }
    }

    void handlePasses(RenderTarget inTarget) {
        // 设置纹理参数
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL12.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL12.GL_LINEAR);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();


        Passes.blit.process(inTarget, Minecraft.getInstance().getMainRenderTarget());

        Passes.separableBlur.process(inTarget, blur[0], 1, 0 ,3);
        Passes.separableBlur.process(blur[0], blur_[0], 0,1 ,3);
        Passes.separableBlur.process(blur_[0], blur[1], 1, 0 ,5);
        Passes.separableBlur.process(blur[1], blur_[1], 0, 1 ,5);
        Passes.separableBlur.process(blur_[1], blur[2], 1, 0 ,7);
        Passes.separableBlur.process(blur[2], blur_[2], 0, 1 ,7);

        Passes.unrealComposite.process(Minecraft.getInstance().getMainRenderTarget(), Minecraft.getInstance().getMainRenderTarget(), blur_[0], blur_[1], blur_[2]);
    }

    @Override
    public void HandlePostProcessing(RenderTarget inTarget) {
        initTargets(inTarget);
        handlePasses(inTarget);
    }
}
