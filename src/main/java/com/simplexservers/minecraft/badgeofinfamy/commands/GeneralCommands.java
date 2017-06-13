package com.simplexservers.minecraft.badgeofinfamy.commands;

import com.simplexservers.minecraft.badgeofinfamy.Main;
import com.simplexservers.minecraft.badgeofinfamy.PlayerSettings;
import com.simplexservers.minecraft.badgeofinfamy.PunishManager;
import com.simplexservers.minecraft.badgeofinfamy.votes.VoteGUI;
import com.simplexservers.minecraft.bukkitutils.commands.BukkitCommandInvoker;
import com.simplexservers.minecraft.bukkitutils.gui.InventoryGUI;
import com.simplexservers.minecraft.commandutils.CommandHandler;
import com.simplexservers.minecraft.commandutils.CommandManager;
import com.simplexservers.minecraft.commandutils.CommandProperties;
import com.simplexservers.minecraft.commandutils.HelpCommandUtil;
import com.simplexservers.minecraft.promptutils.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GeneralCommands implements CommandHandler {

	public static final String BASE_PERMISSION = "badgeofinfamy";

	private CommandManager cmdManager;

	public GeneralCommands(CommandManager cmdManager) {
		this.cmdManager = cmdManager;
	}

	@CommandProperties(
			command = "boi",
			usage = "/boi",
			aliases = { "boi ?", "boi help" },
			description = "Shows the help page for BadgeOfInfamy."
	)
	public void boiHelp(CommandSender sender) {
		sender.sendMessage(HelpCommandUtil.generateSimpleHelp(cmdManager, new BukkitCommandInvoker(sender)));
	}

	@CommandProperties(
			command = "pardon",
			usage = "/pardon",
			description = "Votes to pardon a naughty player.",
			permission = BASE_PERMISSION + ".pardon"
	)
	public void boiVote(Player player) {
		if (PunishManager.getPunishedPlayer(player) != null) {
			player.sendMessage(ChatColor.RED + "You cannot cast a vote since you are currently being restricted yourself.");
			return;
		}

		InventoryGUI gui = VoteGUI.buildVoteGUI(player);

		if (gui == null) {
			player.sendMessage(ChatColor.RED + "No one is currently up for pardon. You'll be notified once a new vote is open.");
			return;
		}

		Main.getInstance().getGUIManager().openGUI(player, gui);
	}

	@CommandProperties(
			command = "pardon notify",
			aliases = {
					"boi toggle vote notify",
					"boi toggle votenotify",
					"boi toggle vote notification",
					"boi toggle vote notifications",
					"boi toggle votenotification",
					"boi toggle votenotifications"
			},
			usage = "/pardon notify",
			description = "Toggles the notification message to pardon players.",
			permission = BASE_PERMISSION + ".settings.votenotify"
	)
	public void boiToggleVoteNotify(Player player) {
		PlayerSettings settings = PlayerSettings.getPlayerSettings(player);
		boolean newState = !settings.showVoteNotification();
		settings.setShowVoteNotification(newState);

		player.sendMessage(ChatColor.GREEN + "Vote notifications toggled to " + ChatColor.WHITE + (newState ? "on" : "off") + ChatColor.GREEN + ".");
	}

	@CommandProperties(
			command = "pardon notify",
			aliases = {
					"boi set vote notify",
					"boi set votenotify",
					"boi set vote notification",
					"boi set vote notifications",
					"boi set votenotification",
					"boi set votenotifications"
			},
			usage = "/pardon notify <on|off>",
			description = "Sets the state of pardon notifications.",
			permission = BASE_PERMISSION + ".settings.votenotify"
	)
	public void boiSetVoteNotify(Player player, boolean state) {
		String stateText = state ? "on" : "off";
		PlayerSettings settings = PlayerSettings.getPlayerSettings(player);

		if (settings.showVoteNotification() == state) {
			player.sendMessage(ChatColor.GREEN + "Vote notifications already set to " + ChatColor.WHITE + stateText + ChatColor.GREEN + ".");
			return;
		}

		settings.setShowVoteNotification(state);
		player.sendMessage(ChatColor.GREEN + "Vote notifications changed to " + ChatColor.WHITE + stateText + ChatColor.GREEN + ".");
	}

}
