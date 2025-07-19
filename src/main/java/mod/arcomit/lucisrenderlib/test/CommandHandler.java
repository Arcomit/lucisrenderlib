package mod.arcomit.lucisrenderlib.test;

import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Lucisrenderlib.MODID)
public class CommandHandler {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        // 注册你的指令
        ExampleCommand.register(event.getDispatcher());
    }
}