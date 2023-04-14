package sir.dev.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sir.dev.DevMod;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.item.ModItems;
import sir.dev.common.item.dev.DevItem;
import sir.dev.common.util.DEV_CONSTS;

import java.rmi.registry.Registry;

@Mixin(ServerPlayerEntity.class)
public abstract class EntityDimensionTransportMixin
{

    @Inject(at = @At("HEAD"), method = "moveToWorld")
    private void moveToWorldInjector(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        try {
            if (((ServerPlayerEntity) (Object) this) != null) {
                ServerPlayerEntity ServerPlayer = ((ServerPlayerEntity) (Object) this);
                PlayerEntity player = ((PlayerEntity) (Object) this);
                DevEntity dev = DevItem.GetDevFromPlayer(((ServerPlayerEntity) (Object) this));
                if (dev != null) {
                    ItemStack devItem = new ItemStack(ModItems.DEV_ITEM, 1);

                    DEV_CONSTS.setInventoryStacks(devItem.getNbt(), dev.getInventoryStacks());
                    DEV_CONSTS.setState(devItem.getNbt(), dev.getState());
                    devItem.getNbt().putBoolean(DEV_CONSTS.NBT_KEY_AI_CONTROL, dev.IsDevAIcontrolled());
                    DEV_CONSTS.setHP(devItem.getNbt(), (int)dev.getHealth());
                    DEV_CONSTS.setOwner(devItem.getNbt(), player.getUuid());

                    if (player.getMainHandStack().isEmpty())
                        player.setStackInHand(Hand.MAIN_HAND, devItem);
                    else
                        player.giveItemStack(devItem);

                    dev.discard();
                }
            }
        } catch (Exception e) {

        }
    }

    @Inject(at = @At("HEAD"), method = "onDeath")
    private void onDeathInjector(DamageSource damageSource, CallbackInfo ci)
    {
        ServerPlayerEntity player = ((ServerPlayerEntity)(Object)this);

        if (player != null)
        {
            DevEntity dev = DevItem.GetDevFromPlayer(player);
            if (dev != null)
            {
                if (!player.getWorld().isClient)
                {
                    RegistryKey<World> spawnDimension = player.getSpawnPointDimension();
                    BlockPos spawnPoint = player.getSpawnPointPosition() != null ? player.getSpawnPointPosition() : player.getWorld().getSpawnPos();
                    try
                    {
                        dev.setPosition(spawnPoint.toCenterPos());
                        dev.moveToWorld(player.server.getWorld(spawnDimension));
                    }
                    catch (Exception ex)
                    {
                        DevMod.LOGGER.error(ex.toString());
                    }
                }
            }
        }
    }
}
