package sir.dev;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.impl.blockrenderlayer.BlockRenderLayerMapImpl;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import sir.dev.client.block.sculk_monolith.SculkMonolithRenderer;
import sir.dev.client.entity.ancientInfector.AncientInfectorMinionRenderer;
import sir.dev.client.entity.ancientInfector.AncientInfectorRenderer;
import sir.dev.client.entity.ancientInfector.ultimate.UltimateInfectorMinionRenderer;
import sir.dev.client.entity.ancientInfector.ultimate.UltimateInfectorRenderer;
import sir.dev.client.entity.dev.DevEntityRenderer;
import sir.dev.client.hud.dev.DevHudOverlay;
import sir.dev.client.screen.ModScreenHandlers;
import sir.dev.client.screen.dev.DevScreen;
import sir.dev.client.screen.dev.Damaged2DevScreen;
import sir.dev.client.screen.dev.Damaged1DevScreen;
import sir.dev.client.screen.dev.Damaged3DevScreen;
import sir.dev.client.screen.dev.Damaged4DevScreen;
import sir.dev.common.block.ModBlocks;
import sir.dev.common.block.entity.ModBlockEntities;
import sir.dev.common.entity.ModEntities;
import sir.dev.common.entity.ancientInfector.UltimateInfector;
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
        EntityRendererRegistry.register(ModEntities.ANCIENT_INFECTOR, AncientInfectorRenderer::new);
        EntityRendererRegistry.register(ModEntities.ANCIENT_INFECTOR_MINION, AncientInfectorMinionRenderer::new);
        EntityRendererRegistry.register(ModEntities.ULTIMATE_INFECTOR, UltimateInfectorRenderer::new);
        EntityRendererRegistry.register(ModEntities.ULTIMATE_INFECTOR_MINION, UltimateInfectorMinionRenderer::new);

        BlockEntityRendererRegistry.register(ModBlockEntities.SCULK_MONOLITH,
                context -> new SculkMonolithRenderer());

        BlockRenderLayerMapImpl.INSTANCE.putBlock(ModBlocks.SCULK_MONOLITH, RenderLayer.getTranslucent());
    }
}
