package mod.arcomit.lucisrenderlib.core.postprocessing.event;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import mod.arcomit.lucisrenderlib.core.postprocessing.particle.rendertype.OpaqueParticleRenderType;
import mod.arcomit.lucisrenderlib.core.postprocessing.particle.rendertype.TranslucentParticleRenderType;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.Queue;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Lucisrenderlib.MODID)
public class InPostPipelineRenderHandler {

    private static final Map<OpaqueParticleRenderType, Queue<Particle>> OPAQUE_PARTICLES = Maps.newTreeMap(ForgeHooksClient.makeParticleRenderTypeComparator(ParticleEngine.RENDER_ORDER));
    private static final Map<TranslucentParticleRenderType, Queue<Particle>> TRANSLUCENT_PARTICLES = Maps.newTreeMap(ForgeHooksClient.makeParticleRenderTypeComparator(ParticleEngine.RENDER_ORDER));

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        LevelRenderer renderer = event.getLevelRenderer();
        PoseStack posestack_ = event.getPoseStack();
        Matrix4f projectionMatrix = event.getProjectionMatrix();
        int renderTick = event.getRenderTick();
        float partialTick = event.getPartialTick();
        Camera camera = event.getCamera();
        Frustum clippingHelper = event.getFrustum();

        Minecraft minecraft = Minecraft.getInstance();
        TextureManager textureManager = minecraft.getTextureManager();
        LightTexture lightTexture = minecraft.gameRenderer.lightTexture();
        ParticleEngine particleEngine = minecraft.particleEngine;

        // 添加我们的粒子渲染批次
        lightTexture.turnOnLightLayer();
        RenderSystem.enableDepthTest();
        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE2);
        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.mulPoseMatrix(posestack_.last().pose());
        RenderSystem.applyModelViewMatrix();

        OPAQUE_PARTICLES.clear();
        TRANSLUCENT_PARTICLES.clear();
        for (ParticleRenderType particleRenderType : particleEngine.particles.keySet()) {
            if (particleRenderType instanceof OpaqueParticleRenderType opaqueParticleRenderType) {
                OPAQUE_PARTICLES.put(opaqueParticleRenderType, particleEngine.particles.get(opaqueParticleRenderType));
            }
            if (particleRenderType instanceof TranslucentParticleRenderType translucentParticleRenderType) {
                TRANSLUCENT_PARTICLES.put(translucentParticleRenderType, particleEngine.particles.get(translucentParticleRenderType));
            }
        }

        //不透明
        InPostPipelineRenderEvent.Opaque opaqueEvent = new InPostPipelineRenderEvent.Opaque(renderer, posestack_, projectionMatrix, renderTick, partialTick, camera, clippingHelper);
        MinecraftForge.EVENT_BUS.post(opaqueEvent);

        for (OpaqueParticleRenderType opaueParticleRenderType : OPAQUE_PARTICLES.keySet()) {
            Iterable<Particle> iterable = OPAQUE_PARTICLES.get(opaueParticleRenderType);
            if (iterable != null) {
                RenderSystem.setShader(GameRenderer::getParticleShader);
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tesselator.getBuilder();
                opaueParticleRenderType.begin(bufferbuilder, textureManager);

                for(Particle particle : iterable) {
                    if (clippingHelper != null && particle.shouldCull() && !clippingHelper.isVisible(particle.getBoundingBox())) continue;
                    try {
                        particle.render(bufferbuilder, camera, partialTick);
                    } catch (Throwable throwable) {
                        CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Particle");
                        CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being rendered");
                        crashreportcategory.setDetail("Particle", particle::toString);
                        crashreportcategory.setDetail("Particle Type", opaueParticleRenderType::toString);
                        throw new ReportedException(crashreport);
                    }
                }

                opaueParticleRenderType.end(tesselator);
            }
        }


        //半透明
        InPostPipelineRenderEvent.Translucent translucentEvent = new InPostPipelineRenderEvent.Translucent(renderer, posestack_, projectionMatrix, renderTick, partialTick, camera, clippingHelper);
        MinecraftForge.EVENT_BUS.post(translucentEvent);

        for (TranslucentParticleRenderType translucentParticleRenderType : TRANSLUCENT_PARTICLES.keySet()) {
            Iterable<Particle> iterable = TRANSLUCENT_PARTICLES.get(translucentParticleRenderType);
            if (iterable != null) {
                RenderSystem.setShader(GameRenderer::getParticleShader);
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tesselator.getBuilder();
                translucentParticleRenderType.begin(bufferbuilder, textureManager);

                for(Particle particle : iterable) {
                    if (clippingHelper != null && particle.shouldCull() && !clippingHelper.isVisible(particle.getBoundingBox())) continue;
                    try {
                        particle.render(bufferbuilder, camera, partialTick);
                    } catch (Throwable throwable) {
                        CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Particle");
                        CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being rendered");
                        crashreportcategory.setDetail("Particle", particle::toString);
                        crashreportcategory.setDetail("Particle Type", translucentParticleRenderType::toString);
                        throw new ReportedException(crashreport);
                    }
                }

                translucentParticleRenderType.end(tesselator);
            }
        }

        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        lightTexture.turnOffLightLayer();
    }
}
