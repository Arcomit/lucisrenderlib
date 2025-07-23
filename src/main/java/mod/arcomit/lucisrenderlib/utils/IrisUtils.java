package mod.arcomit.lucisrenderlib.utils;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraftforge.fml.ModList;

public class IrisUtils {
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
