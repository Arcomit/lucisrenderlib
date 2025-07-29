package mod.arcomit.lucisrenderlib.test.block;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import mod.arcomit.lucisrenderlib.builtin.init.PostPipelines;
import mod.arcomit.lucisrenderlib.core.obj.OBJManager;
import mod.arcomit.lucisrenderlib.core.obj.OBJModel;
import mod.arcomit.lucisrenderlib.utils.IrisUtils;
import net.irisshaders.batchedentityrendering.impl.WrappableRenderType;
import net.irisshaders.iris.Iris;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import static mod.arcomit.lucisrenderlib.test.block.TriangleBlockRenderer.*;

public class RenderTest2 {
    static ResourceLocation texture = Lucisrenderlib.prefix("obj/test4.png");
    public static RenderStateShard currentRenderStateShard;
    public static void AfterSetupRenderState(RenderStateShard renderStateShard) {
        if (ModList.get().isLoaded(Iris.MODID)) {
            currentRenderStateShard = ((renderStateShard instanceof WrappableRenderType) ? ((WrappableRenderType)renderStateShard).unwrap() : renderStateShard);
        }else {
            currentRenderStateShard = renderStateShard;
        }
    }
    public static boolean initialized = false;
    public static boolean initialized2 = false;

    public static void RenderTest() {
        if (!initialized){
            if (currentRenderStateShard == null) return;
            if (currentRenderStateShard.equals(renderType)){
                initialized = true;

                int currentVAO = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
                int currentArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
                int currentElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);

                OBJModel model = OBJManager.getOBJModel(Lucisrenderlib.prefix("obj/test4.obj"));
                model.render("blade",texture,pose,light,overlay);

                GL30.glBindVertexArray(currentVAO);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentArrayBuffer);
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer);
            }
        }

        if (!initialized2){
            if (currentRenderStateShard == null) return;
            if (currentRenderStateShard.equals(renderType2)){
                initialized2 = true;

                int currentVAO = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
                int currentArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
                int currentElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);

                OBJModel model = OBJManager.getOBJModel(Lucisrenderlib.prefix("obj/test4.obj"));
                pose.pushPose();
                pose.scale(1.000001f,1.000001f,1.000001f);
                //PostPipelines.bloom.bindOpaqueWrite();
                //model.render("blade_luminous",texture,pose, LightTexture.pack(15,15),overlay);
                //PostPipelines.bloom.unbindOpaqueWrite();
                pose.popPose();

                GL30.glBindVertexArray(currentVAO);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentArrayBuffer);
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer);
            }
        }
    }
}
