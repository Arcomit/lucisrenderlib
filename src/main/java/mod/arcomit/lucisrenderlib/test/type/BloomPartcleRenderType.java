package mod.arcomit.lucisrenderlib.test.type;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import mod.arcomit.lucisrenderlib.postprocessing.particle.rendertype.OpaqueParticleRenderType;
import mod.arcomit.lucisrenderlib.postprocessing.pipeline.PostPipeline;
import mod.arcomit.lucisrenderlib.test.pipeline.InitPipelines;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;

import static com.mojang.blaze3d.vertex.VertexSorting.ORTHOGRAPHIC_Z;

public class BloomPartcleRenderType extends OpaqueParticleRenderType {

    public static final BloomPartcleRenderType INSTANCE = new BloomPartcleRenderType();

    @Override
    public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
        RenderSystem.disableCull();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        RenderSystem.setShader(this::getShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
        super.begin(bufferBuilder, textureManager);
    }

    @Override
    public void end(Tesselator tesselator) {
        tesselator.getBuilder().setQuadSorting(ORTHOGRAPHIC_Z);
        super.end(tesselator);
        RenderSystem.enableCull();
    }

    @Override
    public PostPipeline getPipeline() {
        return InitPipelines.bloom;
    }
}
