package sir.dev.client.entity.ancientInfector;

import net.minecraft.client.render.entity.EntityRendererFactory;
import sir.dev.common.entity.ancientInfector.AncientInfector;
import sir.dev.common.entity.ancientInfector.AncientInfectorMinion;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;


public class AncientInfectorMinionRenderer extends GeoEntityRenderer<AncientInfectorMinion> {

    public AncientInfectorMinionRenderer(EntityRendererFactory.Context renderManager) {

        super(renderManager, new AncientInfectorMinionModel());

        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));

        this.shadowRadius = .6f;
    }
}
