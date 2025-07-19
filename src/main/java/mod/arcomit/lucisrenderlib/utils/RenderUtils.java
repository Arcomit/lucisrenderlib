package mod.arcomit.lucisrenderlib.utils;

import com.mojang.blaze3d.vertex.PoseStack;

public class RenderUtils {
    public static PoseStack copyPoseStack(PoseStack poseStack) {
        PoseStack finalStack = new PoseStack();
        finalStack.setIdentity();
        finalStack.poseStack.addLast(poseStack.last());
        return finalStack;
    }

}
