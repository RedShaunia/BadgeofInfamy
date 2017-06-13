package com.simplexservers.minecraft.badgeofinfamy.log;

import com.simplexservers.minecraft.badgeofinfamy.PunishedPlayer;
import org.json.simple.JSONObject;

import java.util.Date;

/**
 * Represents a punishment that occurred.
 */
public class LoggedPunishment {

	/**
	 * The reason the player was punished.
	 */
	private String reason;
	/**
	 * The time that the punishment occurred.
	 */
	private long dateTime;

	/**
	 * Creates a new punishment based on a player who's currently punished and the current time.
	 *
	 * @param punishedPlayer The currently punished player.
	 */
	public LoggedPunishment(PunishedPlayer punishedPlayer) {
		this(punishedPlayer.getReason(), System.currentTimeMillis() / 1000L);
	}

	public LoggedPunishment(String reason, long dateTime) {
		this.reason = reason;
		this.dateTime = dateTime;
	}

	/**
	 * Gets the reason why the player was punished.
	 *
	 * @return The punishment reason.
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * Gets the date at which the punishment occurred.
	 *
	 * @return When the punishment occurred.
	 */
	public Date getDate() {
		return new Date(dateTime * 1000);
	}

	/**
	 * Gets the date, in the form of an epoch-time stamp, at which the punishment occurred.
	 *
	 * @return When the punishment occurred.
	 */
	public long getDateTime() {
		return dateTime;
	}

	/**
	 * Serializes the punishment in the form of a JSONObject.
	 *
	 * @return The punishment serialized as JSON.
	 */
	public JSONObject serializeJSON() {
		JSONObject json = new JSONObject();
		json.put("reason", reason);
		json.put("date", dateTime);
		return json;
	}

	/**
	 * Tries to deserialize the JSONObject into a LoggedPunishment.
	 *
	 * @param json The JSON to parse.
	 * @return The LoggedPunishment deserialized from JSON.
	 * @throws IllegalArgumentException If an error occurred deserializing the JSON.
	 */
	public static LoggedPunishment deserializeJSON(JSONObject json) throws IllegalArgumentException {
		try {
			String reason = (String) json.get("reason");
			long dateTime = (long) json.get("date");

			return new LoggedPunishment(reason, dateTime);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Error parsing logged punishment.", e);
		}
	}

}
