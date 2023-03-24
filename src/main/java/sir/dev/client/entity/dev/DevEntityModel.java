package sir.dev.client.entity.dev;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import sir.dev.DevMod;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.util.DEV_CONSTS;
import sir.dev.common.util.DevHealthState;
import sir.dev.common.util.DevState;
import software.bernie.geckolib.model.GeoModel;

public class DevEntityModel extends GeoModel<DevEntity> {

    @Override
    public Identifier getModelResource(DevEntity dev) {
        Identifier mdl = new Identifier(DevMod.MOD_ID, "geo/entity/dev.geo.json");
        DevHealthState state = DevHealthState.getHealthState(dev.getHealth(), (float) DEV_CONSTS.MAX_HP);
        if (state == DevHealthState.BIT_INFECTED)
        {
            mdl = new Identifier(DevMod.MOD_ID, "geo/entity/dev_damaged_1.geo.json");
        }
        else if (state == DevHealthState.NEAR_INFECTED)
        {
            mdl = new Identifier(DevMod.MOD_ID, "geo/entity/dev_damaged_2.geo.json");
        }
        else if (state == DevHealthState.ALMOST_INFECTED)
        {
            mdl = new Identifier(DevMod.MOD_ID, "geo/entity/dev_damaged_3.geo.json");
        }
        else if (state == DevHealthState.COMPLETELY_INFECTED)
        {
            mdl = new Identifier(DevMod.MOD_ID, "geo/entity/dev_damaged_4.geo.json");
        }
        return mdl;
    }

    @Override
    public Identifier getTextureResource(DevEntity dev) {

        Identifier tex = new Identifier(DevMod.MOD_ID, "textures/custom/dev/dev.png");
        DevHealthState state = DevHealthState.getHealthState(dev.getHealth(), (float) DEV_CONSTS.MAX_HP);
        if (state == DevHealthState.BIT_INFECTED)
        {
            tex = new Identifier(DevMod.MOD_ID, "textures/custom/dev/dev_damaged_1.png");
        }
        else if (state == DevHealthState.NEAR_INFECTED)
        {
            tex = new Identifier(DevMod.MOD_ID, "textures/custom/dev/dev_damaged_2.png");
        }
        else if (state == DevHealthState.ALMOST_INFECTED)
        {
            tex = new Identifier(DevMod.MOD_ID, "textures/custom/dev/dev_damaged_3.png");
        }
        else if (state == DevHealthState.COMPLETELY_INFECTED)
        {
            tex = new Identifier(DevMod.MOD_ID, "textures/custom/dev/dev_damaged_4.png");
        }

        return tex;
    }

    public static Identifier getGlowTextureResource(DevEntity dev) {

        Identifier tex = new Identifier(DevMod.MOD_ID, "textures/custom/dev/dev_glowmask.png");
        DevHealthState state = DevHealthState.getHealthState(dev.getHealth(), (float) DEV_CONSTS.MAX_HP);
        if (state == DevHealthState.BIT_INFECTED)
        {
            tex = new Identifier(DevMod.MOD_ID, "textures/custom/dev/dev_damaged_1_glowmask.png");
        }
        else if (state == DevHealthState.NEAR_INFECTED)
        {
            tex = new Identifier(DevMod.MOD_ID, "textures/custom/dev/dev_damaged_2_glowmask.png");
        }
        else if (state == DevHealthState.ALMOST_INFECTED)
        {
            tex = new Identifier(DevMod.MOD_ID, "textures/custom/dev/dev_damaged_3_glowmask.png");
        }
        else if (state == DevHealthState.COMPLETELY_INFECTED)
        {
            tex = new Identifier(DevMod.MOD_ID, "textures/custom/dev/dev_damaged_4_glowmask.png");
        }

        return tex;
    }

    @Override
    public Identifier getAnimationResource(DevEntity dev) {
        Identifier anim = new Identifier(DevMod.MOD_ID, "animations/dev.animation.json");
        DevHealthState state = DevHealthState.getHealthState(dev.getHealth(), (float) DEV_CONSTS.MAX_HP);
        if (state == DevHealthState.ALMOST_INFECTED || state == DevHealthState.COMPLETELY_INFECTED)
        {
            anim = new Identifier(DevMod.MOD_ID, "animations/dev_damaged_3.animation.json");
        }
        return anim;
    }
}