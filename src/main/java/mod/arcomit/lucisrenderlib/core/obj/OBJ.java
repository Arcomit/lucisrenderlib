package mod.arcomit.lucisrenderlib.core.obj;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;

public class OBJ {

    public Obj objloader(ResourceLocation location) throws IOException {
        InputStream stream = Minecraft.getInstance().getResourceManager().getResource(location).get().open();
        Obj obj = ObjReader.read(stream);
        return obj;
    }
}
