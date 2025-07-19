package mod.arcomit.lucisrenderlib.test;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ExampleCommand {

    public static void Debug2(){
        try{
            Player player = Minecraft.getInstance().player;
            Vec3 pos = player.position();
            player.level().addParticle(ParticleRegistry.DMC_JUST_JUDGEMENT_CUT_MARKS.get() ,pos.x,pos.y,pos.z,0,0,0);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 注册指令
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("example") // 指令名称: /example
                        .requires(source -> source.hasPermission(2)) // 权限等级 (0-4)
                        .executes(context -> execute(context.getSource())) // 无参数执行
        );
    }

    // 指令执行逻辑
    private static int execute(CommandSourceStack source) {
        Debug2();
        source.sendSuccess(() ->
                        Component.literal("你好，世界！"),
                true // 是否广播到控制台
        );
        return Command.SINGLE_SUCCESS; // 返回值 1 表示成功
    }
}