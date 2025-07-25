package mod.arcomit.lucisrenderlib.utils;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraftforge.fml.ModList;

public class IrisUtils {
    public static final int vaPosition = 0;
    public static final int vaColor = 1;
    public static final int vaUV0 = 2;
    public static final int vaUV1 = 3;
    public static final int vaUV2 = 4;
    public static final int vaNormal = 5;
    public static final int vaTangent = 9;


    // 判断是否加载了Iris且启用了光影包
    public static boolean irisIsLoadedAndShaderPackon() {
        if (ModList.get().isLoaded(Iris.MODID)) {
            return IrisApi.getInstance().isShaderPackInUse();
        }
        return false;
    }

    public static WorldRenderingPhase getPhase() {
        if (irisIsLoadedAndShaderPackon()) {
            if (Iris.getPipelineManager().getPipelineNullable() != null){
                return Iris.getPipelineManager().getPipelineNullable().getPhase();
            }
            return null;
        }
        return null;
    }

    public static boolean isInPhase(WorldRenderingPhase phase) {

        return getPhase() == phase;
    }

}
