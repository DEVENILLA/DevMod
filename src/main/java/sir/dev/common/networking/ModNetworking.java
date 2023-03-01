package sir.dev.common.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import sir.dev.DevMod;
import sir.dev.common.networking.packets.DevCallC2SPacket;
import sir.dev.common.networking.packets.ExampleC2SPacket;

public class ModNetworking
{
    public static final Identifier EXAMPLE_ID = new Identifier(DevMod.MOD_ID, "example");
    public static final Identifier CALL_DEV_ID = new Identifier(DevMod.MOD_ID, "call_dev");

    //C2S
    public static void registerClientToServerPackets()
    {
        ServerPlayNetworking.registerGlobalReceiver(CALL_DEV_ID, DevCallC2SPacket::receive);
    }

    //S2C
    public static void registerServerToClientPackets()
    {

    }
}
