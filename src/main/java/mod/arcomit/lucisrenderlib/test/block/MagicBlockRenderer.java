package mod.arcomit.lucisrenderlib.test.block;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import mod.arcomit.lucisrenderlib.builtin.init.PostPipelines;
import mod.arcomit.lucisrenderlib.utils.IrisUtils;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class MagicBlockRenderer implements BlockEntityRenderer<MagicBlockEntity> {

    public MagicBlockRenderer(BlockEntityRendererProvider.Context context) {}

    // 顶点数组对象和顶点缓冲对象
    private int VAO;
    private int VBO;
    private boolean initialized = false;

    // 存储当前着色器状态
    private ShaderInstance previousShader;

    private void createVertexData() {
        // 位置 (x, y, z) 和颜色 (r, g, b, a)
        float[] positions = {
                -0.5f, -0.5f, 0.0f,  // 左下
                0.5f, -0.5f, 0.0f,  // 右下
                0.0f,  0.5f, 0.0f   // 顶部
        };

        // 颜色值 (0-255) 对应红、绿、蓝
        byte[] colors = {
                (byte) 255, (byte) 0, (byte) 0, (byte) 255, // 红色
                (byte) 0, (byte) 255, (byte) 0, (byte) 255, // 绿色
                (byte) 0, (byte) 0, (byte) 255, (byte) 255  // 蓝色
        };

        // 合并位置和颜色到单个缓冲区
        ByteBuffer buffer = ByteBuffer.allocateDirect(positions.length * Float.BYTES + colors.length);
        buffer.order(ByteOrder.nativeOrder());

        // 添加位置数据 (浮点数)
        FloatBuffer floatBuffer = buffer.asFloatBuffer();
        floatBuffer.put(positions);

        // 添加颜色数据 (字节)
        buffer.position(positions.length * Float.BYTES);
        buffer.put(colors);

        // 准备上传到GPU
        buffer.flip();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, VBO);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, buffer, GL30.GL_STATIC_DRAW);
    }

    private void initGLResources() {
        // 创建并绑定VAO
        VAO = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(VAO);

        // 创建并绑定VBO
        VBO = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, VBO);

        // 三角形顶点数据（位置和颜色）
        createVertexData();

        // 设置顶点属性指针
        // 位置属性 (location = 0)
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(0);

        // 颜色属性 (location = 1)
