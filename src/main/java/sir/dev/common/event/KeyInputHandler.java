package sir.dev.common.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import sir.dev.common.networking.ModNetworking;
import sir.dev.common.util.DEV_CONSTS;

public class KeyInputHandler
{
    public static final String KEY_CATEGORY_DEV = "key.category.devmod.dev";
    public static final String KEY_CALL_DEV = "key.devmod.call_dev";
    public static final String KEY_CHANGE_DEV_STATE = "key.devmod.change_state_dev";
    public static final String KEY_USE_MAIN = "key.devmod.use_dev_main";
    public static final String KEY_USE_OFF = "key.devmod.use_dev_off";
    public static final String KEY_SWITCH_AI = "key.devmod.dev_switch_ai";
    public static final String KEY_DEV_TARGET = "key.devmod.dev_target";

    public static KeyBinding callDevKey;
    public static KeyBinding changeDevStateKey;
    public static KeyBinding useMainKey;
    public static KeyBinding useOffKey;
    public static KeyBinding switchAIKey;
    public static KeyBinding devTargetKey;

    public static void registerKeyInputs() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(callDevKey.wasPressed()) {
                ClientPlayNetworking.send(ModNetworking.CALL_DEV_ID, PacketByteBufs.create());
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(changeDevStateKey.wasPressed()) {
                ClientPlayNetworking.send(ModNetworking.CHANGE_DEV_STATE_ID, PacketByteBufs.create());
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(useMainKey.wasPressed()) {
                ClientPlayNetworking.send(ModNetworking.USE_DEV_MAIN_ITEM_ID, PacketByteBufs.create());
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(useOffKey.wasPressed()) {
                ClientPlayNetworking.send(ModNetworking.USE_DEV_OFF_ITEM_ID, PacketByteBufs.create());
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(switchAIKey.wasPressed()) {
                ClientPlayNetworking.send(ModNetworking.SWITCH_DEV_AI_ID, PacketByteBufs.create());
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(devTargetKey.wasPressed()) {
                ClientPlayNetworking.send(ModNetworking.DEV_OWNER_SETS_TARGET_ID, PacketByteBufs.create());
            }
        });
    }

    public static void register() {
        callDevKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_CALL_DEV,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KEY_CATEGORY_DEV//adds it to the key group
        ));
        changeDevStateKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_CHANGE_DEV_STATE,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                KEY_CATEGORY_DEV//adds it to the key group
        ));
        useMainKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_USE_MAIN,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                KEY_CATEGORY_DEV//adds it to the key group
        ));
        useOffKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_USE_OFF,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                KEY_CATEGORY_DEV//adds it to the key group
        ));
        switchAIKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_SWITCH_AI,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                KEY_CATEGORY_DEV//adds it to the key group
        ));
        devTargetKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_DEV_TARGET,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_CAPS_LOCK,
                KEY_CATEGORY_DEV//adds it to the key group
        ));

        registerKeyInputs();
    }
}
