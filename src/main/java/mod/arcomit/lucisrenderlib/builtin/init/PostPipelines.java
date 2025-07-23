package mod.arcomit.lucisrenderlib.builtin.init;

import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import mod.arcomit.lucisrenderlib.builtin.pipeline.BloomPipeline;
import mod.arcomit.lucisrenderlib.core.postprocessing.pipeline.PostPipeline;

public class PostPipelines {
    public static final PostPipeline bloom = new BloomPipeline(Lucisrenderlib.prefix("bloom"));
}
