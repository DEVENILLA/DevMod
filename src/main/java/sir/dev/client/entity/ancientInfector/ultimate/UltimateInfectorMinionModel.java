package sir.dev.client.entity.ancientInfector.ultimate;

import net.minecraft.util.Identifier;
import sir.dev.DevMod;
import sir.dev.common.entity.ancientInfector.AncientInfectorMinion;
import sir.dev.common.entity.ancientInfector.UltimateInfectorMinion;
import software.bernie.geckolib.model.GeoModel;

public class UltimateInfectorMinionModel extends GeoModel<UltimateInfectorMinion> {

    @Override
    public Identifier getModelResource(UltimateInfectorMinion dev) {
        Identifier mdl = new Identifier(DevMod.MOD_ID, "geo/entity/ancient_infector_minion.geo.json");
        return mdl;
    }

    @Override
    public Identifier getTextureResource(UltimateInfectorMinion dev) {

        Identifier tex = new Identifier(DevMod.MOD_ID, "textures/entity/ancient_infector/ancient_infector_minion.png");
        return tex;
    }

    @Override
    public Identifier getAnimationResource(UltimateInfectorMinion dev) {
        Identifier anim = new Identifier(DevMod.MOD_ID, "animations/ancient_infector_minion.animation.json");
        return anim;
    }
}