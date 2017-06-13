package com.simplexservers.minecraft.badgeofinfamy.log;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Represents a player who has been punished currently or in the past.
 */
public class LoggedPlayer {

	/**
	 * The Minecraft UUID of the player.
	 */
	private UUID uuid;
	/**
	 * The list of punishments that player has received in the past.
	 */
	private ArrayList<LoggedPunishment> loggedPunishments;

	LoggedPlayer(UUID uuid) {
		this.uuid = uuid;
		this.loggedPunishments = new ArrayList<>();
	}

	private LoggedPlayer(UUID uuid, ArrayList<LoggedPunishment> loggedPunishments) {
		this.uuid = uuid;
		this.loggedPunishments = loggedPunishments;
	}

	/**
	 * Gets a cloned collection of the punishments.
	 *
	 * @return The logged punishments.
	 */
	public Collection<LoggedPunishment> getLoggedPunishments() {
		return (Collection<LoggedPunishment>) loggedPunishments.clone();
	}

	/**
	 * Logs the punishment.
	 *
	 * @param punishment The punishment to log.
	 */
	public void logPunishment(LoggedPunishment punishment) {
		loggedPunishments.add(punishment);
	}

	/**
	 * Serializes the LoggedPlayer to a JSONObject.
	 *
	 * @return The JSONObject that represents the LoggedPlayer.
	 */
	public JSONObject serializeJSON() {
		JSONObject json = new JSONObject();
		json.put("uuid", uuid.toString());

		JSONArray punishments = new JSONArray();
		loggedPunishments.forEach(punishment -> punishments.add(punishment.serializeJSON()));
		json.put("punishments", punishments);

		return json;
	}

	/**
	 * Tries to deserialize the JSONObject into a LoggedPlayer.
	 *
	 * @param json The JSONObject to deserialize.
	 * @return The deserialized LoggedPlayer.
	 * @throws IllegalArgumentException If an error occurred deserializing the JSONObject.
	 */
	public static LoggedPlayer deserializeJSON(JSONObject json) throws IllegalArgumentException {
		try {
			UUID uuid = UUID.fromString((String) json.get("uuid"));

			JSONArray punishments = (JSONArray) json.get("punishments");
			ArrayList<LoggedPunishment> loggedPunishments = new ArrayList<>();
			punishments.forEach(punishmentObj -> loggedPunishments.add(LoggedPunishment.deserializeJSON((JSONObject) punishmentObj)));

			return new LoggedPlayer(uuid, loggedPunishments);
		} catch (Exception e) {
			throw new IllegalArgumentException("An error occurred parsing the LoggedPlayer.", e);
		}
	}

}
