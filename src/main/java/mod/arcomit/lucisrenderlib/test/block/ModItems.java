package mod.arcomit.lucisrenderlib.test.block;

import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Lucisrenderlib.MODID);
    public static final RegistryObject<Item> MAGIC_BLOCK_ITEM = ITEMS.register("magic_block",
            () -> new CustomBlockItem(ModBlocks.MAGIC_BLOCK.get(),
                    new Item.Properties()));
}
