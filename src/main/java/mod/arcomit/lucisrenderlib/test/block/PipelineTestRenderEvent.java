package mod.arcomit.lucisrenderlib.test.block;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import mod.arcomit.lucisrenderlib.builtin.init.PostPipelines;
import mod.arcomit.lucisrenderlib.core.postprocessing.event.InPostPipelineRenderEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Lucisrenderlib.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PipelineTestRenderEvent {
    // 着色器程序ID
    private static int shaderProgram;
    // 顶点数组对象ID
    private static int VAO;
    // 顶点缓冲对象ID
    private static int VBO;
    // 是否已初始化OpenGL资源
    private static boolean initialized = false;
    private static void initGLResources() {
        // 顶点数据 - 位置和颜色
        float[] vertices = {
                // 位置          // 颜色
                -0.5f, -0.5f, 0.0f,  1.0f, 0.0f, 0.0f, // 左下 - 红色
                0.5f, -0.5f, 0.0f,  0.0f, 1.0f, 0.0f, // 右下 - 绿色
                0.0f,  0.5f, 0.0f,  0.0f, 0.0f, 1.0f  // 顶部 - 蓝色
        };

        // 创建VAO
        VAO = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(VAO);

        // 创建VBO
        VBO = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, VBO);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertices, GL30.GL_STATIC_DRAW);

        // 位置属性
        GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, 6 * Float.BYTES, 0);
        GL30.glEnableVertexAttribArray(0);

        // 颜色属性
        GL30.glVertexAttribPointer(1, 3, GL30.GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        GL30.glEnableVertexAttribArray(1);

        // 解绑
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        // 顶点着色器源码
        String vertexShaderSource = "#version 330 core\n" +
                "layout (location = 0) in vec3 aPos;\n" +
                "layout (location = 1) in vec3 aColor;\n" +
                "out vec3 ourColor;\n" +
                "uniform mat4 model;\n" +
                "uniform mat4 view;\n" +
                "uniform mat4 projection;\n" +
                "void main() {\n" +
                "   gl_Position = projection * view * model * vec4(aPos, 1.0);\n" +
                "   ourColor = aColor;\n" +
                "}";

        // 片段着色器源码
        String fragmentShaderSource = "#version 330 core\n" +
                "in vec3 ourColor;\n" +
                "out vec4 FragColor;\n" +
                "void main() {\n" +
                "   FragColor = vec4(ourColor, 1.0);\n" +
                "}";

        // 编译着色器
        int vertexShader = GL30.glCreateShader(GL30.GL_VERTEX_SHADER);
        GL30.glShaderSource(vertexShader, vertexShaderSource);
        GL30.glCompileShader(vertexShader);
        checkCompileErrors(vertexShader, "VERTEX");

        int fragmentShader = GL30.glCreateShader(GL30.GL_FRAGMENT_SHADER);
        GL30.glShaderSource(fragmentShader, fragmentShaderSource);
        GL30.glCompileShader(fragmentShader);
        checkCompileErrors(fragmentShader, "FRAGMENT");

        // 创建着色器程序
        shaderProgram = GL30.glCreateProgram();
        GL30.glAttachShader(shaderProgram, vertexShader);
        GL30.glAttachShader(shaderProgram, fragmentShader);
        GL30.glLinkProgram(shaderProgram);
        checkCompileErrors(shaderProgram, "PROGRAM");

        // 删除着色器对象
        GL30.glDeleteShader(vertexShader);
        GL30.glDeleteShader(fragmentShader);

        initialized = true;
    }

    private static void checkCompileErrors(int shader, String type) {
        int success;
        if (type.equals("PROGRAM")) {
            success = GL30.glGetProgrami(shader, GL30.GL_LINK_STATUS);
            if (success == GL11.GL_FALSE) {
                String log = GL30.glGetProgramInfoLog(shader);
                System.err.println("PROGRAM LINKING ERROR: " + log);
            }
        } else {
            success = GL30.glGetShaderi(shader, GL30.GL_COMPILE_STATUS);
            if (success == GL11.GL_FALSE) {
                String log = GL30.glGetShaderInfoLog(shader);
                System.err.println(type + " SHADER COMPILATION ERROR: " + log);
            }
        }
    }

    // 清理资源
    public static void cleanup() {
        if (initialized) {
            GL30.glDeleteVertexArrays(VAO);
            GL30.glDeleteBuffers(VBO);
            GL30.glDeleteProgram(shaderProgram);
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
                PostPipelines.bloom.bindOpaqueWrite();

                // 保存当前OpenGL状态
                //RenderSystem.backupProjectionMatrix();
                //RenderSystem.backupModelViewMatrix();

                GL11.glDisable(GL11.GL_CULL_FACE);
                GL11.glDepthMask(true);

                Matrix4f projMat = RenderSystem.getProjectionMatrix();

                // 设置视图矩阵
                poseStack.pushPose();

                // 转换到相对摄像机的位置
                poseStack.translate(
                        entity.getBlockPos().getX() - cameraPos.x,
                        entity.getBlockPos().getY() - cameraPos.y,
                        entity.getBlockPos().getZ() - cameraPos.z
                );
                poseStack.translate(0.5, 0.5, 0.5); // 移动到方块中心

                // 应用旋转
                float time = (entity.getLevel().getGameTime() + partialTicks) / 20.0f;
                poseStack.mulPose(com.mojang.math.Axis.YP.rotation(time));
                poseStack.mulPose(com.mojang.math.Axis.XP.rotation(time * 0.5f));

                // 设置模型视图矩阵
                Matrix4f modelViewMat = new Matrix4f(poseStack.last().pose());
                poseStack.popPose();

                // 设置投影矩阵（使用Minecraft的投影矩阵）
                Matrix4f projectionMat = new Matrix4f(projMat);

                // 使用我们的着色器程序
                GL30.glUseProgram(shaderProgram);

                // 传递矩阵到着色器
                int modelLoc = GL30.glGetUniformLocation(shaderProgram, "model");
                GL30.glUniformMatrix4fv(modelLoc, false, modelViewMat.get(new float[16]));

                int viewLoc = GL30.glGetUniformLocation(shaderProgram, "view");
                GL30.glUniformMatrix4fv(viewLoc, false, new Matrix4f().get(new float[16])); // 单位矩阵

                int projLoc = GL30.glGetUniformLocation(shaderProgram, "projection");
                GL30.glUniformMatrix4fv(projLoc, false, projectionMat.get(new float[16]));

                // 绘制三角形
                GL30.glBindVertexArray(VAO);
                GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 3);

                // 解绑
                GL30.glBindVertexArray(0);
                GL30.glUseProgram(0);

                PostPipelines.bloom.unbindOpaqueWrite();

                GL11.glEnable(GL11.GL_CULL_FACE);
            }
        }
    }
}
