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
import mod.arcomit.lucisrenderlib.utils.MathUtils;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
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

public class OBJModel3 {
    protected final Obj obj;
    protected final Map<String, OBJModel3> group = new ConcurrentHashMap<>();
    protected int VAO;
    protected int positionBufferObject;
    protected int normalsBufferObject;
    protected int normalsBufferObject2;
    protected int uvBufferObject;
    protected int tangentBufferObject;
    protected int EBO;
    protected int indexCount;// 索引计数
    protected boolean initialized = false;
    private int diffuseTextureId = -1;
    private int normalsTextureID = -1;
    private int specularTextureID = -1;
    private int emissiveTextureID = -1;

    public OBJModel3(Obj obj) {
        this.obj = obj;
    }

    public void render(String groupName, ResourceLocation texture, PoseStack poseStack, int light, int overlay) {
        if (group.containsKey(groupName)) {
            OBJModel3 model = group.get(groupName);
            model.render(texture, poseStack, light, overlay);
        }else {
            ObjGroup group = obj.getGroup(groupName);
            if (group != null) {
                Obj groupObj = ObjUtils.groupToObj(obj, group, null);
                groupObj = ObjUtils.convertToRenderable(groupObj);
                OBJModel3 model = new OBJModel3(groupObj);
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
            System.out.println(shader.MODEL_VIEW_MATRIX.getName());
            //shader.MODEL_VIEW_MATRIX.set(poseStack.last().pose());
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

        int mm = GL20.glGetUniformLocation(currentProgram, "iris_ModelViewMat");
        if (mm >= 0) {
            Matrix4f poseMatrix = new Matrix4f(poseStack.last().pose());
            FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
            poseMatrix.get(buffer);
            GL20.glUniformMatrix4fv(mm, false, buffer);
        }
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

//
//        float[] normal0 = new float[3];
//        float[] normal1 = new float[3];
//        float[] cross = new float[3];
//        float[] tangent = new float[3];
//
//        for(int i = 0; i < obj.getNumVertices(); i++) {
//            position.position(i * 3);
//
//            normal1[0] = -normal0[2];
//            normal1[1] = normal0[0];
//            normal1[2] = normal0[1];
//
//            MathUtils.cross(normal0, normal1, cross);
//            MathUtils.normalize(cross, tangent);
//
//            tangents.put(tangent[0]);
//            tangents.put(tangent[1]);
//            tangents.put(tangent[2]);
//            tangents.put(1.0F);
//        }
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
                int idx = indices.get(face * 3 + vert);
                position.position(idx * 3);
                out[0] = position.get();
                out[1] = position.get();
                out[2] = position.get();
            }

            @Override
            public void getNormal(float[] out, int face, int vert) {
                int idx = indices.get(face * 3 + vert);
                normals.position(idx * 3);
                out[0] = normals.get();
                out[1] = normals.get();
                out[2] = normals.get();
            }

            @Override
            public void getTexCoord(float[] out, int face, int vert) {
                int idx = indices.get(face * 3 + vert);
                uv.position(idx * 2);
                out[0] = uv.get();
                out[1] = uv.get();
            }

            @Override
            public void setTSpaceBasic(float[] tangent, float sign, int face, int vert) {
                int idx = indices.get(face * 3 + vert);
                tangents.position(idx * 4);
                tangents.put(tangent[0]);
                tangents.put(tangent[1]);
                tangents.put(tangent[2]);
                tangents.put(sign);
                //tangents.put(-sign);
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
        GL20.glVertexAttribPointer(IrisUtils.vaTangent, 4, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(IrisUtils.vaTangent);


        EBO = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indices, GL30.GL_STATIC_DRAW);


        GL30.glBindVertexArray(0);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void cleanup() {
        if (VAO != 0) GL30.glDeleteVertexArrays(VAO);
        if (positionBufferObject != 0) GL30.glDeleteBuffers(positionBufferObject);
        if (uvBufferObject != 0) GL30.glDeleteBuffers(uvBufferObject);
        if (normalsBufferObject != 0) GL30.glDeleteBuffers(normalsBufferObject);
        if (tangentBufferObject != 0) GL30.glDeleteBuffers(tangentBufferObject);
        if (EBO != 0) GL30.glDeleteBuffers(EBO);

        group.values().forEach(OBJModel3::cleanup);
        group.clear();
    }
}
