package mod.arcomit.lucisrenderlib.core.obj;

import com.jme3.util.mikktspace.MikkTSpaceContext;
import com.jme3.util.mikktspace.MikktspaceTangentGenerator;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.javagl.obj.*;
import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import mod.arcomit.lucisrenderlib.core.obj2.obj.Face;
import mod.arcomit.lucisrenderlib.core.obj2.obj.WavefrontObject;
import mod.arcomit.lucisrenderlib.utils.IrisUtils;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.fml.ModList;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntConsumer;

import static mod.arcomit.lucisrenderlib.test.block.RenderTest2.currentRenderStateShard;
import static mod.arcomit.lucisrenderlib.test.block.TriangleBlockRenderer.renderType2;
import static net.irisshaders.iris.pipeline.programs.ShaderKey.ENTITIES_EYES_TRANS;
import static net.irisshaders.iris.pipeline.programs.ShaderKey.SHADOW_ENTITIES_CUTOUT;
import static org.lwjgl.opengl.GL11.*;

public class OBJModel {
    protected final Obj obj;
    protected final Map<String, OBJModel> group = new ConcurrentHashMap<>();
    protected int VAO;
    protected int VBO;
    protected int EBO;
    protected int indexCount;// 索引计数
    protected boolean initialized = false;
    private int diffuseTextureId = -1;
    private int normalsTextureID = -1;
    private int specularTextureID = -1;
    private int emissiveTextureID = -1;

    public OBJModel(Obj obj) {
        this.obj = obj;
    }

    public void render(String groupName, ResourceLocation texture, PoseStack poseStack, int light, int overlay) {
        if (group.containsKey(groupName)) {
            OBJModel model = group.get(groupName);
            model.render(texture, poseStack, light, overlay,groupName);
        }else {
            ObjGroup group = obj.getGroup(groupName);
            if (group != null) {
                Obj groupObj = ObjUtils.groupToObj(obj, group, null);
                groupObj = ObjUtils.convertToRenderable(groupObj);
                OBJModel model = new OBJModel(groupObj);
                model.render(texture, poseStack, light, overlay,groupName);
                this.group.put(groupName, model);
            }else {
                //Lucisrenderlib.LOGGER.error("The obj model does not have groups: " + groupName);
            }
        }

    }

