package mod.arcomit.lucisrenderlib.postprocessing.target;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;

public class ScaledTarget extends RenderTarget {
    float scaleW;
    float scaleH;

    public ScaledTarget(float scaleW, float scaleH, int width, int height, boolean useDepth, boolean pClearError) {
        super(useDepth);
        this.scaleW = scaleW;
        this.scaleH = scaleH;
        RenderSystem.assertOnRenderThreadOrInit();
        this.resize(width, height, pClearError);
    }

    @Override
    public void resize(int pWidth, int pHeight, boolean pClearError) {
        super.resize((int)(pWidth * scaleW), (int)(pHeight * scaleH), pClearError);
    }
}