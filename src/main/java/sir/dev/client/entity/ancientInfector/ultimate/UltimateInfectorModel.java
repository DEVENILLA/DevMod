package sir.dev.client.entity.ancientInfector.ultimate;

import net.minecraft.util.Identifier;
import sir.dev.DevMod;
import sir.dev.common.entity.ancientInfector.AncientInfector;
import sir.dev.common.entity.ancientInfector.UltimateInfector;
import sir.dev.common.entity.ancientInfector.UltimateInfectorMinion;
import software.bernie.geckolib.model.GeoModel;

public class UltimateInfectorModel extends GeoModel<UltimateInfector> {

    @Override
    public Identifier getModelResource(UltimateInfector dev) {
        Identifier mdl = new Identifier(DevMod.MOD_ID, "geo/entity/ultimate_infector.geo.json");
        return mdl;
    }

    @Override
    public Identifier getTextureResource(UltimateInfector dev) {

        Identifier tex = new Identifier(DevMod.MOD_ID, "textures/entity/ancient_infector/ultimate_infector.png");
        return tex;
    }

    @Override
    public Identifier getAnimationResource(UltimateInfector dev) {
        Identifier anim = new Identifier(DevMod.MOD_ID, "animations/ultimate_infector.animation.json");
        return anim;
    }
}