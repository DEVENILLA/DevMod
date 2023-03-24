package sir.dev.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Nameable;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sir.dev.DevMod;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.item.dev.DevItem;
import sir.dev.common.util.DEV_CONSTS;
import sir.dev.common.util.IEntityDataSaver;

@Mixin(Entity.class)
public abstract class EntityDimensionTransportMixin implements Nameable, EntityLike, CommandOutput
{
    @Inject(at = @At("HEAD"), method = "moveToWorld")
    private void moveToWorldInjector(ServerWorld destination, CallbackInfoReturnable info)
    {
        DevMod.LOGGER.info("step0 + " + ((Object)this).getClass().getName());
        if (((Entity)(Object)this) instanceof PlayerEntity player)
        {
            DevMod.LOGGER.info("step1");
            DevEntity dev = DevItem.GetDevFromPlayer(player);
            DevMod.LOGGER.info(dev != null ? "yes" : "no");
            if (dev != null)
            {
                DevMod.LOGGER.info("step2");
                dev.moveDevToWorld(destination);
            }
        }
    }
}
