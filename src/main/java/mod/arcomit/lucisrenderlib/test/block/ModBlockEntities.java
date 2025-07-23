package mod.arcomit.lucisrenderlib.test.block;

import mod.arcomit.lucisrenderlib.Lucisrenderlib;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Lucisrenderlib.MODID);

    public static final RegistryObject<BlockEntityType<MagicBlockEntity>> MAGIC_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("magic_block_entity",
                    () -> BlockEntityType.Builder.of(
                            MagicBlockEntity::new,
                            ModBlocks.MAGIC_BLOCK.get()
                    ).build(null));
}