package sir.dev.client.block.sculk_monolith;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import sir.dev.DevMod;
import sir.dev.common.block.SculkMonolith;
import sir.dev.common.block.entity.SculkMonolithEntity;
import software.bernie.example.block.entity.FertilizerBlockEntity;
import software.bernie.geckolib.GeckoLib;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;

public class SculkMonolithModel extends DefaultedBlockGeoModel<SculkMonolithEntity> {
    public SculkMonolithModel() {
        super(new Identifier(DevMod.MOD_ID, "sculk_monolith"));
    }

    /**
     * Return the fertilizer animation path if it's raining, or the botarium animation path if not.
     */
    @Override
    public Identifier getAnimationResource(SculkMonolithEntity animatable) {
        return new Identifier(DevMod.MOD_ID, "animations/the_monolith.animation.json");
    }

    @Override
    public Identifier getModelResource(SculkMonolithEntity animatable) {
        return new Identifier(DevMod.MOD_ID, "geo/entity/the_monolith.geo.json");
    }

    /**
     * Return the fertilizer texture path if it's raining, or the botarium texture path if not.
     */
    @Override
    public Identifier getTextureResource(SculkMonolithEntity animatable) {
        return new Identifier(DevMod.MOD_ID, "textures/block/sculk_monolith.png");
    }

    @Override
    public RenderLayer getRenderType(SculkMonolithEntity animatable, Identifier texture) {
        return RenderLayer.getEntityTranslucent(getTextureResource(animatable));
    }
}
