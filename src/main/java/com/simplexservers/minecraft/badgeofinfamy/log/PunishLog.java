package com.simplexservers.minecraft.badgeofinfamy.log;

import com.simplexservers.minecraft.badgeofinfamy.Main;
import com.simplexservers.minecraft.badgeofinfamy.PunishedPlayer;
import org.bukkit.OfflinePlayer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

/**
 * The manager of punishment logs.
 */
public class PunishLog {

	/**
	 * The directory of player logs.
	 */
	private static final File PLAYER_LOG_DIR = new File(Main.getInstance().getDataFolder(), "log");

	/**
	 * The cache of logged players.
	 */
	public static HashMap<UUID, LoggedPlayer> loggedPlayers = new HashMap<>();

	/**
	 * Handles logging and saving the currently punished player.
	 *
	 * @param punishedPlayer The punished player to log.
	 */
	public static void logPunishment(PunishedPlayer punishedPlayer) {
		LoggedPlayer loggedPlayer = getLoggedPlayer(punishedPlayer.getPlayerUUID());
		loggedPlayer.logPunishment(new LoggedPunishment(punishedPlayer));
		saveLoggedPlayer(punishedPlayer.getPlayerUUID());
	}

	/**
	 * Saves the LoggedPlayer with the given UUID.
	 *
	 * @param uuid The UUID of the player to save.
	 */
	public static void saveLoggedPlayer(UUID uuid) {
		if (!loggedPlayers.containsKey(uuid)) {
			// If it's not in the cache then there's nothing to save
			return;
		}
		LoggedPlayer player = loggedPlayers.get(uuid);

		File playerLogFile = getPlayerLogFile(uuid);
		if (!playerLogFile.exists()) {
			playerLogFile.getParentFile().mkdirs();
		}

		try (FileWriter writer = new FileWriter(playerLogFile)) {
			player.serializeJSON().writeJSONString(writer);
		} catch (IOException e) {
			Main.getInstance().getLogger().log(Level.WARNING, "An error occurred saving the player punishment log.", e);
		}
	}

	/**
	 * Gets the LoggedPlayer that represents the given Bukkit player.
	 * If a LoggedPlayer does not yet exist, one will be created.
	 *
	 * @param player The Bukkit player to use to get the LoggedPlayer.
	 * @return The LoggedPlayer matching the Bukkit player.
	 */
	public static LoggedPlayer getLoggedPlayer(OfflinePlayer player) {
		return getLoggedPlayer(player.getUniqueId());
	}

	/**
	 * Gets the LoggedPlayer with the given UUID.
	 * If a LoggedPlayer does not yet exist, one will b ecreated.
	 *
	 * @param uuid The Minecraft UUID of the player to get.
	 * @return The LoggedPlayer with the given UUID.
	 */
	public static LoggedPlayer getLoggedPlayer(UUID uuid) {
		if (!loggedPlayers.containsKey(uuid)) {
			File playerLogFile = getPlayerLogFile(uuid);
			LoggedPlayer player = null;
			if (playerLogFile.exists()) {
				// Load the LoggedPlayer from the file & cache
				JSONParser parser = new JSONParser();

				try (FileReader reader = new FileReader(playerLogFile)) {
					JSONObject json = (JSONObject) parser.parse(reader);
					player = LoggedPlayer.deserializeJSON(json);
				} catch (Exception e) {
					Main.getInstance().getLogger().log(Level.WARNING, "An error occurred parsing a player punshment log.", e);
				}
			}

			if (player == null) {
				player = new LoggedPlayer(uuid);
			}

			loggedPlayers.put(uuid, player);
			return player;
		}

		return loggedPlayers.get(uuid);
	}

	/**
	 * Gets the log file for the given player.
	 *
	 * @param uuid The UUID of the player to get the log file for.
	 * @return The log file for the player with the UUID.
	 */
	private static File getPlayerLogFile(UUID uuid) {
		return new File(PLAYER_LOG_DIR, uuid.toString() + ".json");
	}

}
