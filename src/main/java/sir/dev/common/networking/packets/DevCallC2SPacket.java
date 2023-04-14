package sir.dev.common.networking.packets;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.item.dev.DevItem;
import sir.dev.common.util.DEV_CONSTS;
import sir.dev.common.util.DevState;
import sir.dev.common.util.IEntityDataSaver;

public class DevCallC2SPacket
{
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender)
    {
        //Everything here happens only on the server
        if (DevItem.PlayerHasDevAlive(player))
        {
            DevEntity dev = DevItem.GetDevFromPlayer(player);

            if (dev instanceof DevEntity && dev != null && dev.isAlive())
            {
                if (dev.getDevState() == DevState.sitting) dev.setState(DevState.following);
                dev.setDevCalled(true);
                dev.ClearTargets();
                if (dev.lastCalledPos != null && dev.lastCalledPos == dev.getBlockPos() && dev.distanceTo(dev.getOwner()) > DevEntity.TARGET_DISTANCE)
                {
                    BlockPos blockPos = dev.getOwner().getBlockPos();

                    dev.refreshPositionAndAngles(blockPos.getX(), blockPos.getY(), blockPos.getZ(), dev.getYaw(), dev.getPitch());
                    dev.getNavigation().stop();

                    LightningEntity lighting = new LightningEntity(EntityType.LIGHTNING_BOLT, dev.world);
                    lighting.setPos(dev.getX(), dev.getY(), dev.getZ());
                    lighting.setCosmetic(true);
                    lighting.setOnFire(false);
                    dev.world.spawnEntity(lighting);
                }
                dev.lastCalledPos = dev.getBlockPos();
            }
        }
    }
}
