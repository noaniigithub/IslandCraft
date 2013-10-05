package com.github.hoqhuuep.islandcraft.bukkit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.github.hoqhuuep.islandcraft.bukkit.config.WorldConfig;
import com.github.hoqhuuep.islandcraft.bukkit.terraincontrol.BiomeIndex;
import com.github.hoqhuuep.islandcraft.bukkit.terraincontrol.IslandCraftBiomeGenerator;
import com.github.hoqhuuep.islandcraft.common.Geometry;
import com.github.hoqhuuep.islandcraft.common.api.ICPlayer;
import com.github.hoqhuuep.islandcraft.common.api.ICServer;
import com.github.hoqhuuep.islandcraft.common.api.ICWorld;
import com.github.hoqhuuep.islandcraft.common.type.ICBiome;
import com.github.hoqhuuep.islandcraft.common.type.ICLocation;
import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.TerrainControl;

public class BukkitWorld implements ICWorld {
	private final World world;
	private final WorldConfig config;
	private final ICServer server;

	private static final long ONE_DAY = 24000;
	private static final long HOURS_PER_DAY = 24;
	private static final long MINUTES_PER_HOUR = 60;
	private static final long ONE_HOUR = ONE_DAY / HOURS_PER_DAY;
	private static final long ONE_MINUTE = ONE_HOUR / MINUTES_PER_HOUR;
	private static final long HOUR_ORIGIN = 6 * ONE_HOUR;

	public BukkitWorld(final World world, final WorldConfig config, final ICServer server) {
		this.world = world;
		this.config = config;
		this.server = server;
	}

	@Override
	public final List<ICPlayer> getPlayers() {
		final List<Player> players = world.getPlayers();
		final List<ICPlayer> result = new ArrayList<ICPlayer>(players.size());
		for (final Player p : players) {
			result.add(server.findOnlinePlayer(p.getName()));
		}
		return result;
	}

	@Override
	public final ICLocation getSpawnLocation() {
		return BukkitUtils.convertLocation(world.getSpawnLocation());
	}

	@Override
	public final String getTime() {
		// TODO seems to be 63 minutes in an hour???
		final long time = world.getTime();
		final long hour = ((time + HOUR_ORIGIN) / ONE_HOUR) % HOURS_PER_DAY;
		final long minute = ((time + HOUR_ORIGIN) % ONE_HOUR) / ONE_MINUTE;
		return String.format("%02d:%02d", new Long(hour), new Long(minute));
	}

	@Override
	public final String getName() {
		return world.getName();
	}

	@Override
	public final boolean isNormalWorld() {
		return world.getEnvironment() == World.Environment.NORMAL;
	}

	@Override
	public final ICServer getServer() {
		return server;
	}

	@Override
	public final Geometry getGeometry() {
		final LocalWorld tcWorld = TerrainControl.getWorld(world.getName());
		if (tcWorld == null || IslandCraftBiomeGenerator.class != tcWorld.getSettings().biomeMode) {
			return null;
		}
		final ICBiome[] biomes = BiomeIndex.getBiomes(tcWorld, config);
		return new Geometry(config.getIslandSizeChunks(), config.getIslandGapChunks(), config.getResourceIslandRarity(), biomes);
	}

	public final World getBukkitWorld() {
		// TODO check again if this is needed
		return world;
	}
}
