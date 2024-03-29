package sir.dev.common.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import sir.dev.DevMod;
import sir.dev.common.networking.packets.*;

public class ModNetworking
{
    public static final Identifier EXAMPLE_ID = new Identifier(DevMod.MOD_ID, "example");
    public static final Identifier CALL_DEV_ID = new Identifier(DevMod.MOD_ID, "call_dev");
    public static final Identifier CHANGE_DEV_STATE_ID = new Identifier(DevMod.MOD_ID, "change_dev_state");
    public static final Identifier USE_DEV_MAIN_ITEM_ID = new Identifier(DevMod.MOD_ID, "dev_use_main");
    public static final Identifier USE_DEV_OFF_ITEM_ID = new Identifier(DevMod.MOD_ID, "dev_use_off");;
    public static final Identifier SWITCH_DEV_AI_ID = new Identifier(DevMod.MOD_ID, "dev_switch_ai");
    public static final Identifier DEV_OWNER_SETS_TARGET_ID = new Identifier(DevMod.MOD_ID, "dev_target");
    public static final Identifier DEV_OPEN_MENU = new Identifier(DevMod.MOD_ID, "dev_open");

    //C2S
    public static void registerClientToServerPackets()
    {
        ServerPlayNetworking.registerGlobalReceiver(CALL_DEV_ID, DevCallC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(CHANGE_DEV_STATE_ID, OnDevChangeStateButtonC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(DEV_OWNER_SETS_TARGET_ID, OnDevOwnerSetsTarget::receive);
        ServerPlayNetworking.registerGlobalReceiver(DEV_OPEN_MENU, DevOpenMenuC2SPacket::receive);
    }

    //S2C
    public static void registerServerToClientPackets()
    {

    }
}
