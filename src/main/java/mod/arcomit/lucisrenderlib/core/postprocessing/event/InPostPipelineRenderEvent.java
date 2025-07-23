package mod.arcomit.lucisrenderlib.core.postprocessing.event;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.arcomit.lucisrenderlib.core.postprocessing.PostMultiBufferSource;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraftforge.eventbus.api.Event;
import org.joml.Matrix4f;

public class InPostPipelineRenderEvent extends Event {
    private final LevelRenderer levelRenderer;
    private final PoseStack poseStack;
    private final Matrix4f projectionMatrix;
    private final int renderTick;
    private final float partialTick;
    private final Camera camera;
    private final Frustum frustum;
    private final PostMultiBufferSource bufferSource = PostMultiBufferSource.BUFFER_SOURCE;

    public InPostPipelineRenderEvent(LevelRenderer levelRenderer, PoseStack poseStack, Matrix4f projectionMatrix, int renderTick, float partialTick, Camera camera, Frustum frustum) {
        this.levelRenderer = levelRenderer;
        this.poseStack = poseStack;
        this.projectionMatrix = projectionMatrix;
        this.renderTick = renderTick;
        this.partialTick = partialTick;
        this.camera = camera;
        this.frustum = frustum;
    }

    public LevelRenderer getLevelRenderer() {
        return this.levelRenderer;
    }

    public PoseStack getPoseStack() {
        return this.poseStack;
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public int getRenderTick() {
        return this.renderTick;
    }

    public float getPartialTick() {
        return this.partialTick;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public Frustum getFrustum() {
        return this.frustum;
    }

    public PostMultiBufferSource getBufferSource() {
        return this.bufferSource;
    }

    public static class Opaque extends InPostPipelineRenderEvent {
        public Opaque(LevelRenderer levelRenderer, PoseStack poseStack, Matrix4f projectionMatrix, int renderTick, float partialTick, Camera camera, Frustum frustum) {
            super(levelRenderer, poseStack, projectionMatrix, renderTick, partialTick, camera, frustum);
        }
    }

    public static class Translucent extends InPostPipelineRenderEvent {
        public Translucent(LevelRenderer levelRenderer, PoseStack poseStack, Matrix4f projectionMatrix, int renderTick, float partialTick, Camera camera, Frustum frustum) {
            super(levelRenderer, poseStack, projectionMatrix, renderTick, partialTick, camera, frustum);
        }
    }
}
