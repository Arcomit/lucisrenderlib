package mod.arcomit.lucisrenderlib.test.block;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import mod.arcomit.lucisrenderlib.utils.IrisUtils;
import mod.arcomit.lucisrenderlib.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static mod.arcomit.lucisrenderlib.test.block.RenderTest.initialized2;

public class TriangleBlockRenderer implements BlockEntityRenderer<BlockEntity> {

    public TriangleBlockRenderer(BlockEntityRendererProvider.Context context) {}

    public static RenderType renderType = NBRenderType.getNB();
    public static PoseStack pose;

    @Override
    public void render(BlockEntity entity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        renderType = NBRenderType.getNB();
        VertexConsumer vertexBuilder = bufferSource.getBuffer(renderType);
        initialized2 = false;
        pose = RenderUtils.copyPoseStack(poseStack);
    }
}