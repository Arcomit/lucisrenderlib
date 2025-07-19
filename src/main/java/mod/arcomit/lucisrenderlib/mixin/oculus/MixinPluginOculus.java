package mod.arcomit.lucisrenderlib.mixin.oculus;

import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPluginOculus implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (LoadingModList.get().getModFileById("oculus") != null){
            if (mixinClassName.equals("mod.arcomit.lucisrenderlib.mixin.oculus.MixinParticleEngineB")){
                return true;
            }
        }else {
            if (mixinClassName.equals("mod.arcomit.lucisrenderlib.mixin.oculus.MixinParticleEngineA")){
                return true;
            }
        }

        return false;
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

}
