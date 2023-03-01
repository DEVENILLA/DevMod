package sir.dev.client.item.dev;

import net.minecraft.util.Identifier;
import sir.dev.DevMod;
import sir.dev.common.item.dev.DevItem;
import software.bernie.geckolib.model.GeoModel;

public class DevItemModel extends GeoModel<DevItem> {
    @Override
    public Identifier getModelResource(DevItem dev) {
        Identifier mdl = new Identifier(DevMod.MOD_ID, "geo/item/dev.geo.json");
        return mdl;
    }

    @Override
    public Identifier getTextureResource(DevItem dev) {
        Identifier tex = new Identifier(DevMod.MOD_ID, "textures/custom/dev/dev.png");
        return tex;
    }

    @Override
    public Identifier getAnimationResource(DevItem dev) {
        Identifier anim = new Identifier(DevMod.MOD_ID, "animations/dev.animation.json");
        return anim;
    }
}
