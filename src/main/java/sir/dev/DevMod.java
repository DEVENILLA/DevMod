package sir.dev;

import net.fabricmc.api.ModInitializer;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.dev.client.screen.ModScreenHandlers;
import sir.dev.common.block.ModBlocks;
import sir.dev.common.entity.ModEntities;
import sir.dev.common.item.ModItemGroup;
import sir.dev.common.item.ModItems;
import sir.dev.common.networking.ModNetworking;
import sir.dev.common.sound.ModSounds;
import software.bernie.geckolib.GeckoLib;

import java.util.List;

public class DevMod implements ModInitializer {
	public static final String MOD_ID = "devmod";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		GeckoLib.initialize();

		ModItemGroup.register();
		ModItems.register();

		ModEntities.register();
		ModBlocks.register();

		ModScreenHandlers.register();

		ModSounds.register();

		ModNetworking.registerClientToServerPackets();

		LOGGER.info("Hello Fabric world!");
	}
}