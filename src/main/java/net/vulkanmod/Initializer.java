package net.vulkanmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.loader.api.FabricLoader;
import net.vulkanmod.config.Config;
import net.vulkanmod.config.Platform;
import net.vulkanmod.config.video.VideoModeManager;
import net.vulkanmod.render.chunk.build.frapi.VulkanModRenderer;
import net.vulkanmod.util.VkStructureHelper;
import net.vulkanmod.util.UnsafeAccessPatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public class Initializer implements ClientModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("VulkanMod");

	private static String VERSION;
	public static Config CONFIG;

	@Override
	public void onInitializeClient() {

		// Patch Unsafe access for Android Java 21 compatibility - MUST be first
		// This fixes NoSuchFieldError: sun.misc.Unsafe.UNSAFE issues
		UnsafeAccessPatcher.patch();

		VERSION = FabricLoader.getInstance()
				.getModContainer("vulkanmod")
				.get()
				.getMetadata()
				.getVersion().getFriendlyString();

		LOGGER.info("== VulkanMod ==");

		Platform.init();
		VideoModeManager.init();

		var configPath = FabricLoader.getInstance()
				.getConfigDir()
				.resolve("vulkanmod_settings.json");

		CONFIG = loadConfig(configPath);

		RendererAccess.INSTANCE.registerRenderer(VulkanModRenderer.INSTANCE);
	}

	/**
	 * Initialize Unsafe access helper for Android Java 21 compatibility.
	 * This must be called before any Vulkan initialization.
	 */
	private static void initializeUnsafeHelper() {
		try {
			if (VkStructureHelper.isUnsafeAvailable()) {
				LOGGER.debug("Unsafe initialized successfully for VkStructureHelper");
			} else {
				LOGGER.warn("Unsafe not available for VkStructureHelper - fallbacks will be used");
			}
		} catch (Exception e) {
			LOGGER.error("Failed to initialize Unsafe helper: " + e.getMessage(), e);
			// Don't fail completely, just warn
		}
	}

	private static Config loadConfig(Path path) {
		Config config = Config.load(path);

		if(config == null) {
			config = new Config();
			config.write();
		}

		return config;
	}

	public static String getVersion() {
		return VERSION;
	}
}
