package sir.dev;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.impl.blockrenderlayer.BlockRenderLayerMapImpl;
import net.fabricmc.fabric.impl.client.rendering.EntityRendererRegistryImpl;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import sir.dev.client.entity.dev.DevEntityRenderer;
import sir.dev.client.hud.dev.DevHudOverlay;
import sir.dev.client.screen.ModScreenHandlers;
import sir.dev.client.screen.dev.DevScreen;
import sir.dev.common.entity.ModEntities;
import sir.dev.common.event.KeyInputHandler;
import sir.dev.common.networking.ModNetworking;
import software.bernie.example.registry.BlockRegistry;

public class DevModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerRenderers();

        KeyInputHandler.register();

        ModNetworking.registerServerToClientPackets();

        HandledScreens.register(ModScreenHandlers.DEV_SCREEN_HANDLER_TYPE, DevScreen::new);

        HudRenderCallback.EVENT.register(new DevHudOverlay());
    }

    public static void registerRenderers()
    {
        EntityRendererRegistry.register(ModEntities.DEV, DevEntityRenderer::new);
    }
}
