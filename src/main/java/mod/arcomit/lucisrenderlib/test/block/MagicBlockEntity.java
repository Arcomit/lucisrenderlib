package mod.arcomit.lucisrenderlib.test.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// MagicBlockEntity.java
public class MagicBlockEntity extends BlockEntity {
    public MagicBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAGIC_BLOCK_ENTITY.get(), pos, state);
    }
}
