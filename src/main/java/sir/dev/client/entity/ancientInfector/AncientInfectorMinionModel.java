package sir.dev.client.entity.ancientInfector;

import net.minecraft.util.Identifier;
import sir.dev.DevMod;
import sir.dev.common.entity.ancientInfector.AncientInfector;
import sir.dev.common.entity.ancientInfector.AncientInfectorMinion;
import software.bernie.geckolib.model.GeoModel;

public class AncientInfectorMinionModel extends GeoModel<AncientInfectorMinion> {

    @Override
    public Identifier getModelResource(AncientInfectorMinion dev) {
        Identifier mdl = new Identifier(DevMod.MOD_ID, "geo/entity/ancient_infector_minion.geo.json");
        return mdl;
    }

    @Override
    public Identifier getTextureResource(AncientInfectorMinion dev) {

        Identifier tex = new Identifier(DevMod.MOD_ID, "textures/entity/ancient_infector/ancient_infector_minion.png");
        return tex;
    }

    @Override
    public Identifier getAnimationResource(AncientInfectorMinion dev) {
        Identifier anim = new Identifier(DevMod.MOD_ID, "animations/ancient_infector_minion.animation.json");
        return anim;
    }
}