package sir.dev.client.entity.dev;

import net.fabricmc.tinyremapper.extension.mixin.common.Logger;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import sir.dev.DevMod;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.util.DEV_CONSTS;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.DynamicGeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.shadowed.eliotlash.mclib.math.functions.classic.Abs;


public class DevEntityRenderer extends DynamicGeoEntityRenderer<DevEntity> {

    private static final String LEFT_HAND = "lefthand";
    private static final String RIGHT_HAND = "righthand";
    public ItemStack mainHandItem;
    public ItemStack offhandItem;

    public DevEntityRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new DevEntityModel());
        this.shadowRadius = .3f;

        // Add some held item rendering
        addRenderLayer(new BlockAndItemGeoLayer<>(this)
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
                    poseStack.translate(0, 0, -0.1);

                    poseStack.scale(1f, 1f, 1f);
                    if (stack.getItem() instanceof ShieldItem)
                    {
                        poseStack.scale(.4f, .4f, .4f);
                        poseStack.translate(0, 0, -0.1);
                    }
                }
                else if (stack == DevEntityRenderer.this.offhandItem) {
                    poseStack.translate(0, 0, 0.1);
                    poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90f));

                    poseStack.scale(1f, 1f, 1f);
                    if (stack.getItem() instanceof ShieldItem){
                        poseStack.translate(0, 0, .1);
                        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                        poseStack.scale(.4f, .4f, .4f);
                    }
                }

                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });
    }

    @Override
    public void preRender(MatrixStack poseStack, DevEntity animatable, BakedGeoModel model, VertexConsumerProvider bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        this.mainHandItem = animatable.getMainHandStack();
        this.offhandItem = animatable.getOffHandStack();
    }
}