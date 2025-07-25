package mod.arcomit.lucisrenderlib.core.obj;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.opengl.GL11;

public class TextureLoader {
    private static final TextureManager textureManager = Minecraft.getInstance().getTextureManager();

    // 加载并绑定纹理
    public static int loadTexture(ResourceLocation location) {
        // 检查纹理是否已加载
        if (textureManager.getTexture(location, null) == null) {
            try {
                // 加载纹理
                textureManager.register(location, new SimpleTexture(location));
            } catch (Exception e) {
                // 加载失败时使用默认纹理
                return loadTexture(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/missing_texture.png"));
            }
        }

        // 获取纹理ID
        return textureManager.getTexture(location, null).getId();
    }

    // 绑定方块纹理图集（用于获取特定方块的纹理）
    public static void bindBlockAtlas() {
        TextureAtlas blockAtlas = Minecraft.getInstance().getModelManager().getBlockModelShaper().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, blockAtlas.getId());
    }

    // 获取特定方块状态对应的UV坐标
    public static float[] getBlockUVs(BlockState state) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModel(state).getParticleIcon();
        return new float[]{
                sprite.getU0(), sprite.getV0(),
                sprite.getU1(), sprite.getV1()
        };
    }
}