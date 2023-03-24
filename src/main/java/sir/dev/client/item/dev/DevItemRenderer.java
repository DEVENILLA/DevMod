package sir.dev.client.item.dev;

import net.minecraft.util.Identifier;
import sir.dev.DevMod;
import sir.dev.common.item.dev.DevItem;
import software.bernie.example.item.JackInTheBoxItem;
import software.bernie.geckolib.GeckoLib;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class DevItemRenderer extends GeoItemRenderer<DevItem> {
    public DevItemRenderer() {
        super(new DefaultedItemGeoModel<>(new Identifier(DevMod.MOD_ID, "dev_item")));
    }
}