    public void render(ResourceLocation texture, PoseStack poseStack, int light, int overlay,String groupName) {
        // 初始化
        if (!initialized) {
            init(groupName);
            initialized = true;
        }
        // 测试用
        if (diffuseTextureId == -1) {
            diffuseTextureId = TextureLoader.loadTexture(texture);
        }
        normalsTextureID = TextureLoader.loadTexture(Lucisrenderlib.prefix("obj/test4_n.png"));
        specularTextureID = TextureLoader.loadTexture(Lucisrenderlib.prefix("obj/test4_s.png"));
        // 测试用
        ShaderInstance shader = RenderSystem.getShader();
        if (currentRenderStateShard.equals(renderType2)) {
            if (ModList.get().isLoaded(Iris.MODID)) {
                WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

                if (pipeline instanceof ShaderRenderingPipeline) {
                    if (ShadowRenderer.ACTIVE) {
                        shader = ((ShaderRenderingPipeline) pipeline).getShaderMap().getShader(SHADOW_ENTITIES_CUTOUT);
                    }else {
                        shader = ((ShaderRenderingPipeline) pipeline).getShaderMap().getShader(ENTITIES_EYES_TRANS);
                    }
                }
            }
//            shader = GameRenderer.positionColorTexLightmapShader;
        }

//        shader.clear();
        for (int i = 0; i < 12; ++i) {
            int j = RenderSystem.getShaderTexture(i);
            shader.setSampler("Sampler" + i, j);
        }
        if (shader.MODEL_VIEW_MATRIX != null) {
            shader.MODEL_VIEW_MATRIX.set(poseStack.last().pose());
        }
        if (shader.PROJECTION_MATRIX != null) {
            shader.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
        }
        if (shader.INVERSE_VIEW_ROTATION_MATRIX != null) {
            shader.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
        }
        if (shader.COLOR_MODULATOR != null) {
            shader.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        }
        if (shader.FOG_START != null) {
            shader.FOG_START.set(RenderSystem.getShaderFogStart());
        }
        if (shader.FOG_END != null) {
            shader.FOG_END.set(RenderSystem.getShaderFogEnd());
        }
        if (shader.FOG_COLOR != null) {
            shader.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        }
        if (shader.FOG_SHAPE != null) {
            shader.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        }
        if (shader.TEXTURE_MATRIX != null) {
            shader.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        }
        if (shader.GAME_TIME != null) {
            shader.GAME_TIME.set(RenderSystem.getShaderGameTime());
        }
        if (shader.SCREEN_SIZE != null) {
            Window window = Minecraft.getInstance().getWindow();
            shader.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
        }


        RenderSystem.setupShaderLights(shader);
        shader.apply();
        int currentProgram = shader.getId();
//        GL13.glActiveTexture(GL13.GL_TEXTURE0);
//        GL11.glBindTexture(GL11.GL_TEXTURE_2D, diffuseTextureId);
//        int normals = GL20.glGetUniformLocation(currentProgram, "normals");
//        if(normals != -1) {
//            GL13.glActiveTexture(GL13.GL_TEXTURE0 + GL20.glGetUniformi(currentProgram, normals));
//            GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalsTextureID);
//        }
//        int specular = GL20.glGetUniformLocation(currentProgram, "specular");
//        if(specular != -1) {
//            GL13.glActiveTexture(GL13.GL_TEXTURE0 + GL20.glGetUniformi(currentProgram, specular));
//            GL11.glBindTexture(GL11.GL_TEXTURE_2D, specularTextureID);
//        }
        int nm = GL20.glGetUniformLocation(currentProgram, "iris_NormalMat");
        if (nm >= 0) {
            System.out.println("normalMat: " + nm);
            Matrix3f normalMatrix = new Matrix3f(poseStack.last().normal());
            FloatBuffer buffer = BufferUtils.createFloatBuffer(9);
            normalMatrix.get(buffer);
            GL20.glUniformMatrix3fv(nm, false, buffer);
        }
        GL30.glBindVertexArray(VAO);
        // 颜色
        GL20.glDisableVertexAttribArray(IrisUtils.vaColor);  // 禁用属性数组
        GL20.glVertexAttrib4f(IrisUtils.vaColor, 1.0f, 1.0f, 1.0f, 1.0f);  // RGBA(1,1,1,1)

        GL30.glDisableVertexAttribArray(IrisUtils.vaUV1);
        GL30.glVertexAttribI2i(IrisUtils.vaUV1, overlay & '\uffff', overlay >> 16 & '\uffff');
        GL30.glDisableVertexAttribArray(IrisUtils.vaUV2);
        GL30.glVertexAttribI2i(IrisUtils.vaUV2, light & '\uffff', light >> 16 & '\uffff');

        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE); // 显示三角形线框
        //绘制
        GL30.glDrawElements(
                GL30.GL_TRIANGLES,
                indexCount,
                GL30.GL_UNSIGNED_INT,
                0
        );
        //GL30.glPolygonMode(GL30.GL_FRONT_AND_BACK, GL30.GL_FILL);

        GL30.glVertexAttribI2i(IrisUtils.vaUV1, 0, 0);
        GL30.glVertexAttribI2i(IrisUtils.vaUV2, 0, 0);

