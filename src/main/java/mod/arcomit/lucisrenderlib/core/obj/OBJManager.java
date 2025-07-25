package mod.arcomit.lucisrenderlib.core.obj;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OBJManager {
    private static final Map<ResourceLocation, OBJModel> cache = new ConcurrentHashMap<>();

    public static OBJModel getOBJModel(ResourceLocation location) {
        // 尝试从缓存获取，一帧多次调取时比computeIfAbsent优化好。
        OBJModel cached = cache.get(location);
        if (cached != null) {
            return cached;
        }

        return cache.computeIfAbsent(location, loc -> {
            if (!Minecraft.getInstance().isSameThread()) {
                throw new IllegalStateException("OBJ model loading must occur on main render thread");
            }

            try (InputStream stream = Minecraft.getInstance().getResourceManager()
                    .getResource(loc)
                    .orElseThrow(() -> new IOException("Resource not found: " + loc))
                    .open()) {

                Obj obj = ObjReader.read(stream);
                obj = ObjUtils.convertToRenderable(obj);
                return new OBJModel(obj);

            } catch (IOException e) {
                throw new RuntimeException("Failed to load OBJ model: " + loc, e);
            }
        });
    }

    public static void clearCache() {
        cache.clear();
    }
}