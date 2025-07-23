package mod.arcomit.lucisrenderlib.test.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import java.util.ArrayList;
import java.util.List;

public class VisibleBlockEntityFinder {

    public static List<BlockEntity> getVisibleBlockEntities() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return List.of();

        // 获取玩家周围的方块实体（半径 64 格）
        List<BlockEntity> nearbyEntities = getBlockEntitiesInRadius(mc.player.blockPosition(), 64);

        // 获取当前渲染的视锥体
        Frustum frustum = mc.levelRenderer.getFrustum();
        List<BlockEntity> visibleEntities = new ArrayList<>();

        for (BlockEntity entity : nearbyEntities) {
            // 获取方块实体的包围盒
            AABB aabb = entity.getRenderBoundingBox();
            if (aabb == null) {
                aabb = new AABB(entity.getBlockPos());
            }

            // 视锥体裁剪检测
            if (frustum.isVisible(aabb)) {
                visibleEntities.add(entity);
            }
        }
        return visibleEntities;
    }

    // 获取指定半径内的方块实体
    public static List<BlockEntity> getBlockEntitiesInRadius(BlockPos center, int radius) {
        List<BlockEntity> entities = new ArrayList<>();
        int minX = center.getX() - radius;
        int maxX = center.getX() + radius;
        int minY = Math.max(0, center.getY() - radius);
        int maxY = Math.min(255, center.getY() + radius);
        int minZ = center.getZ() - radius;
        int maxZ = center.getZ() + radius;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockEntity entity = Minecraft.getInstance().level.getBlockEntity(pos);
                    if (entity != null) {
                        entities.add(entity);
                    }
                }
            }
        }
        return entities;
    }
}