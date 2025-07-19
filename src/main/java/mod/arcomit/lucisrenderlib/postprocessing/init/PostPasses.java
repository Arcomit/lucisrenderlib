package mod.arcomit.lucisrenderlib.postprocessing.init;

import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import mod.arcomit.lucisrenderlib.postprocessing.pass.*;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = Lucisrenderlib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PostPasses {

    public static PostPassBase blit;
    public static DepthCull depth_cull;
    public static DownSampling downSampling;
    public static UpSampling upSampling;
    public static UnityComposite unityComposite;

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        try {
            ResourceManager rm = Minecraft.getInstance().getResourceManager();
            blit = new PostPassBase(Lucisrenderlib.prefix("blit"),rm);
            depth_cull = new DepthCull(Lucisrenderlib.prefix("depth_cull"), rm);
            downSampling = new DownSampling(Lucisrenderlib.prefix("down_sampling"), rm);
            upSampling = new UpSampling(Lucisrenderlib.prefix("up_sampling"), rm);
            unityComposite = new UnityComposite(Lucisrenderlib.prefix("unity_composite"),rm);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
