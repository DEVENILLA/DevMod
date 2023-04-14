package sir.dev.client.entity.ancientInfector;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import sir.dev.common.entity.ancientInfector.AncientInfector;
import sir.dev.common.entity.dev.DevEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;


public class AncientInfectorRenderer extends GeoEntityRenderer<AncientInfector> {

    public AncientInfectorRenderer(EntityRendererFactory.Context renderManager) {

        super(renderManager, new AncientInfectorModel());

        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));

        this.shadowRadius = .6f;
    }

    @Override
    protected float getDeathMaxRotation(AncientInfector animatable) {
        return 0;
    }
}
