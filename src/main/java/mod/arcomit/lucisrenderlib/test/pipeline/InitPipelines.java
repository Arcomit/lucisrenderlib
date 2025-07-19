package mod.arcomit.lucisrenderlib.test.pipeline;

import com.mojang.blaze3d.pipeline.RenderTarget;
import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import mod.arcomit.lucisrenderlib.postprocessing.pipeline.PostPipeline;

public class InitPipelines {
    public static final PostPipeline bloom = new BloomPipeline(Lucisrenderlib.prefix("bloom"));
}
