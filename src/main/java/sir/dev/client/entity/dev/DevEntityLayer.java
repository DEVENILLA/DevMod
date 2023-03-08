package sir.dev.client.entity.dev;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import sir.dev.DevMod;
import sir.dev.common.entity.dev.DevEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class DevEntityLayer extends GeoRenderLayer<DevEntity> {

    public DevEntityLayer(GeoRenderer<DevEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    protected Identifier getTextureResource(DevEntity dev) {
        return DevEntityModel.getGlowTextureResource(dev);
    }

    protected RenderLayer getRenderType(DevEntity dev) {
        return RenderLayer.getEntityTranslucentEmissive(getTextureResource(dev));
    }

    @Override
    public void render(MatrixStack poseStack, DevEntity dev, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        RenderLayer emissiveRenderType = this.getRenderType(dev);

        this.getRenderer().reRender(bakedModel, poseStack, bufferSource, dev, emissiveRenderType,
                bufferSource.getBuffer(emissiveRenderType), partialTick, 15728640, OverlayTexture.DEFAULT_UV,
                1, 1, 1, 1);
    }


}
