package com.simplexservers.minecraft.badgeofinfamy.votes;

import com.simplexservers.minecraft.badgeofinfamy.Main;
import com.simplexservers.minecraft.badgeofinfamy.PunishManager;
import com.simplexservers.minecraft.badgeofinfamy.PunishedPlayer;
import com.simplexservers.minecraft.bukkitutils.gui.InventoryGUI;
import com.simplexservers.minecraft.bukkitutils.gui.InventoryGUIEntry;
import com.simplexservers.minecraft.promptutils.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VoteGUI {

	public static final int LORE_MAX_CHARS_PER_LINE = 40;

	public static InventoryGUI buildVoteGUI(Player player) {
		Collection<PunishedPlayer> punishedPlayers = PunishManager.getPunishedPlayers();
		punishedPlayers.removeIf(p -> !p.isVoteOpen());

		if (punishedPlayers.size() == 0) {
			return null;
		}

		InventoryGUI.InventoryGUIBuilder guiBuilder = new InventoryGUI.InventoryGUIBuilder(Main.getInstance().getGUIManager(), "Up for Pardon");
		punishedPlayers.forEach(punishedPlayer -> guiBuilder.addEntry(getPunishedPlayerEntry(player, punishedPlayer)));
		return guiBuilder.build();
	}

	public static InventoryGUIEntry getPunishedPlayerEntry(Player player, PunishedPlayer punishedPlayer) {
		if (punishedPlayer.getCurrentVotes() >= punishedPlayer.getRequiredVotes()) {
			return null;
		}

		// Build the player item
		ItemStack stack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3); // 3 for player skull type
		SkullMeta meta = (SkullMeta) stack.getItemMeta();

		String username = Main.getInstance().getNameResolver().getUsername(punishedPlayer.getPlayerUUID());

		meta.setOwner(username);
		meta.setDisplayName(username);

		// Lore
		List<String> lore = new ArrayList<>();

		// Lore - already voted
		if (punishedPlayer.hasVoted(player)) {
			lore.add(ChatColor.RED + "You have already voted for " + username);
			lore.add("");
		}

		// Lore - reason
		String reason = ChatColor.RED + username + " was punished for: " + ChatColor.WHITE + punishedPlayer.getReason();
		String[] reasonWords = reason.split(" ");

		int lastWordIndex = 0;
		while (lastWordIndex < reasonWords.length) {
			StringBuilder line = new StringBuilder();

			while (lastWordIndex < reasonWords.length && line.length() < LORE_MAX_CHARS_PER_LINE) {
				if (line.length() > 0) {
					line.append(' ');
				}

				line.append(reasonWords[lastWordIndex++]);
			}

			lore.add(ChatColor.WHITE + line.toString());
		}

		lore.add("");
		lore.add(ChatColor.YELLOW + "Votes: " + punishedPlayer.getCurrentVotes() + " out of " + punishedPlayer.getRequiredVotes());
		lore.add("");
		lore.add(ChatColor.BLUE + "Click to pardon " + ChatColor.WHITE + username);
		meta.setLore(lore);

		stack.setItemMeta(meta);

		// Build the entry
		InventoryGUIEntry entry = new InventoryGUIEntry(stack, punishedPlayer.getPlayerUUID());
		return entry;
	}

}
