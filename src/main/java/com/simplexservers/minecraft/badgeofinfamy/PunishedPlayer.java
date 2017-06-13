package com.simplexservers.minecraft.badgeofinfamy;

import com.simplexservers.minecraft.promptutils.Time;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Represents a player that is under punishment.
 */
public final class PunishedPlayer {

	/**
	 * The Minecraft UUID of the player being punished.
	 */
	private final UUID playerUUID;
	/**
	 * The description of the reason the player is being punished.
	 */
	private final String reason;
	/**
	 * The epoch-time stamp of when the player is allowed to speak in chat.
	 */
	private final long chatSpeakTime;
	/**
	 * The epoch-time stamp of when the vote is open for the player to be pardoned.
	 */
	private final long voteOpenTime;
	/**
	 * The required number of votes for the player to be pardoned.
	 */
	private final int requiredVotes;
	/**
	 * The array of players that have voted for pardon.
	 */
	private ArrayList<UUID> playerVotes;
	/**
	 * The previous groups the player was assigned to.
	 */
	private final String[] previousGroups;

	private PunishedPlayer(UUID playerUUID, String reason, long chatSpeakTime, long voteOpenTime, int requiredVotes, String[] previousGroups) {
		this(playerUUID, reason, chatSpeakTime, voteOpenTime, requiredVotes, new ArrayList<>(), previousGroups);
	}

	private PunishedPlayer(UUID playerUUID, String reason, long chatSpeakTime, long voteOpenTime, int requiredVotes, ArrayList<UUID> playerVotes, String[] previousGroups) {
		this.playerUUID = playerUUID;
		this.reason = reason;
		this.chatSpeakTime = chatSpeakTime;
		this.voteOpenTime = voteOpenTime;
		this.requiredVotes = requiredVotes;
		this.playerVotes = playerVotes;
		this.previousGroups = previousGroups;
	}

	/**
	 * Gets if the player is allowed to speak based on their mute duration.
	 *
	 * @return true if the player can speak in chat, false if they should be muted.
	 */
	public boolean isAllowedToSpeak() {
		return System.currentTimeMillis() / 1000L > chatSpeakTime;
	}

	/**
	 * Gets if the vote to pardon the player is open.
	 *
	 * @return true if the pardon vote is open, false if it's closed.
	 */
	public boolean isVoteOpen() {
		return System.currentTimeMillis() / 1000L >= voteOpenTime;
	}

	/**
	 * Gets the epoch time stamp for when the player is allowed to speak in chat.
	 *
	 * @return The epoch time stamp that the player is un-muted.
	 */
	public long getChatSpeakTime() {
		return chatSpeakTime;
	}

	/**
	 * Gets the epoch time stamp for when other players are allowed to vote to pardon the current player.
	 *
	 * @return The epoch time stamp for when the player can be voted to be pardoned.
	 */
	public long getVoteOpenTime() {
		return voteOpenTime;
	}

	/**
	 * Gets the required number of votes for the player to be pardoned.
	 *
	 * @return The required pardon vote amount.
	 */
	public int getRequiredVotes() {
		return requiredVotes;
	}

	/**
	 * Logs the player as casting their vote to pardon the punished player.
	 */
	public void castVote(Player player) {
		playerVotes.add(player.getUniqueId());
	}

	/**
	 * Gets if the player has already cast their vote.
	 *
	 * @param player The player to check against.
	 * @return true if the player has voted, false if they have not.
	 */
	public boolean hasVoted(Player player) {
		return hasVoted(player.getUniqueId());
	}

	/**
	 * Gets if the player has already cast their vote.
	 *
	 * @param uuid The Minecraft UUID of the player to check against.
	 * @return true if the player has voted, false if they have not.
	 */
	public boolean hasVoted(UUID uuid) {
		return playerVotes.contains(uuid);
	}

	/**
	 * Gets the current number of votes the player has to be pardoned.
	 *
	 * @return The number of votes received for the player to be pardoned.
	 */
	public int getCurrentVotes() {
		return playerVotes.size();
	}

	/**
	 * Gets the list of player UUID's that have voted for the player.
	 *
	 * @return Player's who have voted to pardon the player.
	 */
	public Collection<UUID> getVoters() {
		return (Collection<UUID>) playerVotes.clone();
	}

	/**
	 * Gets the Minecraft UUID of the player.
	 *
	 * @return The player's UUID.
	 */
	public UUID getPlayerUUID() {
		return playerUUID;
	}

	/**
	 * Gets the reason why the player was punished.
	 *
	 * @return The reason the player was punished.
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * Gets the previous groups the player belonged to.
	 *
	 * @return The player's previous groups before being punished.
	 */
	public String[] getPreviousGroups() {
		return previousGroups.clone();
	}

	/**
	 * Serializes the player into a JSONObject.
	 *
	 * @return The player serialized to a JSONObject.
	 */
	public JSONObject serializeJSON() {
		JSONObject player = new JSONObject();
		player.put("uuid", getPlayerUUID().toString());
		player.put("reason", getReason());
		player.put("chat_time", getChatSpeakTime());
		player.put("vote_time", getVoteOpenTime());
		player.put("required_votes", getRequiredVotes());

		JSONArray playerVotes = new JSONArray();
		this.playerVotes.forEach(uuid -> playerVotes.add(uuid.toString()));
		player.put("votes", playerVotes);

		JSONArray previousGroups = new JSONArray();
		Collections.addAll(previousGroups, getPreviousGroups());
		player.put("groups", previousGroups);

		return player;
	}

