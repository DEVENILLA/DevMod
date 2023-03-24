package sir.dev.common.networking.packets;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.item.dev.DevItem;
import sir.dev.common.util.DEV_CONSTS;
import sir.dev.common.util.DevState;

import java.util.List;

public class OnDevOwnerSetsTarget
{
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender)
    {
        //Everything here happens only on the server
        if (DevItem.PlayerHasDevAlive(player))
        {
            DevEntity dev = DevItem.GetDevFromPlayer(player);

            if (dev != null && dev.isAlive() && dev.getDevState() == DevState.defending)
            {
                LivingEntity target = null;

                List<MobEntity> entities = player.getWorld().getEntitiesByClass(
                        MobEntity.class,
                        Box.of(player.getPos(), 256, 256, 256),
                        livingEntity -> {
                            if (livingEntity == dev || livingEntity == dev.getOwner()) return false;
                            if (!IsViableTarget(dev, livingEntity)) return false;
                            if (!isPlayerStaring(player, livingEntity)) return false;
                            return true;
                        }
                );

                if (entities.size() > 0) target = entities.get(0);

                if (target != null) dev.setTarget(target);
            }
        }
    }


    public static boolean isPlayerStaring(PlayerEntity player, MobEntity entity) {
        Vec3d vec3d = player.getRotationVec(1.0F).normalize();
        Vec3d vec3d2 = new Vec3d(entity.getX() - player.getX(), entity.getEyeY() - player.getEyeY(), entity.getZ() - player.getZ());
        double d = vec3d2.length();
        vec3d2 = vec3d2.normalize();
        double e = vec3d.dotProduct(vec3d2);
        return e > 1.0D - 0.025D / d ? player.canSee(entity) : false;
    }

    public static boolean IsViableTarget(DevEntity dev, MobEntity entity)
    {
        if (dev.getDevState() != DevState.defending || dev.IsDevCalled() == true || dev.getOwner() == null)
        {
            return false;
        }

        if (DEV_CONSTS.GetDistance(entity.getPos(), dev.getPos()) >= dev.CHASE_DISTANCE*2)
        {
            return false;
        }

        if (entity == dev)
        {
            return false;
        }

        if (entity instanceof TameableEntity tamed && tamed.isTamed() && tamed.getOwner() != null && tamed.getOwner() == dev.getOwner())
        {
            return false;
        }

        if (entity == dev.getOwner())
        {
            return false;
        }

        return true;
    }
}
