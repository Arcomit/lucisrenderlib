package mod.arcomit.lucisrenderlib.test.screenshot;

import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static mod.arcomit.lucisrenderlib.test.screenshot.RenderTargetExporter.captureRequested;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Lucisrenderlib.MODID)
public class TestEvent {

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL && captureRequested) {
            captureRequested = false;
            Minecraft mc = Minecraft.getInstance();
            DepthMapExporter.exportCurrentRenderTarget();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if(event.phase != TickEvent.Phase.END) return;
        Player player = Minecraft.getInstance().player;
        if(player == null) return;

        while(TestKeys.TEST.consumeClick()) {
            captureRequested = true;
        }
    }

}
