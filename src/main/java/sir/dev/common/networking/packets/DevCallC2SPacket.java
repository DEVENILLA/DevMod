package sir.dev.common.networking.packets;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.item.dev.DevItem;
import sir.dev.common.util.DEV_CONSTS;
import sir.dev.common.util.IEntityDataSaver;

public class DevCallC2SPacket
{
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender)
    {
        //Everything here happens only on the server
        if (DevItem.PlayerHasDevAlive(player))
        {
            IEntityDataSaver playerData = (IEntityDataSaver)player;
            NbtCompound playerNBT = playerData.getPersistentData();
            if (playerNBT.contains(DEV_CONSTS.NBT_KEY_OWNED_DEV))
            {
                DevEntity dev = (DevEntity) player.world.getEntityById(playerNBT.getInt(DEV_CONSTS.NBT_KEY_OWNED_DEV));

                if (dev instanceof DevEntity && dev != null && dev.isAlive())
                {
                    dev.setPosition(player.getPos());
                }
            }
        }
    }
}