//        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        int colorOffset = 3 * 3 * Float.BYTES;
        GL20.glVertexAttribPointer(1, 4, GL11.GL_UNSIGNED_BYTE, true, 0, colorOffset);
        GL20.glEnableVertexAttribArray(1);

        // 解绑
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        initialized = true;
    }

    // 清理资源
    public void cleanup() {
        if (initialized) {
            GL30.glDeleteVertexArrays(VAO);
            GL30.glDeleteBuffers(VBO);
        }
    }

    @Override
    public void render(MagicBlockEntity entity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // 确保OpenGL资源已初始化
        if (!initialized) {
            initGLResources();
        }

//        PostPipelines.bloom.bindOpaqueWrite();

        GL11.glDisable(GL11.GL_CULL_FACE);


        int currentVAO = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        int currentArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        int currentElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        // 保存当前着色器程序
        previousShader = RenderSystem.getShader();

        // 使用Minecraft的位置-颜色着色器 (POSITION_COLOR_SHADER)
        ShaderInstance positionColorShader = GameRenderer.getRendertypeLightningShader();

        if (positionColorShader != null) {
            WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
            if (pipeline instanceof ShaderRenderingPipeline) {
                if (positionColorShader.equals(((ShaderRenderingPipeline) pipeline).getShaderMap().getShader(ShaderKey.SHADOW_LIGHTNING))){
                    System.out.println("oh,yeah~");
                }else {
                    System.out.println("oh,no~");
                }
            }
        }


        // 设置着色器uniforms

//        for (int i = 0; i < 12; ++i) {
//            int j = RenderSystem.getShaderTexture(i);
//            positionColorShader.setSampler("Sampler" + i, j);
//        }

        if (positionColorShader.PROJECTION_MATRIX != null) {
            positionColorShader.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
        }
        if (positionColorShader.INVERSE_VIEW_ROTATION_MATRIX != null) {
            positionColorShader.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
        }
//        if (positionColorShader.TEXTURE_MATRIX != null) {
//            positionColorShader.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
//        }
//        if (positionColorShader.SCREEN_SIZE != null) {
//            Window window = Minecraft.getInstance().getWindow();
//            positionColorShader.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
//        }
//        if (positionColorShader.COLOR_MODULATOR != null) {
//            positionColorShader.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
//        }
//        if (positionColorShader.FOG_START != null) {
//            positionColorShader.FOG_START.set(RenderSystem.getShaderFogStart());
//        }
//        if (positionColorShader.FOG_END != null) {
//            positionColorShader.FOG_END.set(RenderSystem.getShaderFogEnd());
//        }
//        if (positionColorShader.FOG_COLOR != null) {
//            positionColorShader.FOG_COLOR.set(RenderSystem.getShaderFogColor());
//        }
//        if (positionColorShader.FOG_SHAPE != null) {
//            positionColorShader.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
//        }
//        if (positionColorShader.GAME_TIME != null) {
//            positionColorShader.GAME_TIME.set(RenderSystem.getShaderGameTime());
//        }
//        // 设置光影特定的uniforms（如果存在）
//        trySetShaderUniform(positionColorShader, "modelOffset", 0.0f, 0.0f, 0.0f);
//        trySetShaderUniform(positionColorShader, "alphaTestRef", 0.1f);
//        // 设置常见的光影uniforms
//        trySetShaderUniform(positionColorShader, "viewWidth", (float)Minecraft.getInstance().getWindow().getWidth());
//        trySetShaderUniform(positionColorShader, "viewHeight", (float)Minecraft.getInstance().getWindow().getHeight());
//        trySetShaderUniform(positionColorShader, "aspectRatio",
//                (float)Minecraft.getInstance().getWindow().getWidth() /
//                        (float)Minecraft.getInstance().getWindow().getHeight());
//
//// 设置时间相关uniforms
//        long worldTime = entity.getLevel().getDayTime();
//        trySetShaderUniform(positionColorShader, "worldTime", (int)(worldTime % 24000));
//        trySetShaderUniform(positionColorShader, "worldDay", (int)(worldTime / 24000));
//        trySetShaderUniform(positionColorShader, "frameTime", Minecraft.getInstance().getFrameTime());



        // 绑定我们的顶点数组
        GL30.glBindVertexArray(VAO);

        // 移动到方块中心并应用旋转
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        float time = (entity.getLevel().getGameTime() + partialTicks) / 20.0f;
        poseStack.mulPose(com.mojang.math.Axis.YP.rotation(time));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotation(time * 0.5f));

        // 更新模型矩阵并设置到着色器
        Matrix4f modelMatrix = poseStack.last().pose();
        if (positionColorShader.MODEL_VIEW_MATRIX != null) {
            positionColorShader.MODEL_VIEW_MATRIX.set(modelMatrix);
        }
        positionColorShader.apply();

        // 绘制三角形
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 3);

        // 解绑VAO
        GL30.glBindVertexArray(0);

        // 恢复之前的着色器
        if (previousShader != null) {
            previousShader.apply();
        }

        GL30.glBindVertexArray(currentVAO);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentArrayBuffer);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer);

        poseStack.popPose();

//        PostPipelines.bloom.unbindOpaqueWrite();
        GL11.glEnable(GL11.GL_CULL_FACE);

        // 恢复OpenGL状态
        //RenderSystem.restoreModelViewMatrix();
        //RenderSystem.restoreProjectionMatrix();
    }

    // 尝试设置着色器uniform（兼容不同着色器）
    private void trySetShaderUniform(ShaderInstance shader, String name, Matrix4f value) {
        try {
            shader.safeGetUniform(name).set(value);
        } catch (Exception e) {
            // Uniform不存在，忽略
        }
    }

    private void trySetShaderUniform(ShaderInstance shader, String name, float x, float y, float z) {
        try {
            shader.safeGetUniform(name).set(x, y, z);
        } catch (Exception e) {
            // Uniform不存在，忽略
        }
    }

    private void trySetShaderUniform(ShaderInstance shader, String name, float value) {
        try {
            shader.safeGetUniform(name).set(value);
        } catch (Exception e) {
            // Uniform不存在，忽略
        }
    }

}