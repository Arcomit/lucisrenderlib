package mod.arcomit.lucisrenderlib.test;

import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import mod.arcomit.lucisrenderlib.test.DMC.JustJudgementCutMarksParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Lucisrenderlib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleRegistry {
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Lucisrenderlib.MODID);

    // 次元斩·绝-Just judgement cut
    // 刀痕粒子
    public static final RegistryObject<SimpleParticleType> DMC_JUST_JUDGEMENT_CUT_MARKS = PARTICLE_TYPES.register("dmc_just_judgement_cut_marks", () -> new SimpleParticleType(true));

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerParticleFactories(RegisterParticleProvidersEvent event){
        ParticleEngine particleEngine = Minecraft.getInstance().particleEngine;
        particleEngine.register(DMC_JUST_JUDGEMENT_CUT_MARKS.get(), JustJudgementCutMarksParticles.Provider::new);
    }

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
