package com.simplexservers.minecraft.badgeofinfamy;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * The various settings each player can modify.
 */
public class PlayerSettings {

	private static final File SETTINGS_FILE = new File(Main.getInstance().getDataFolder(), "player_settings.json");
	public static HashMap<UUID, PlayerSettings> playerSettings = new HashMap<>();

	/**
	 * If the user should be shown vote notifications.
	 */
	private boolean showVoteNotification = true;

	/**
	 * If the user should be shown vote notifications.
	 *
	 * @return true if notifications are enabled, false if disabled.
	 */
	public boolean showVoteNotification() {
		return showVoteNotification;
	}

	/**
	 * Sets whether vote notifications should be shown to the player.
	 * Saves player settings asynchronously.
	 *
	 * @param showVoteNotification Vote notification state.
	 */
	public void setShowVoteNotification(boolean showVoteNotification) {
		setShowVoteNotification(showVoteNotification, true);
	}

	/**
	 * Sets whether vote notifications should be shown to the player.
	 *
	 * @param showVoteNotification Vote notification state.
	 * @param save Whether to save the player settings asynchronously after changing the setting.
	 */
	public void setShowVoteNotification(boolean showVoteNotification, boolean save) {
		this.showVoteNotification = showVoteNotification;
		if (save) {
			new BukkitRunnable() {
				@Override
				public void run() {
					savePlayerSettings();
				}
			}.runTaskAsynchronously(Main.getInstance());
		}
	}

	/**
	 * Gets the PlayerSettings for the Bukkit player.
	 *
	 * @param player The player to get the settings for.
	 * @return The settings for the player.
	 */
	public static PlayerSettings getPlayerSettings(Player player) {
		return getPlayerSettings(player.getUniqueId());
	}

	/**
	 * Gets the PlayerSettings for the player with the given UUID.
	 * Creates a new PlayerSettings if one doesn't not yet exist.
	 *
	 * @param uuid The UUID of the player to get the settings for.
	 * @return The settings for the player.
	 */
	public static PlayerSettings getPlayerSettings(UUID uuid) {
		synchronized (playerSettings) {
			PlayerSettings settings = playerSettings.get(uuid);

			// Create a new PlayerSettings if one doesn't exist
			if (settings == null) {
				settings = new PlayerSettings();
				playerSettings.put(uuid, settings);
			}

			return settings;
		}
	}

	/**
	 * Saves the player settings to the file. The save function IS thread-safe.
	 */
	public static void savePlayerSettings() {
		JSONObject players = new JSONObject();

		synchronized (playerSettings) {
			// Build the JSON object of player settings
			for (Map.Entry<UUID, PlayerSettings> entry : playerSettings.entrySet()) {
				PlayerSettings settings = entry.getValue();

				JSONObject jsonSettings = new JSONObject();
				jsonSettings.put("vote_notification", settings.showVoteNotification);

				players.put(entry.getKey().toString(), jsonSettings);
			}

			// Save the file
			try (FileWriter writer = new FileWriter(SETTINGS_FILE)) {
				players.writeJSONString(writer);
			} catch (IOException e) {
				Main.getInstance().getLogger().log(Level.SEVERE, "Could not save the player settings!", e);
			}
		}
	}

	/**
	 * Loads the player settings from the file. The load function IS thread-safe.
	 */
	public static void loadPlayerSettings() {
		if (!SETTINGS_FILE.exists())
			return;

		synchronized (playerSettings) {
			// Load the file & parse
			JSONParser parser = new JSONParser();
			try (FileReader reader = new FileReader(SETTINGS_FILE)) {
				JSONObject players = (JSONObject) parser.parse(reader);

				// Process the JSON data
				for (Map.Entry<String, JSONObject> entry : (Set<Map.Entry<String, JSONObject>>) players.entrySet()) {
					UUID uuid = UUID.fromString(entry.getKey());
					JSONObject jsonSettings = entry.getValue();

					PlayerSettings settings = new PlayerSettings();
					settings.showVoteNotification = (boolean) jsonSettings.getOrDefault("vote_notification", settings.showVoteNotification);

					playerSettings.put(uuid, settings);
				}
			} catch (Exception e) {
				Main.getInstance().getLogger().log(Level.SEVERE, "Could not load the player settings!", e);
			}
		}
	}

}
