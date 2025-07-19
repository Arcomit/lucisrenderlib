package mod.arcomit.lucisrenderlib.test.screenshot;

import com.mojang.blaze3d.platform.InputConstants;
import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Lucisrenderlib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TestKeys {
    //按键-开关索敌(Toggle enemy lock-on on/off)
    public static final KeyMapping TEST = new KeyMapping(
            "key."+ Lucisrenderlib.MODID +".test",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_5,
            "category."+ Lucisrenderlib.MODID
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TEST);
    }
}
