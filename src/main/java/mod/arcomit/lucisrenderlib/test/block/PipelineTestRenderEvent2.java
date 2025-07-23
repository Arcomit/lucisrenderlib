package mod.arcomit.lucisrenderlib.test.block;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import mod.arcomit.lucisrenderlib.builtin.init.PostPipelines;
import mod.arcomit.lucisrenderlib.core.postprocessing.event.InPostPipelineRenderEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Lucisrenderlib.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PipelineTestRenderEvent2 {

    // 顶点数组对象和顶点缓冲对象
    private static int VAO;
    private static int VBO;
    private static boolean initialized = false;

    // 存储当前着色器状态
    private static ShaderInstance previousShader;

    private static void createVertexData() {
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

    private static void initGLResources() {
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
    public static void cleanup() {
        if (initialized) {
            GL30.glDeleteVertexArrays(VAO);
            GL30.glDeleteBuffers(VBO);
        }
    }


    //@SubscribeEvent
    public static void onRender(InPostPipelineRenderEvent.Opaque event) {
        // 确保OpenGL资源已初始化
        if (!initialized) {
            initGLResources();
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        List<BlockEntity> canLookEntity = VisibleBlockEntityFinder.getBlockEntitiesInRadius(player.blockPosition(),100);
        for (BlockEntity be : canLookEntity) {
            if (be instanceof MagicBlockEntity entity){
                PoseStack poseStack = event.getPoseStack();
                float partialTicks = event.getPartialTick();

                GL11.glDisable(GL11.GL_CULL_FACE);

                // 保存当前着色器程序
                previousShader = RenderSystem.getShader();

                // 使用Minecraft的位置-颜色着色器 (POSITION_COLOR_SHADER)
                ShaderInstance positionColorShader = GameRenderer.getPositionColorShader();

                // 设置着色器uniforms

                for (int i = 0; i < 12; ++i) {
                    int j = RenderSystem.getShaderTexture(i);
                    //positionColorShader.setSampler("Sampler" + i, j);
                }

                if (positionColorShader.PROJECTION_MATRIX != null) {
                    positionColorShader.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
                }
                if (positionColorShader.INVERSE_VIEW_ROTATION_MATRIX != null) {
                    positionColorShader.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
                }
                if (positionColorShader.TEXTURE_MATRIX != null) {
                    positionColorShader.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
                }
                if (positionColorShader.SCREEN_SIZE != null) {
                    Window window = Minecraft.getInstance().getWindow();
                    positionColorShader.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
                }
                if (positionColorShader.COLOR_MODULATOR != null) {
                    positionColorShader.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
                }
                if (positionColorShader.FOG_START != null) {
                    positionColorShader.FOG_START.set(RenderSystem.getShaderFogStart());
                }
                if (positionColorShader.FOG_END != null) {
                    positionColorShader.FOG_END.set(RenderSystem.getShaderFogEnd());
                }
                if (positionColorShader.FOG_COLOR != null) {
                    positionColorShader.FOG_COLOR.set(RenderSystem.getShaderFogColor());
                }
                if (positionColorShader.FOG_SHAPE != null) {
                    positionColorShader.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
                }
                if (positionColorShader.GAME_TIME != null) {
                    positionColorShader.GAME_TIME.set(RenderSystem.getShaderGameTime());
                }




                // 绑定我们的顶点数组
                GL30.glBindVertexArray(VAO);

                // 移动到方块中心并应用旋转
                poseStack.pushPose();
                // 转换到相对摄像机的位置
                poseStack.translate(
                        entity.getBlockPos().getX() - cameraPos.x,
                        entity.getBlockPos().getY() - cameraPos.y,
                        entity.getBlockPos().getZ() - cameraPos.z
                );
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

                poseStack.popPose();

                GL11.glEnable(GL11.GL_CULL_FACE);
            }
        }
    }
}
