package sir.dev.client.item.dev;

import sir.dev.common.item.dev.DevItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class DevItemRenderer extends GeoItemRenderer<DevItem> {
    public DevItemRenderer()
    {
        super(new DevItemModel());
    }
}
