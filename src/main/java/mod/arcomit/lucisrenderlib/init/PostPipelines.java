package mod.arcomit.lucisrenderlib.init;

import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import mod.arcomit.lucisrenderlib.postprocessing.pipeline.BloomPipeline;
import mod.arcomit.lucisrenderlib.postprocessing.pipeline.PostPipeline;

public class PostPipelines {
    public static final PostPipeline bloom = new BloomPipeline(Lucisrenderlib.prefix("bloom"));
}
