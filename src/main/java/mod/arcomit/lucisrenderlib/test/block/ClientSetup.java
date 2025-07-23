package mod.arcomit.lucisrenderlib.test.block;

import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Lucisrenderlib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(ModBlockEntities.MAGIC_BLOCK_ENTITY.get(), TriangleBlockRenderer::new);
    }


    //@SubscribeEvent()
    public static void baked(final ModelEvent.ModifyBakingResult event) {
        ModelResourceLocation loc = new ModelResourceLocation(ForgeRegistries.ITEMS.getKey(ModItems.MAGIC_BLOCK_ITEM.get()), "inventory");
        BakedModel model = new CustomItemModel(event.getModels().get(loc));
        event.getModels().put(loc, model);
    }
}