package com.github.hoqhuuep.islandcraft;

import java.io.IOException;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppedEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

import com.github.hoqhuuep.islandcraft.core.ICLogger;
import com.github.hoqhuuep.islandcraft.core.IslandDatabase;
import com.google.inject.Inject;

import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

@Plugin(id = "islandcraft", name = "IslandCraft", version = "1.0.8")
public class IslandCraftPlugin {
	@Inject
	private Logger logger;

	@Inject
	@DefaultConfig(sharedRoot = false)
	private ConfigurationLoader<CommentedConfigurationNode> configLoader;

	private CommentedConfigurationNode config;

	@Listener
	public void onGameInitialization(GameInitializationEvent event) throws IOException, SQLException {
		// Logging
		ICLogger.logger = new Slf4jLogger(logger);

		// Metrics
		// https://github.com/Hidendra/Plugin-Metrics/wiki/Usage
		try {
			Metrics metrics = new Metrics(Sponge.getGame(), Sponge.getPluginManager().fromInstance(this).get());
			metrics.start();
		} catch (final Exception e) {
			ICLogger.logger.warning("Failed to start MCStats");
		}

		// Configuration
		config = configLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
		if (!"1.0.0".equals(config.getNode("config-version").setComment("Do not modify config-version").getString("1.0.0"))) {
			logger.error("Invalid config-version found in './config/islandcraft/islandcraft.config'. Must be '1.0.0'.");
			return;
		}

		// Database
		SqlService sqlService = Sponge.getServiceManager().provide(SqlService.class).get();
		IslandDatabase database = new JdbcIslandDatabase(sqlService, config.getNode("database"));

		// Modify world generator
		WorldGeneratorModifier islandCraftGeneratorModifier = new IslandCraftGeneratorModifier(config.getNode("worlds"), database);
		Sponge.getRegistry().register(WorldGeneratorModifier.class, islandCraftGeneratorModifier);
	}

	@Listener
	public void onGameStopped(GameStoppedEvent event) throws IOException {
		if (config != null) {
			configLoader.save(config);
		}
	}
}