        GL30.glBindVertexArray(0);
        shader.clear();
//        shader.apply();
//        RenderSystem.disableBlend();
//        RenderSystem.defaultBlendFunc();
    }


    //为VAO绑定顶点坐标，UV0，法线，切线
    private void init(String groupName) {
        BufferBuilder bufferBuilder = new BufferBuilder(256);
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.NEW_ENTITY);
        //这里上传顶点
        Face.setCol(Color.WHITE);
        Face.setLightMap(LightTexture.pack(15, 15));
        WavefrontObject obj = new WavefrontObject(Lucisrenderlib.prefix("obj/test4.obj"));
        obj.tessellateOnly(bufferBuilder, groupName);

        BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();
        BufferBuilder.DrawState drawState = renderedBuffer.drawState();
        ByteBuffer vertexBuffer = renderedBuffer.vertexBuffer();
        ByteBuffer indexBuffer = renderedBuffer.indexBuffer();
        System.out.println(drawState.sequentialIndex());
        VAO = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(VAO);

        VBO = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, VBO);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertexBuffer, GL30.GL_STATIC_DRAW);
        drawState.format().setupBufferState();

// 计算所需容量（双倍扩展策略）
        indexCount = drawState.indexCount();
        int newCapacity = Math.max(indexCount * 2, 65536);

// 直接创建指定容量的整型缓冲区
        IntBuffer intBuffer = ByteBuffer.allocateDirect(newCapacity * 4)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer();

// 高效生成连续索引序列
//        for (int i = 0; i < newCapacity; i += 1024) {
//            int chunkSize = Math.min(1024, newCapacity - i);
//            for (int j = 0; j < chunkSize; j++) {
//                intBuffer.put(i + j, i + j); // 直接设置指定位置的值
//            }
//        }
        for (int i = 0; i < indexCount; i++) {
            intBuffer.put(i, i);
        }
        intBuffer.position(0);  // 重置缓冲区位置
        intBuffer.limit(indexCount);
        EBO = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, intBuffer, GL30.GL_STATIC_DRAW);
//        EBO = GL30.glGenBuffers();
//        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);
//        indexCount = drawState.indexCount();
//        // 双倍扩展策略
//        int newCapacity = Math.max(indexCount * 2, 65536);
//
//
//        ByteBuffer buffer = ByteBuffer.allocateDirect(newCapacity * 4) // 每索引4字节
//                .order(ByteOrder.nativeOrder());
//
//        // 创建连续索引序列（0,1,2,...N）
//        IntBuffer intBuffer = buffer.asIntBuffer();
//        for (int i = 0; i < newCapacity; i++) {
//            intBuffer.put(i);
//        }
//        intBuffer.flip();
//        //ensureStorage(indexCount);
//        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, intBuffer, GL30.GL_STATIC_DRAW);

        GL30.glBindVertexArray(0);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private void ensureStorage(int neededIndexCount) {
        VertexFormat.IndexType indexType = VertexFormat.IndexType.least(neededIndexCount);
        int byteSize = Mth.roundToward(neededIndexCount * indexType.bytes, 4);

        GlStateManager._glBufferData(
                GL30.GL_ELEMENT_ARRAY_BUFFER,
                (long) byteSize,
                GL30.GL_DYNAMIC_DRAW
        );

        ByteBuffer buffer = GlStateManager._glMapBuffer(
                GL30.GL_ELEMENT_ARRAY_BUFFER,
                GL30.GL_WRITE_ONLY
        );

        if (buffer == null) {
            throw new RuntimeException("Failed to map GL buffer");
        }

        try {
            // 根据类型直接写入缓冲区，避免虚方法调用
            switch (indexType) {
                case SHORT:
                    for (int i = 0; i < neededIndexCount; i++) {
                        buffer.putShort((short) i);  // 注意：需确保i<32768
                    }
                    break;

                case INT:
                    for (int i = 0; i < neededIndexCount; i++) {
                        buffer.putInt(i);
                    }
                    break;

                default:
                    throw new IllegalStateException("Unsupported index type: " + indexType);
            }
        } finally {
            // 确保始终执行取消映射
            GlStateManager._glUnmapBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER);
        }
    }

    public void cleanup() {
        if (VAO != 0) GL30.glDeleteVertexArrays(VAO);
        if (VBO != 0) GL30.glDeleteBuffers(VBO);
        if (EBO != 0) GL30.glDeleteBuffers(EBO);

        group.values().forEach(OBJModel::cleanup);
        group.clear();
    }
}
