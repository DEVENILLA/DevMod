package sir.dev.client.entity.ancientInfector.ultimate;

import net.minecraft.client.render.entity.EntityRendererFactory;
import sir.dev.common.entity.ancientInfector.AncientInfectorMinion;
import sir.dev.common.entity.ancientInfector.UltimateInfectorMinion;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;


public class UltimateInfectorMinionRenderer extends GeoEntityRenderer<UltimateInfectorMinion> {

    public UltimateInfectorMinionRenderer(EntityRendererFactory.Context renderManager) {

        super(renderManager, new UltimateInfectorMinionModel());

        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));

        this.shadowRadius = .6f;
    }
}
