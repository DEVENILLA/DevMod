package sir.dev.client.entity.dev;

import net.fabricmc.tinyremapper.extension.mixin.common.Logger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.GlowSquidEntityRenderer;
import net.minecraft.client.render.entity.feature.EndermanEyesFeatureRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import sir.dev.DevMod;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.util.DEV_CONSTS;
import sir.dev.common.util.DevHealthState;
import sir.dev.common.util.DevState;
import software.bernie.example.client.renderer.entity.layer.CoolKidGlassesLayer;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.texture.AutoGlowingTexture;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.loading.json.raw.Bone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.DynamicGeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.shadowed.eliotlash.mclib.math.functions.classic.Abs;


public class DevEntityRenderer extends GeoEntityRenderer<DevEntity> {
    private static final String LEFT_HAND = "lefthand";
    private static final String RIGHT_HAND = "righthand";
    public ItemStack mainHandItem;
    public ItemStack offhandItem;

    public DevEntityRenderer(EntityRendererFactory.Context renderManager) {

        super(renderManager, new DevEntityModel());

        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));

        this.addRenderLayer(new BlockAndItemGeoLayer<>(this)
        {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, DevEntity animatable) {
                // Retrieve the items in the entity's hands for the relevant bone
                return switch (bone.getName()) {
                    case LEFT_HAND -> DevEntityRenderer.this.offhandItem;
                    case RIGHT_HAND -> DevEntityRenderer.this.mainHandItem;
                    default -> null;
                };
            }

            @Override
            protected ModelTransformation.Mode getTransformTypeForStack(GeoBone bone, ItemStack stack, DevEntity animatable) {
                // Apply the camera transform for the given hand
                return switch (bone.getName()) {
                    case LEFT_HAND, RIGHT_HAND -> ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND;
                    default -> ModelTransformation.Mode.NONE;
                };
            }

            // Do some quick render modifications depending on what the item is
            @Override
            protected void renderStackForBone(MatrixStack poseStack, GeoBone bone, ItemStack stack, DevEntity animatable,
                                              VertexConsumerProvider bufferSource, float partialTick, int packedLight, int packedOverlay) {

                if (stack == DevEntityRenderer.this.mainHandItem) {
                    poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90f));
                    poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
                    poseStack.translate(0, 0, 0);

                    poseStack.scale(1f, 1f, 1f);
                    if (stack.getItem() instanceof ShieldItem)
                    {
                        poseStack.scale(.4f, .4f, .4f);
                        poseStack.translate(0, 0, -0.1);
                    }
                }
                else if (stack == DevEntityRenderer.this.offhandItem) {
                    poseStack.translate(0, 0, 0);
                    poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90f));
                    poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));

                    poseStack.scale(1f, 1f, 1f);
                    if (stack.getItem() instanceof ShieldItem){
                        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                        poseStack.translate(0, -0.1, .1);
                        poseStack.scale(.4f, .4f, .4f);
                    }
                }

                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });

        this.shadowRadius = .3f;
    }

    @Override
    public void preRender(MatrixStack poseStack, DevEntity animatable, BakedGeoModel model, VertexConsumerProvider bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        this.mainHandItem = animatable.getMainHandStack();
        this.offhandItem = animatable.getOffHandStack();
    }
}
