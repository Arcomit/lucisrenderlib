package mod.arcomit.lucisrenderlib.test.block;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import mod.arcomit.lucisrenderlib.core.postprocessing.event.InPostPipelineRenderEvent;
import mod.arcomit.lucisrenderlib.utils.IrisUtils;
import mod.arcomit.lucisrenderlib.utils.RenderUtils;
import net.irisshaders.batchedentityrendering.impl.WrappableRenderType;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static mod.arcomit.lucisrenderlib.test.block.TriangleBlockRenderer.*;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Lucisrenderlib.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RenderTest {


    // 顶点数组对象和顶点缓冲对象
    private static int VAO;
    private static int VBO;
    private static int VBO2;
    private static int EBO;
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

    public static Obj LoaderOBJ(ResourceLocation location) throws IOException {
        InputStream stream = Minecraft.getInstance().getResourceManager().getResource(location).get().open();
        Obj obj = ObjReader.read(stream);
        obj = ObjUtils.convertToRenderable(obj);
        return obj;
    }
    static int indexCount;
    private static void initGLResources(){
        try{
            Obj obj = LoaderOBJ(ResourceLocation.parse("lucisrenderlib:test/cube.obj"));

            IntBuffer indices = ObjData.getFaceVertexIndices(obj, 3);
            FloatBuffer vertices = ObjData.getVertices(obj);
            FloatBuffer texCoords = ObjData.getTexCoords(obj, 2);
            indexCount = indices.capacity();
            // 创建并绑定VAO
            VAO = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(VAO);

            // 创建并绑定VBO
            VBO = GL30.glGenBuffers();
            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, VBO);
            GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertices, GL30.GL_STATIC_DRAW);



            EBO = GL30.glGenBuffers();
            GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);
            GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indices, GL30.GL_STATIC_DRAW);



            // 设置顶点属性指针
            // 位置属性 (location = 0)
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
            GL20.glEnableVertexAttribArray(0);



            // 颜色属性 (location = 1)
//        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
//        int colorOffset = 3 * 3 * Float.BYTES;
//        GL20.glVertexAttribPointer(1, 4, GL11.GL_UNSIGNED_BYTE, true, 0, colorOffset);
//        GL20.glEnableVertexAttribArray(1);

            GL20.glDisableVertexAttribArray(1);  // 禁用属性数组
            GL20.glVertexAttrib4f(1, 1.0f, 1.0f, 1.0f, 1.0f);  // RGBA(1,1,1,1)

            // 解绑
            GL30.glBindVertexArray(0);
            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
            GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, 0);

            initialized = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 清理资源
    public static void cleanup() {
        if (initialized) {
            GL30.glDeleteVertexArrays(VAO);
            GL30.glDeleteBuffers(VBO);
        }
    }


    public static boolean initialized2 = false;

    private static RenderStateShard currentRenderStateShard;
    public static void AfterSetupRenderState(RenderStateShard renderStateShard) {
        currentRenderStateShard = ((renderStateShard instanceof WrappableRenderType) ? ((WrappableRenderType)renderStateShard).unwrap() : renderStateShard);
    }

    static PoseStack poseStack = new PoseStack();
    static float partialTick = 0;

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            tick = 0;
        }
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;

        poseStack = RenderUtils.copyPoseStack(event.getPoseStack());
        partialTick = event.getPartialTick();

    }

    static int tick = 0;
    public static void RenderTest() {
        if (!initialized2) {
            int currentVAO = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
            int currentArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
            int currentElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
            if (currentRenderStateShard == null) return;
                if (currentRenderStateShard.equals(renderType)){
                    tick++;
                    System.out.println("tick: " + tick);

                    GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
                    initialized2 = true;
                    // 确保OpenGL资源已初始化
                    if (!initialized) {
                        initGLResources();
                    }
                    Minecraft minecraft = Minecraft.getInstance();
                    Player player = minecraft.player;
                    Camera camera = minecraft.gameRenderer.getMainCamera();
                    Vec3 cameraPos = camera.getPosition();


                    GL11.glDisable(GL11.GL_CULL_FACE);

                    // 保存当前着色器程序
                    previousShader = RenderSystem.getShader();

                    // 使用Minecraft的位置-颜色着色器 (POSITION_COLOR_SHADER)
                    ShaderInstance positionColorShader = RenderSystem.getShader();

                    // 绑定我们的顶点数组
                    GL30.glBindVertexArray(VAO);

                    // 移动到方块中心并应用旋转
                    poseStack.pushPose();
                    // 转换到相对摄像机的位置
                    poseStack.translate(
                            0 - cameraPos.x,
                            100 - cameraPos.y,
                            0 - cameraPos.z
                    );
                    PoseStack pose = TriangleBlockRenderer.pose;
                    pose.pushPose();
                    pose.translate(0.5, 0.5, 0.5);
                    float time = (minecraft.level.getGameTime() + partialTick) / 20.0f;
                    pose.mulPose(com.mojang.math.Axis.YP.rotation(time));
                    pose.mulPose(com.mojang.math.Axis.XP.rotation(time * 0.5f));

                    // 更新模型矩阵并设置到着色器
                    Matrix4f modelMatrix = poseStack.last().pose();
                    if (positionColorShader.MODEL_VIEW_MATRIX != null) {
                        positionColorShader.MODEL_VIEW_MATRIX.set(pose.last().pose());
                    }
                    if (positionColorShader.PROJECTION_MATRIX != null) {
                        positionColorShader.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
                    }
                    if (positionColorShader.INVERSE_VIEW_ROTATION_MATRIX != null) {
                        positionColorShader.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
                    }
                    positionColorShader.apply();
                    GL30.glVertexAttribI2i(3, overlay & '\uffff', overlay >> 16 & '\uffff');
                    GL30.glVertexAttribI2i(4, light & '\uffff', light >> 16 & '\uffff');
                    // 绘制三角形
//                    GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 3);
                    for (int i = 0; i < 5; i++) {
                        GL30.glDrawElements(
                                GL30.GL_TRIANGLES,        // 绘制模式
                                indexCount,        // 关键：索引数量
                                GL30.GL_UNSIGNED_INT,     // 索引类型
                                0                    // 偏移量
                        );
                    }

                    GL30.glVertexAttribI2i(3, 0, 0);
                    GL30.glVertexAttribI2i(4, 0, 0);
                    RenderSystem.disableBlend();
                    RenderSystem.defaultBlendFunc();
                    // 解绑VAO
                    GL30.glBindVertexArray(0);
                    RenderSystem.disableBlend();
                    RenderSystem.defaultBlendFunc();
                    positionColorShader.clear();
                    // 恢复之前的着色器
                    if (previousShader != null) {
                        previousShader.apply();
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    }
                    pose.popPose();
                    poseStack.popPose();


                    GL11.glEnable(GL11.GL_CULL_FACE);
//                    List<BlockEntity> canLookEntity = VisibleBlockEntityFinder.getBlockEntitiesInRadius(player.blockPosition(),100);
//                    for (BlockEntity be : canLookEntity) {
////                        if (be instanceof MagicBlockEntity entity){
///
////                        }
//                    }

                }
            GL30.glBindVertexArray(currentVAO);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentArrayBuffer);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer);
        }
    }
}
