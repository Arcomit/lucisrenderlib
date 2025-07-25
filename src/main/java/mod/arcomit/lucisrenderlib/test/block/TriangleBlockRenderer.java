package mod.arcomit.lucisrenderlib.test.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import mod.arcomit.lucisrenderlib.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import static mod.arcomit.lucisrenderlib.test.block.RenderTest2.initialized;
import static mod.arcomit.lucisrenderlib.test.block.RenderTest2.initialized2;

public class TriangleBlockRenderer implements BlockEntityRenderer<BlockEntity> {

    public TriangleBlockRenderer(BlockEntityRendererProvider.Context context) {}

    public static RenderType renderType;
    public static RenderType renderType2;
    public static PoseStack pose;
    public static int light;
    public static int overlay;
    static ResourceLocation texture = Lucisrenderlib.prefix("obj/test5.png");
    @Override
    public void render(BlockEntity entity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        initialized = false;
        initialized2 = false;
        pose = RenderUtils.copyPoseStack(poseStack);
        pose.translate(0.5, 0.5, 0.5);
        float time = (Minecraft.getInstance().level.getGameTime() + partialTicks) / 20.0f;
//        pose.mulPose(com.mojang.math.Axis.YP.rotation(time));
//        pose.mulPose(com.mojang.math.Axis.XP.rotation(time * 0.5f));
        pose.scale(0.01F, 0.01F, 0.01F);
        light = packedLight;
        overlay = packedOverlay;

        renderType2 = NBRenderType.getSlashBladeBlendLuminous(texture);
        VertexConsumer vertexBuilder2 = bufferSource.getBuffer(renderType2);

        renderType = NBRenderType.getSlashBladeBlend(texture);
        VertexConsumer vertexBuilder = bufferSource.getBuffer(renderType);
    }
}