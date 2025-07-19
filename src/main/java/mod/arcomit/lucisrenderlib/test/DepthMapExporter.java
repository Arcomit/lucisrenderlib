package mod.arcomit.lucisrenderlib.test;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL30;

import java.io.File;
import java.nio.file.Path;

public class DepthMapExporter {

    public static boolean captureRequested = false;
    public static String customFileName = "custom_render";

    // 请求截图（在需要导出的地方调用此方法）
    public static void requestExport(String fileName) {
        captureRequested = true;
        customFileName = fileName;
    }

    public static RenderTarget getSource() {
        if (Minecraft.getInstance().levelRenderer.transparencyChain == null) {
            return Minecraft.getInstance().getMainRenderTarget(); // 主渲染目标
        } else {
            return Minecraft.getInstance().levelRenderer.getParticlesTarget(); // 粒子专用目标
        }
    }

    public static void exportCurrentRenderTarget() {
        Minecraft mc = Minecraft.getInstance();
        // 获取主渲染目标
        var renderTarget = getSource();
//        var renderTarget = mc.getMainRenderTarget();
        renderTarget.bindWrite(false);
//        // 暂存当前帧缓冲区
//        int originalFbo = GlStateManager._getInteger(GL30.GL_FRAMEBUFFER_BINDING);
//        renderTarget.bindWrite(true);

        try {
            // 创建NativeImage捕捉帧缓冲
            NativeImage image = takeScreenshot(renderTarget);

//            // 读取像素数据（可能需要格式转换）
//            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, renderTarget.frameBufferId);
//            image.downloadTexture(0, false); // 从当前绑定的FBO读取

            // 保存文件
            Path gameDir = mc.gameDirectory.toPath();
            Path outputPath = gameDir.resolve("screenshots").resolve(customFileName + ".png");
            image.writeToFile(outputPath);

            image.close(); // 重要：释放NativeImage资源
            mc.gui.getChat().addMessage(Component.literal("渲染目标已导出至: " + outputPath));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 恢复原始帧缓冲区
//            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, originalFbo);
        }
    }

    public static NativeImage takeScreenshot(RenderTarget pFramebuffer) {
        int i = pFramebuffer.width;
        int j = pFramebuffer.height;

        NativeImage depthImage = new NativeImage(
                NativeImage.Format.LUMINANCE, // 关键修改点
                i,
                j,
                false // 不使用Mipmap
        );
        depthImage.downloadDepthBuffer( 0);
        depthImage.flipY();
        return depthImage;
    }
}