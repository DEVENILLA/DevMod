package sir.dev.client.entity.dev;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import sir.dev.DevMod;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.util.DEV_CONSTS;
import sir.dev.common.util.DevState;
import software.bernie.geckolib.model.GeoModel;

public class DevEntityModel extends GeoModel<DevEntity> {
    @Override
    public Identifier getModelResource(DevEntity dev) {
        Identifier mdl = new Identifier(DevMod.MOD_ID, "geo/entity/dev.geo.json");
        return mdl;
    }

    @Override
    public Identifier getTextureResource(DevEntity dev) {

        Identifier tex = new Identifier(DevMod.MOD_ID, "textures/custom/dev/dev.png");
        if (dev.getDevState() == DevState.sitting)
        {
            tex = new Identifier(DevMod.MOD_ID, "textures/custom/dev/dev_sit.png");
        }
        return tex;
    }

    @Override
    public Identifier getAnimationResource(DevEntity dev) {
        Identifier anim = new Identifier(DevMod.MOD_ID, "animations/dev.animation.json");
        return anim;
    }
}
