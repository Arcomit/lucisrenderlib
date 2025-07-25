package mod.arcomit.lucisrenderlib.core.obj;

import com.jme3.util.mikktspace.MikkTSpaceContext;
import com.jme3.util.mikktspace.MikktspaceTangentGenerator;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjGroup;
import de.javagl.obj.ObjUtils;
import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import mod.arcomit.lucisrenderlib.utils.IrisUtils;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static mod.arcomit.lucisrenderlib.test.block.RenderTest2.currentRenderStateShard;
import static mod.arcomit.lucisrenderlib.test.block.TriangleBlockRenderer.renderType2;
import static net.irisshaders.iris.pipeline.programs.ShaderKey.ENTITIES_EYES_TRANS;
import static net.irisshaders.iris.pipeline.programs.ShaderKey.SHADOW_ENTITIES_CUTOUT;

public class OBJModel {
    protected final Obj obj;
    protected final Map<String, OBJModel> group = new ConcurrentHashMap<>();
    protected int VAO;
    protected int positionBufferObject;
    protected int normalsBufferObject;
    protected int uvBufferObject;
    protected int tangentBufferObject;
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
            model.render(texture, poseStack, light, overlay);
        }else {
            ObjGroup group = obj.getGroup(groupName);
            if (group != null) {
                Obj groupObj = ObjUtils.groupToObj(obj, group, null);
                groupObj = ObjUtils.convertToRenderable(groupObj);
                OBJModel model = new OBJModel(groupObj);
                model.render(texture, poseStack, light, overlay);
                this.group.put(groupName, model);
            }else {
                //Lucisrenderlib.LOGGER.error("The obj model does not have groups: " + groupName);
            }
        }

    }

    public void render(ResourceLocation texture, PoseStack poseStack, int light, int overlay) {
        // 初始化
        if (!initialized) {
            init();
            initialized = true;
        }
        // 测试用
        if (diffuseTextureId == -1) {
            diffuseTextureId = TextureLoader.loadTexture(texture);
        }
//        GL13.glActiveTexture(GL13.GL_TEXTURE0);
//        GL11.glBindTexture(GL11.GL_TEXTURE_2D, diffuseTextureId);
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
        if (shader.getUniform("iris_NormalMat") != null){
            System.out.println("iris_NormalMat is 真的！！！");
            shader.getUniform("iris_NormalMat").set(poseStack.last().normal());
        }
        if (shader.getUniform("iris_NormalMatrix") != null){
            System.out.println("iris_NormalMatrix is 真的！！！");
            shader.getUniform("iris_NormalMatrix").set(poseStack.last().normal());
        }

        int currentProgram = shader.getId();
        int nm = GL20.glGetUniformLocation(currentProgram, "iris_NormalMat");
// 检查 uniform 位置是否有效
        if (nm >= 0) {
            System.out.println("nm is 真的！！！");
            // 创建 FloatBuffer 存储矩阵数据（3x3=9 个元素）
            FloatBuffer buffer = BufferUtils.createFloatBuffer(9);

            // 将矩阵按列主序存入 buffer
            poseStack.last().normal().get(buffer);
            buffer.flip(); // 切换为读取模式（position=0, limit=9）

            // 传递数据到着色器 (false 表示不需要转置)
            GL20.glUniformMatrix3fv(nm, false, buffer);
        }

        RenderSystem.setupShaderLights(shader);
        shader.apply();
        GL30.glBindVertexArray(VAO);
        // 颜色
        GL20.glDisableVertexAttribArray(IrisUtils.vaColor);  // 禁用属性数组
        GL20.glVertexAttrib4f(IrisUtils.vaColor, 1.0f, 1.0f, 1.0f, 1.0f);  // RGBA(1,1,1,1)

        GL30.glVertexAttribI2i(IrisUtils.vaUV1, overlay & '\uffff', overlay >> 16 & '\uffff');
        GL30.glVertexAttribI2i(IrisUtils.vaUV2, light & '\uffff', light >> 16 & '\uffff');
        //绘制
        GL30.glDrawElements(
                GL30.GL_TRIANGLES,
                indexCount,
                GL30.GL_UNSIGNED_INT,
                0
        );
        GL30.glVertexAttribI2i(IrisUtils.vaUV1, 0, 0);
        GL30.glVertexAttribI2i(IrisUtils.vaUV2, 0, 0);

        GL30.glBindVertexArray(0);
        shader.clear();
//        shader.apply();
//        RenderSystem.disableBlend();
//        RenderSystem.defaultBlendFunc();
    }

    //为VAO绑定顶点坐标，UV0，法线，切线
    private void init(){
        IntBuffer indices = ObjData.getFaceVertexIndices(obj, 3);
        FloatBuffer position = ObjData.getVertices(obj);// 顶点坐标
        FloatBuffer uv = ObjData.getTexCoords(obj, 2, true);// UV
        FloatBuffer normals = ObjData.getNormals(obj);// 法线

        FloatBuffer originalPos = position.duplicate();
        FloatBuffer originalUv = uv.duplicate();
        FloatBuffer originalNormals = normals.duplicate();

        FloatBuffer tangents = BufferUtils.createFloatBuffer(obj.getNumVertices() * 4);
        MikktspaceTangentGenerator.genTangSpaceDefault(new MikkTSpaceContext() {

            @Override
            public int getNumFaces() {
                return obj.getNumFaces();
            }

            @Override
            public int getNumVerticesOfFace(int face) {
                return 3;
            }

            @Override
            public void getPosition(float[] out, int face, int vert) {
                int idx = indices.get(face * 3 + vert) * 3;
                originalPos.position(idx);
                out[0] = originalPos.get();
                out[1] = originalPos.get();
                out[2] = originalPos.get();
            }

            @Override
            public void getNormal(float[] out, int face, int vert) {
                int idx = indices.get(face * 3 + vert) * 3;
                originalNormals.position(idx);
                out[0] = originalNormals.get();
                out[1] = originalNormals.get();
                out[2] = originalNormals.get();
            }

            @Override
            public void getTexCoord(float[] out, int face, int vert) {
                int idx = indices.get(face * 3 + vert) * 2;
                originalUv.position(idx);
                out[0] = originalUv.get();
                out[1] = originalUv.get();
            }

            @Override
            public void setTSpaceBasic(float[] tangent, float sign, int face, int vert) {
                int idx = indices.get(face * 3 + vert) * 4;
                tangents.position(idx);
                tangents.put(tangent[0]);
                tangents.put(tangent[1]);
                tangents.put(tangent[2]);
                tangents.put(sign);
            }

            @Override
            public void setTSpace(float[] tangent, float[] biTangent, float magS, float magT, boolean isOrientationPreserving, int face, int vert) {
                // 不需要实现
            }
        });
        position.rewind();
        uv.rewind();
        normals.rewind();
        indices.rewind();
        tangents.rewind();
        System.out.println("First tangent: " +
                tangents.get() + ", " +
                tangents.get() + ", " +
                tangents.get() + ", " +
                tangents.get());
        tangents.rewind();

        indexCount = indices.capacity();

        VAO = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(VAO);

        positionBufferObject = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, positionBufferObject);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, position, GL30.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(IrisUtils.vaPosition, 3, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(IrisUtils.vaPosition);

        uvBufferObject = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, uvBufferObject);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, uv, GL30.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(IrisUtils.vaUV0, 2, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(IrisUtils.vaUV0);

        normalsBufferObject = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, normalsBufferObject);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, normals, GL30.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(IrisUtils.vaNormal, 3, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(IrisUtils.vaNormal);

        tangentBufferObject = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, tangentBufferObject);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, tangents, GL30.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(IrisUtils.vaTangent, 4, GL11.GL_FLOAT, false, 0, 0); // 使用location=3
        GL20.glEnableVertexAttribArray(IrisUtils.vaTangent);


        EBO = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indices, GL30.GL_STATIC_DRAW);


        GL30.glBindVertexArray(0);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

}
