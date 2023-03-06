package sir.dev.common.networking.packets;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import sir.dev.common.entity.dev.DevEntity;
import sir.dev.common.item.dev.DevItem;

public class DevSwitchAI
{
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender)
    {
        //Everything here happens only on the server
        if (DevItem.PlayerHasDevAlive(player))
        {
            DevEntity dev = DevItem.GetDevFromPlayer(player);

            if (dev != null && dev.isAlive())
            {
                dev.setDevAIcontrol(dev.IsDevAIcontrolled() == true ? false : true);
            }
        }
    }
}
