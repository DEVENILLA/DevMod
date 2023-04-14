package sir.dev.client.entity.ancientInfector.ultimate;

import net.minecraft.client.render.entity.EntityRendererFactory;
import sir.dev.common.entity.ancientInfector.AncientInfector;
import sir.dev.common.entity.ancientInfector.UltimateInfector;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;


public class UltimateInfectorRenderer extends GeoEntityRenderer<UltimateInfector> {

    public UltimateInfectorRenderer(EntityRendererFactory.Context renderManager) {

        super(renderManager, new UltimateInfectorModel());

        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));

        this.shadowRadius = .6f;
    }

    @Override
    protected float getDeathMaxRotation(UltimateInfector animatable) {
        return 0;
    }
}
