package sir.dev.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sir.dev.DevMod;
import sir.dev.common.block.entity.SculkMonolithEntity;
import sir.dev.common.item.ModItems;
import sir.dev.common.item.dev.SculkTumor;
import sir.dev.common.util.IFallingBlock;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemEntity.class)
public abstract class SculkTumorItemEntityMixin {

    @Shadow public abstract ItemStack getStack();

    @Inject(at = @At("HEAD"), method = "tick")
    private void tickInject(CallbackInfo info) {
        if (getStack().getItem() instanceof SculkTumor)
        {
            if (((ItemEntity)(Object)this).world instanceof ServerWorld serverWorld)
            {
                serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, ((ItemEntity)(Object)this).getX(), ((ItemEntity)(Object)this).getY(), ((ItemEntity)(Object)this).getZ(), 2, .5, .5, .5, .02);
                serverWorld.spawnParticles(ParticleTypes.SCULK_CHARGE_POP, ((ItemEntity)(Object)this).getX(), ((ItemEntity)(Object)this).getY(), ((ItemEntity)(Object)this).getZ(), 3, .6, .6, .6, .02);
            }

            List<AllayEntity> allays = new ArrayList<>();

            allays =  ((ItemEntity)(Object)this).world.getEntitiesByClass(
                    AllayEntity.class,
                    Box.of( ((ItemEntity)(Object)this).getPos(), 12, 12, 12),
                    livingEntity -> {
                        return true;
                    }
            );

            for (AllayEntity drop : allays)
            {
                double xDir = drop.getX() - ((ItemEntity)(Object)this).getX();
                double yDir = drop.getY() - ((ItemEntity)(Object)this).getY();
                double zDir = drop.getZ() - ((ItemEntity)(Object)this).getZ();
                double magnitude = Math.sqrt(Math.pow(xDir, 2)+Math.pow(yDir, 2)+Math.pow(zDir, 2));
                Vec3d Velocity = new Vec3d
                        (xDir/magnitude * -.05,
                                yDir/magnitude * -.05,
                                zDir/magnitude * -.05);
                if (((ItemEntity)(Object)this).world instanceof ServerWorld serverWorld)
                {
                    serverWorld.spawnParticles(ParticleTypes.SCULK_CHARGE_POP, drop.getX(), drop.getY(), drop.getZ(), 1, .01, .03, .01, .01);
                }
                drop.move(MovementType.SELF ,Velocity);
                if (drop.distanceTo(((ItemEntity)(Object)this)) < 1)
                {
                    LightningEntity lighting = new LightningEntity(EntityType.LIGHTNING_BOLT, ((ItemEntity)(Object)this).world);
                    lighting.setPos(((ItemEntity)(Object)this).getX(), ((ItemEntity)(Object)this).getY(), ((ItemEntity)(Object)this).getZ());
                    lighting.setCosmetic(true);
                    ((ItemEntity)(Object)this).world.spawnEntity(lighting);
                    drop.playSound( SoundEvents.ENTITY_WARDEN_ROAR, 100, 1.0f );
                    ItemEntity item = new ItemEntity(((ItemEntity)(Object)this).world, ((ItemEntity)(Object)this).getX(), ((ItemEntity)(Object)this).getY(), ((ItemEntity)(Object)this).getZ(), new ItemStack(ModItems.DEV_ITEM, 1));
                    ((ItemEntity)(Object)this).world.spawnEntity(item);
                    drop.discard();
                    ((ItemEntity)(Object)this).discard();
                }
            }
        }
    }
}