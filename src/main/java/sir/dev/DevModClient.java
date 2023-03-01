package sir.dev;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import sir.dev.client.entity.dev.DevEntityRenderer;
import sir.dev.client.screen.ModScreenHandlers;
import sir.dev.client.screen.dev.DevScreen;
import sir.dev.common.entity.ModEntities;
import sir.dev.common.event.KeyInputHandler;
import sir.dev.common.networking.ModNetworking;

public class DevModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.DEV, DevEntityRenderer::new);

        KeyInputHandler.register();

        ModNetworking.registerServerToClientPackets();

        HandledScreens.register(ModScreenHandlers.DEV_SCREEN_HANDLER_TYPE, DevScreen::new);
    }
}
