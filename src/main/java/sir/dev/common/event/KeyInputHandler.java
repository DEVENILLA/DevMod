package sir.dev.common.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import sir.dev.common.networking.ModNetworking;

public class KeyInputHandler
{
    public static final String KEY_CATEGORY_DEV = "key.category.devmod.dev";
    public static final String KEY_CALL_DEV = "key.devmod.call_dev";

    public static KeyBinding callDevKey;

    public static void registerKeyInputs() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(callDevKey.wasPressed()) {
                ClientPlayNetworking.send(ModNetworking.CALL_DEV_ID, PacketByteBufs.create());
            }
        });
    }

    public static void register() {
        callDevKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_CALL_DEV,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                KEY_CATEGORY_DEV//adds it to the key group
        ));

        registerKeyInputs();
    }
}
