package mod.arcomit.lucisrenderlib.core.pass;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * 后处理渲染通道的基类，用于实现自定义着色器效果
 */
public class PassBase {
    protected EffectInstance effect;  // 着色器效果实例

    public PassBase(EffectInstance effect) {
        this.effect = effect;
    }

    public PassBase(ResourceLocation shaderLocation, ResourceManager resourceManager) throws IOException {
        this(new EffectInstance(resourceManager, shaderLocation.toString()));
    }

    public void process(RenderTarget inTarget, RenderTarget outTarget) {
        process(inTarget, outTarget, null);
    }

    public void process(RenderTarget inTarget, RenderTarget outTarget, Consumer<EffectInstance> uniformConsumer) {
        RenderSystem.viewport(0, 0, outTarget.width, outTarget.height);

        this.effect.setSampler("DiffuseSampler", inTarget::getColorTextureId);
        this.effect.safeGetUniform("ProjMat").set(orthographic(outTarget));  // 投影矩阵
        this.effect.safeGetUniform("OutSize").set((float) outTarget.width, (float) outTarget.height);  // 输出尺寸

        if (uniformConsumer != null) {
            uniformConsumer.accept(effect);
        }

        this.effect.apply();

        pushVertex(outTarget);

        this.effect.clear();
        outTarget.unbindWrite();  // 解绑输出目标
    }

    protected static Matrix4f orthographic(RenderTarget out) {
        return new Matrix4f().setOrtho(0.0F, (float) out.width, 0.0F, (float) out.height, 0.1F, 1000.0F);
    }

    public void pushVertex(RenderTarget outTarget) {
        // 准备输出目标
        if (outTarget != Minecraft.getInstance().getMainRenderTarget()){
            outTarget.clear(Minecraft.ON_OSX);  // 清除缓冲区
        }
        outTarget.bindWrite(false);         // 绑定为写入目标

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        // 构建全屏四边形
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        // 四个顶点（覆盖整个屏幕，Z值固定为500）
        bufferbuilder.vertex(0.0D, 0.0D, 500.0D).endVertex();
        bufferbuilder.vertex(outTarget.width, 0.0D, 500.0D).endVertex();
        bufferbuilder.vertex(outTarget.width, outTarget.height, 500.0D).endVertex();
        bufferbuilder.vertex(0.0D, outTarget.height, 500.0D).endVertex();

        // 提交绘制
        BufferUploader.draw(bufferbuilder.end());

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }
}