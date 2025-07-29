package mod.arcomit.lucisrenderlib.test.block;

import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Lucisrenderlib.MODID);

    public static final RegistryObject<Block> MAGIC_BLOCK = BLOCKS.register("magic_block",
            () -> new MagicBlock(Block.Properties.of()
                    .strength(3.0f)
                    .noOcclusion() // 重要：允许透明渲染
            ));
}