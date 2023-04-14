package sir.dev.client.entity.ancientInfector;

import net.minecraft.util.Identifier;
import sir.dev.DevMod;
import sir.dev.common.entity.ancientInfector.AncientInfector;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.util.DEV_CONSTS;
import sir.dev.common.util.DevHealthState;
import software.bernie.geckolib.model.GeoModel;

public class AncientInfectorModel extends GeoModel<AncientInfector> {

    @Override
    public Identifier getModelResource(AncientInfector dev) {
        Identifier mdl = new Identifier(DevMod.MOD_ID, "geo/entity/ancient_infector.geo.json");
        return mdl;
    }

    @Override
    public Identifier getTextureResource(AncientInfector dev) {

        Identifier tex = new Identifier(DevMod.MOD_ID, "textures/entity/ancient_infector/ancient_infector.png");
        int percent = dev.getHealthPercentage();
        if (percent > 65)
        {
            tex = new Identifier(DevMod.MOD_ID, "textures/entity/ancient_infector/ancient_infector_boost_1.png");
        }
        if (percent > 75)
        {
            tex = new Identifier(DevMod.MOD_ID, "textures/entity/ancient_infector/ancient_infector_boost_2.png");
        }
        if (percent > 85)
        {
            tex = new Identifier(DevMod.MOD_ID, "textures/entity/ancient_infector/ancient_infector_boost_3.png");
        }
        return tex;
    }

    @Override
    public Identifier getAnimationResource(AncientInfector dev) {
        Identifier anim = new Identifier(DevMod.MOD_ID, "animations/ancient_infector.animation.json");
        return anim;
    }
}