	/**
	 * Deserializes the JSONObject to a PunishedPlayer.
	 *
	 * @param player The JSONObject to deserialize.
	 * @return The deserialized PunishedPlayer.
	 * @throws IllegalArgumentException If there was an error parsing the JSONObject.
	 */
	public static PunishedPlayer deserializeJSON(JSONObject player) throws IllegalArgumentException {
		try {
			UUID uuid = UUID.fromString((String) player.get("uuid"));
			String reason = (String) player.get("reason");
			long chatSpeakTime = (long) player.get("chat_time");
			long voteOpenTime = (long) player.get("vote_time");
			int requiredVotes = (int) (long) player.get("required_votes");

			JSONArray playerVotesArr = (JSONArray) player.get("votes");
			ArrayList<UUID> playerVotes = new ArrayList<>();
			playerVotesArr.forEach(obj -> playerVotes.add(UUID.fromString((String) obj)));

			JSONArray previousGroupsArr = (JSONArray) player.get("groups");
			String[] previousGroups = new String[previousGroupsArr.size()];
			for (int i = 0; i < previousGroupsArr.size(); i++) {
				previousGroups[i] = (String) previousGroupsArr.get(i);
			}

			return new PunishedPlayer(uuid, reason, chatSpeakTime, voteOpenTime, requiredVotes, playerVotes, previousGroups);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("The JSONObject could not be parsed to a PunishedPlayer.", e);
		}
	}

	/**
	 * A builder for a punished player.
	 */
	public static class PunishedPlayerBuilder {

		/**
		 * The player being punished.
		 */
		private OfflinePlayer player;
		/**
		 * The previous groups the player was assigned to.
		 */
		private String[] previousGroups;
		/**
		 * The reason the player is being punished.
		 */
		private String reason = null;
		/**
		 * The duration until the player is allowed to speak again.
		 */
		private Time chatMuteDuration = null;
		/**
		 * The duration until the player is allowed to be voted to be pardoned.
		 */
		private Time voteWaitDuration = null;
		/**
		 * The required number of votes for the player to be pardoned.
		 */
		private int requiredVotes = 0;

		public PunishedPlayerBuilder(OfflinePlayer player) {
			this.player = player;
			this.previousGroups = Main.getInstance().getPermissionsHook().getPlayerGroups(null, player);
		}

		/**
		 * Gets the player being punished.
		 *
		 * @return The OfflinePlayer being punished.
		 */
		public OfflinePlayer getPlayer() {
			return player;
		}

		/**
		 * Gets the previous groups the player belonged to before being punished.
		 *
		 * @return The groups the player was assigned to.
		 */
		public String[] getPreviousGroups() {
			return previousGroups.clone();
		}

		/**
		 * Gets the reason the player is being punished.
		 *
		 * @return The reason for the punishment.
		 */
		public String getReason() {
			return reason;
		}

		/**
		 * Sets the reason for why the player is being punished.
		 *
		 * @param reason Punishment reason.
		 */
		public void setReason(String reason) {
			this.reason = reason;
		}

		/**
		 * Gets the duration until the player is allowed to speak in chat.
		 *
		 * @return The time duration until the player is un-muted in chat.
		 */
		public Time getChatMuteDuration() {
			return chatMuteDuration;
		}

		/**
		 * Sets the delay for how long the player should be muted in chat.
		 *
		 * @param duration The duration for how long the player should be muted.
		 */
		public void setChatMuteDuration(Time duration) {
			chatMuteDuration = duration;
		}

		/**
		 * Gets the duration until the player is able to be voted to be pardoned.
		 *
		 * @return The time duration until the player can be voted to be pardoned.
		 */
		public Time getVoteWaitDuration() {
			return voteWaitDuration;
		}

		/**
		 * Sets the delay fo rhow long the player has to wait until they can be voted to be pardoned.
		 *
		 * @param duration The duration until the player can be pardoned.
		 */
		public void setVoteWaitDuration(Time duration) {
			voteWaitDuration = duration;
		}

		/**
		 * Gets the number of required votes for the player to be pardoned.
		 *
		 * @return The pardon vote amount.
		 */
		public int getRequiredVotes() {
			return requiredVotes;
		}

		/**
		 * Sets the number of required votes for the player to be pardoned.
		 *
		 * @param requiredVotes The pardon vote amount.
		 */
		public void setRequiredVotePercentage(int requiredVotes) {
			this.requiredVotes = requiredVotes;
		}

		/**
		 * Validates that the PunishedPlayerBuilder settings are able to be constructed into a PunishedPlayer.
		 *
		 * @return true if there are no issues, false otherwise.
		 */
		public boolean validate() {
			return reason != null && chatMuteDuration != null && voteWaitDuration != null && requiredVotes > 0;
		}

		/**
		 * Builds the PunishedPlayer from the set settings.
		 *
		 * @return The PunishedPlayer that was constructed.
		 * @throws IllegalArgumentException If there are invalid settings for the PunishedPlayer.
		 */
		PunishedPlayer build() throws IllegalArgumentException {
			if (!validate()) {
				throw new IllegalArgumentException("There are unset required variables.");
			}

			return new PunishedPlayer(player.getUniqueId(), reason, chatMuteDuration.durationToEpochTime(), voteWaitDuration.durationToEpochTime(), requiredVotes, previousGroups);
		}

	}

}
