package com.simplexservers.minecraft.badgeofinfamy.commands;

import com.simplexservers.minecraft.badgeofinfamy.Main;
import com.simplexservers.minecraft.badgeofinfamy.PunishManager;
import com.simplexservers.minecraft.badgeofinfamy.PunishedPlayer;
import com.simplexservers.minecraft.badgeofinfamy.log.LoggedPlayer;
import com.simplexservers.minecraft.badgeofinfamy.log.LoggedPunishment;
import com.simplexservers.minecraft.badgeofinfamy.log.PunishLog;
import com.simplexservers.minecraft.badgeofinfamy.prompts.PunishPrompt;
import com.simplexservers.minecraft.bukkitutils.commands.BukkitCommandInvoker;
import com.simplexservers.minecraft.commandutils.CommandHandler;
import com.simplexservers.minecraft.commandutils.CommandManager;
import com.simplexservers.minecraft.commandutils.CommandProperties;
import com.simplexservers.minecraft.commandutils.HelpCommandUtil;
import com.simplexservers.minecraft.promptutils.ChatColor;
import com.simplexservers.minecraft.promptutils.Time;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public class AdminCommands implements CommandHandler {

	private CommandManager cmdManager;

	public AdminCommands(CommandManager cmdManager) {
		this.cmdManager = cmdManager;
	}

	@CommandProperties(
			command = "boi admin",
			usage = "/boi admin",
			aliases = { "boi admin ?", "boi admin help" },
			description = "Shows the admin help page for BadgeOfInfamy.",
			permission = GeneralCommands.BASE_PERMISSION + ".admin.help"
	)
	public void boiAdminHelp(CommandSender sender) {
		sender.sendMessage(HelpCommandUtil.generateHelp(cmdManager, new BukkitCommandInvoker(sender), true));
	}

	@CommandProperties(
			command = "boi punish",
			usage = "/boi punish <playername>",
			description = "Starts the prompt to punish the player.",
			permission = GeneralCommands.BASE_PERMISSION + ".admin.punish"
	)
	public void punishPlayer(Player admin, OfflinePlayer victim) {
		if (victim == null) {
			admin.sendMessage(ChatColor.RED + "That player does not exist.");
			return;
		}

		// Start the punish prompt
		new PunishPrompt(admin, victim, Main.getInstance().getPromptListener(), builder -> {
			PunishManager.punishPlayer(builder);

			if (victim.isOnline()) {
				((Player) victim).sendMessage(ChatColor.RED + "You have been penalized for " + ChatColor.WHITE + builder.getReason() + ChatColor.RED + "." +
						" As a result, you have been muted in chat for " + ChatColor.WHITE + builder.getChatMuteDuration().formatRounded() + ChatColor.RED +
						" and you have been demoted to the rank " + ChatColor.WHITE + Main.getInstance().getPluginConfig().PUNISHED_GROUP + ChatColor.RED + " until other players vote to have you pardoned.");
			}
			String victimName = Main.getInstance().getNameResolver().getUsername(victim.getUniqueId());
			admin.sendMessage(victimName + ChatColor.GREEN + " has been successfully punished.");
		}).begin();
	}

	@CommandProperties(
			command = "boi pardon",
			usage = "/boi pardon <playername>",
			description = "Instantly pardons the player without the need for a vote.",
			permission = GeneralCommands.BASE_PERMISSION + ".admin.pardon"
	)
	public void pardonPlayer(Player admin, OfflinePlayer victim) {
		if (victim == null) {
			admin.sendMessage(ChatColor.RED + "That player does not exist.");
			return;
		}

		PunishManager.freePlayer(victim);
		String victimName = Main.getInstance().getNameResolver().getUsername(victim.getUniqueId());
		admin.sendMessage(ChatColor.GREEN + victimName + " has just been pardoned.");
	}

	@CommandProperties(
			command = "boi log",
			usage = "/boi log <playername>",
			description = "Gets the log of previous punishments the player has received.",
			permission = GeneralCommands.BASE_PERMISSION + ".admin.log"
	)
	public void viewPlayerLog(CommandSender admin, OfflinePlayer punishedPlayer) {
		if (punishedPlayer == null) {
			admin.sendMessage(ChatColor.RED + "That player does not exist.");
			return;
		}

		LoggedPlayer loggedPlayer = PunishLog.getLoggedPlayer(punishedPlayer);
		Collection<LoggedPunishment> punishments = loggedPlayer.getLoggedPunishments();

		if (punishments.isEmpty()) {
			admin.sendMessage(ChatColor.RED + "That player has never been punished before.");
			return;
		}

		StringBuilder punishmentsString = new StringBuilder();
		punishments.stream().forEach(punishment -> {
			String border = ChatColor.YELLOW + "-------------------------------------";
			punishmentsString.append(border);
			punishmentsString.append("\n" + ChatColor.BLUE + "Date: " + ChatColor.WHITE + punishment.getDate());
			punishmentsString.append("\n" + ChatColor.BLUE + "Reason: " + ChatColor.WHITE + punishment.getReason());
			punishmentsString.append("\n" + border + "\n");
		});

		String punishedPlayerUsername = Main.getInstance().getNameResolver().getUsername(punishedPlayer.getUniqueId());
		admin.sendMessage(ChatColor.BLUE + "All (" + ChatColor.GOLD + punishments.size() + ChatColor.BLUE + ") punishments on " +
				ChatColor.GOLD + punishedPlayerUsername + ChatColor.BLUE + ":" + punishmentsString);
	}

	@CommandProperties(
			command = "boi details",
			usage = "/boi details <playername>",
			description = "Gets the details of the current punishment inflicted on the player.",
			permission = GeneralCommands.BASE_PERMISSION + ".admin.details"
	)
	public void viewPunishmentDetails(CommandSender admin, OfflinePlayer player) {
		if (player == null) {
			admin.sendMessage(ChatColor.RED + "That player does not exist.");
			return;
		}

		String playerUsername = Main.getInstance().getNameResolver().getUsername(player.getUniqueId());
		PunishedPlayer punishedPlayer = PunishManager.getPunishedPlayer(player);
		if (punishedPlayer == null) {
			admin.sendMessage(ChatColor.RED + playerUsername + " is not currently punished.");
			return;
		}

		StringBuilder punishmentDetails = new StringBuilder();
		String border = ChatColor.YELLOW + "-------------------------------------";
		punishmentDetails.append(border);
		punishmentDetails.append("\n" + ChatColor.BLUE + "Player: " + ChatColor.GOLD + playerUsername);
		punishmentDetails.append("\n" + ChatColor.BLUE + "Reason: " + ChatColor.WHITE + punishedPlayer.getReason());
		String muteDuration = punishedPlayer.isAllowedToSpeak() ? "Unmuted" : Time.epochTimeToDuration(punishedPlayer.getChatSpeakTime()).formatRounded();
		punishmentDetails.append("\n" + ChatColor.BLUE + "Remaining Mute Duration: " + ChatColor.WHITE + muteDuration);
		String voteOpen = punishedPlayer.isVoteOpen() ? "Yes" : Time.epochTimeToDuration(punishedPlayer.getVoteOpenTime()).formatRounded();
		punishmentDetails.append("\n" + ChatColor.BLUE + "Vote Open: " + ChatColor.WHITE + voteOpen);
		punishmentDetails.append("\n" + ChatColor.BLUE + "Required Votes: " + ChatColor.WHITE + punishedPlayer.getRequiredVotes());

		if (punishedPlayer.getCurrentVotes() == 0) {
			punishmentDetails.append("\n" + ChatColor.BLUE + "Current Votes: " + ChatColor.WHITE + "None");
		} else {
			punishmentDetails.append("\n" + ChatColor.BLUE + "Current Votes (" + ChatColor.GOLD + punishedPlayer.getCurrentVotes() + ChatColor.BLUE + "): " + ChatColor.WHITE);

			StringBuilder currentVotes = new StringBuilder();
			punishedPlayer.getVoters().forEach(uuid -> {
				String voterUsername = Main.getInstance().getNameResolver().getUsername(uuid);
				if (currentVotes.length() != 0) {
					currentVotes.append(", ");
				}

				currentVotes.append(voterUsername);
			});

			punishmentDetails.append(currentVotes);
		}

		punishmentDetails.append("\n" + ChatColor.BLUE + "Previous Groups: " + ChatColor.WHITE);
		StringBuilder previousGroups = new StringBuilder();
		for (String group : punishedPlayer.getPreviousGroups()) {
			if (previousGroups.length() != 0) {
				previousGroups.append(", ");
			}

			previousGroups.append(group);
		}
		punishmentDetails.append(previousGroups);

		punishmentDetails.append(border);

		admin.sendMessage(punishmentDetails.toString());
	}

	@CommandProperties(
			command = "boi list",
			usage = "/boi list",
			description = "Lists all the players that are currently punished.",
			permission = GeneralCommands.BASE_PERMISSION + ".admin.list"
	)
	public void listPunished(CommandSender admin) {
		StringBuilder punished = new StringBuilder();
		Collection<PunishedPlayer> punishedPlayers = PunishManager.getPunishedPlayers();
		punishedPlayers.forEach(punishedPlayer -> {
			if (punished.length() != 0) {
				punished.append(", ");
			}

			String username = Main.getInstance().getNameResolver().getUsername(punishedPlayer.getPlayerUUID());
			punished.append(username);
		});

		if (punished.length() == 0) {
			punished.append("None");
		}

		admin.sendMessage(ChatColor.BLUE + "Currently punished players (" + ChatColor.GOLD + punishedPlayers.size() +
				ChatColor.BLUE + "): " + ChatColor.WHITE + punished.toString());
	}

}
