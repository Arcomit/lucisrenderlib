package mod.arcomit.lucisrenderlib.postprocessing.event;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.arcomit.lucisrenderlib.postprocessing.PostMultiBufferSource;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraftforge.eventbus.api.Event;
import org.joml.Matrix4f;

public class InPipelineRenderEvent extends Event {
    private final LevelRenderer levelRenderer;
    private final PoseStack poseStack;
    private final Matrix4f projectionMatrix;
    private final int renderTick;
    private final float partialTick;
    private final Camera camera;
    private final Frustum frustum;
    private final PostMultiBufferSource bufferSource = PostMultiBufferSource.BUFFER_SOURCE;

    public InPipelineRenderEvent(LevelRenderer levelRenderer, PoseStack poseStack, Matrix4f projectionMatrix, int renderTick, float partialTick, Camera camera, Frustum frustum) {
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

    public static class Opaque extends InPipelineRenderEvent {
        public Opaque(LevelRenderer levelRenderer, PoseStack poseStack, Matrix4f projectionMatrix, int renderTick, float partialTick, Camera camera, Frustum frustum) {
            super(levelRenderer, poseStack, projectionMatrix, renderTick, partialTick, camera, frustum);
        }
    }

    public static class Translucent extends InPipelineRenderEvent {
        public Translucent(LevelRenderer levelRenderer, PoseStack poseStack, Matrix4f projectionMatrix, int renderTick, float partialTick, Camera camera, Frustum frustum) {
            super(levelRenderer, poseStack, projectionMatrix, renderTick, partialTick, camera, frustum);
        }
    }
}
