package sir.dev.client.block.sculk_monolith;

import sir.dev.common.block.SculkMonolith;
import sir.dev.common.block.entity.SculkMonolithEntity;
import software.bernie.example.block.entity.FertilizerBlockEntity;
import software.bernie.example.client.model.block.FertilizerModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class SculkMonolithRenderer extends GeoBlockRenderer<SculkMonolithEntity> {
    public SculkMonolithRenderer() {
        super(new SculkMonolithModel());
    }
}
