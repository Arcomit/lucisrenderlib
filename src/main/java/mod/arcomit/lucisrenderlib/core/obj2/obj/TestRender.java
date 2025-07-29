package mod.arcomit.lucisrenderlib.core.obj2.obj;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;

import java.nio.ByteBuffer;

public class TestRender {
    WavefrontObject obj;
    protected int VAO;
    protected int VBO;
    protected int EBO;

    public TestRender(ResourceLocation resourceLocation) {
        this.obj = new WavefrontObject(resourceLocation);
    }

    public void render() {
        BufferBuilder bufferBuilder = new BufferBuilder(256);
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.NEW_ENTITY);

        obj.tessellateOnly(bufferBuilder, "blade");

        BufferBuilder.RenderedBuffer renderedBuffer = bufferBuilder.end();
        BufferBuilder.DrawState drawState = renderedBuffer.drawState();
        ByteBuffer vertexBuffer = renderedBuffer.vertexBuffer();

    }
}
