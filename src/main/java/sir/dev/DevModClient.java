package sir.dev;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import sir.dev.client.entity.dev.DevEntityRenderer;
import sir.dev.client.hud.dev.DevHudOverlay;
import sir.dev.client.screen.ModScreenHandlers;
import sir.dev.client.screen.dev.DevScreen;
import sir.dev.client.screen.dev.Damaged2DevScreen;
import sir.dev.client.screen.dev.Damaged1DevScreen;
import sir.dev.client.screen.dev.Damaged3DevScreen;
import sir.dev.client.screen.dev.Damaged4DevScreen;
import sir.dev.common.entity.ModEntities;
import sir.dev.common.event.KeyInputHandler;
import sir.dev.common.networking.ModNetworking;

public class DevModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerRenderers();

        KeyInputHandler.register();

        ModNetworking.registerServerToClientPackets();

        HandledScreens.register(ModScreenHandlers.DEV_SCREEN_HANDLER_TYPE_DMG_1, Damaged1DevScreen::new);
        HandledScreens.register(ModScreenHandlers.DEV_SCREEN_HANDLER_TYPE_DMG_2, Damaged2DevScreen::new);
        HandledScreens.register(ModScreenHandlers.DEV_SCREEN_HANDLER_TYPE_DMG_3, Damaged3DevScreen::new);
        HandledScreens.register(ModScreenHandlers.DEV_SCREEN_HANDLER_TYPE_DMG_4, Damaged4DevScreen::new);
        HandledScreens.register(ModScreenHandlers.DEV_SCREEN_HANDLER_TYPE_NORMAL, DevScreen::new);

        HudRenderCallback.EVENT.register(new DevHudOverlay());
    }

    public static void registerRenderers()
    {
        EntityRendererRegistry.register(ModEntities.DEV, DevEntityRenderer::new);
    }
}
