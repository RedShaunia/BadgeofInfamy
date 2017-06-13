package com.simplexservers.minecraft.badgeofinfamy;

import com.simplexservers.minecraft.badgeofinfamy.commands.GeneralCommands;
import com.simplexservers.minecraft.badgeofinfamy.log.PunishLog;
import com.simplexservers.minecraft.badgeofinfamy.votes.VoteGUI;
import com.simplexservers.minecraft.badgeofinfamy.votes.VoteNotificationTimer;
import com.simplexservers.minecraft.bukkitutils.gui.InventoryGUI;
import com.simplexservers.minecraft.bukkitutils.gui.InventoryGUIEntry;
import com.simplexservers.minecraft.promptutils.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class PunishManager {

	private static final File PUNISHED_PLAYERS_FILE = new File(Main.getInstance().getDataFolder(), "punished_players.json");

	private static HashMap<UUID, PunishedPlayer> punishedPlayers = new HashMap<>();

	/**
	 * Gets the PunishedPlayer from the Bukkit player object.
	 *
	 * @param player The Bukkit player being punished.
	 * @return The PunishedPlayer object or null if they are not under punishment.
	 */
	public static PunishedPlayer getPunishedPlayer(OfflinePlayer player) {
		return getPunishedPlayer(player.getUniqueId());
	}

	/**
	 * Gets the PunishedPlayer from the player's Minecraft UUID.
	 *
	 * @param playerUUID The Minecraft UUID of the player being punished.
	 * @return The PunishedPlayer object or null if they are not under punishment.
	 */
	public static PunishedPlayer getPunishedPlayer(UUID playerUUID) {
		return punishedPlayers.get(playerUUID);
	}

	/**
	 * Gets a copy of the array of the punished players.
	 *
	 * @return The players that are being punished.
	 */
	public static Collection<PunishedPlayer> getPunishedPlayers() {
		return new ArrayList<>(punishedPlayers.values());
	}

	// TODO Fix duplicates
	public static PunishedPlayer punishPlayer(PunishedPlayer.PunishedPlayerBuilder builder) throws IllegalArgumentException {
		PunishedPlayer punishedPlayer = builder.build();

		// Remove their previous groups
		for (String group : builder.getPreviousGroups()) {
			Main.getInstance().getPermissionsHook().playerRemoveGroup(null, builder.getPlayer(), group);
		}

		// Set their restricted group
		Main.getInstance().getPermissionsHook().playerAddGroup(null, builder.getPlayer(), Main.getInstance().getPluginConfig().PUNISHED_GROUP);

		punishedPlayers.put(punishedPlayer.getPlayerUUID(), punishedPlayer);
		saveCachedPlayers();

		// Notify the vote timer
		VoteNotificationTimer.scheduleNextNotification();

		// Log the punishment
		PunishLog.logPunishment(punishedPlayer);

		// Notify admins
		String username = Main.getInstance().getNameResolver().getUsername(punishedPlayer.getPlayerUUID());
		String message = ChatColor.GOLD + username + ChatColor.YELLOW + " has just been punished for: " +
				ChatColor.WHITE + punishedPlayer.getReason() + "\n" + ChatColor.YELLOW + "Use " + ChatColor.GOLD +
				"/boi details " + username + ChatColor.YELLOW + " for additional information.";
		Bukkit.getOnlinePlayers().stream()
				.filter(player -> player.hasPermission(GeneralCommands.BASE_PERMISSION + ".admin.notifications"))
				.forEach(admin -> admin.sendMessage(message));
		return punishedPlayer;
	}

	public static void freePlayer(OfflinePlayer player) {
		PunishedPlayer punishedPlayer = punishedPlayers.get(player.getUniqueId());
		if (punishedPlayer == null) {
			return;
		}

		// Remove their restricted group
		Main.getInstance().getPermissionsHook().playerRemoveGroup(null, player, Main.getInstance().getPluginConfig().PUNISHED_GROUP);

		// Set their previous groups
		for (String group : punishedPlayer.getPreviousGroups()) {
			Main.getInstance().getPermissionsHook().playerAddGroup(null, player, group);
		}

		punishedPlayers.remove(punishedPlayer.getPlayerUUID());
		saveCachedPlayers();

		if (player.isOnline()) {
			((Player) player).sendMessage(ChatColor.GREEN + "You have been pardoned by your fellow players! You have returned to your normal rank.");
		}

		// Notify admins
		String username = Main.getInstance().getNameResolver().getUsername(punishedPlayer.getPlayerUUID());
		String adminMessage = ChatColor.GOLD + username + ChatColor.YELLOW + " has just been pardoned with " +
				punishedPlayer.getCurrentVotes() + " votes.";
		Bukkit.getOnlinePlayers().stream()
				.filter(p -> p.hasPermission(GeneralCommands.BASE_PERMISSION + ".admin.notifications"))
				.forEach(admin -> admin.sendMessage(adminMessage));

		// Notify online players
		String message = ChatColor.YELLOW + "A player you've voted for, " + username + ", has just been pardoned.";
		punishedPlayer.getVoters().forEach(voterUUID -> {
			Player voter = Bukkit.getPlayer(voterUUID);
			if (voter != null && voter.isOnline()) {
				voter.sendMessage(message);
			}
		});
	}

	public static void saveCachedPlayers() {
		JSONArray users = new JSONArray();

		punishedPlayers.values().stream().forEach(p -> users.add(p.serializeJSON()));

		try (FileWriter writer = new FileWriter(PUNISHED_PLAYERS_FILE)) {
			users.writeJSONString(writer);
		} catch (IOException e) {
			Main.getInstance().getLogger().log(Level.SEVERE, "An error occurred saving the punished players.", e);
		}
	}

	public static void loadCachedPlayers() {
		if (PUNISHED_PLAYERS_FILE.exists()) {
			JSONParser parser = new JSONParser();

			try (FileReader reader = new FileReader(PUNISHED_PLAYERS_FILE)) {
				JSONArray users = (JSONArray) parser.parse(reader);

				users.forEach(playerObj -> {
					JSONObject player = (JSONObject) playerObj;
					try {
						PunishedPlayer punishedPlayer = PunishedPlayer.deserializeJSON(player);
						punishedPlayers.put(punishedPlayer.getPlayerUUID(), punishedPlayer);
					} catch (IllegalArgumentException e) {
						Main.getInstance().getLogger().log(Level.SEVERE, "An error occurred loading a punished player.", e);
					}
				});

				VoteNotificationTimer.scheduleNextNotification();
			} catch (Exception e) {
				Main.getInstance().getLogger().log(Level.SEVERE, "An error occurred loading the punished players.", e);
			}
		}
	}

	/**
	 * Handles a player voting for a punished player to be pardoned.
	 *
	 * @param punishedPlayer The punished player being voted for.
	 * @param voter The player voting for the punished player to be pardoned.
	 */
	public static void playerPardonVote(PunishedPlayer punishedPlayer, Player voter) {
		String punishedPlayerUsername = Main.getInstance().getNameResolver().getUsername(punishedPlayer.getPlayerUUID());

		if (punishedPlayer.hasVoted(voter)) {
			voter.sendMessage(ChatColor.RED + "You have already voted to pardon " + punishedPlayerUsername + ".");
			return;
		}

		punishedPlayer.castVote(voter);

		if (punishedPlayer.getCurrentVotes() >= punishedPlayer.getRequiredVotes()) {
			freePlayer(Bukkit.getOfflinePlayer(punishedPlayer.getPlayerUUID()));
		} else {
			saveCachedPlayers();
		}

		voter.sendMessage(ChatColor.YELLOW + "Thanks for your input! We have cast your ballot to pardon " + punishedPlayerUsername);

		// Update currently open GUI's.
		Main.getInstance().getGUIManager().getOpenGUIs().forEach(entry -> {
			Player player = entry.getKey();
			InventoryGUI gui = entry.getValue();

			if (player.isOnline()) {
				Collection<Integer> matchingSlots = gui.getSlotsWithValue(punishedPlayer.getPlayerUUID());
				matchingSlots.forEach(slot -> {
					InventoryGUIEntry newEntry = VoteGUI.getPunishedPlayerEntry(player, punishedPlayer);
					if (newEntry != null) {
						gui.updateEntry(slot, newEntry);
					} else {
						gui.removeEntry(slot);
					}
				});
				player.updateInventory();
			}
		});
	}

}
