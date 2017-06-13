package com.simplexservers.minecraft.badgeofinfamy.listeners;

import com.simplexservers.minecraft.badgeofinfamy.PunishManager;
import com.simplexservers.minecraft.badgeofinfamy.PunishedPlayer;
import com.simplexservers.minecraft.bukkitutils.gui.InventoryGUISelectEvent;
import com.simplexservers.minecraft.promptutils.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

/**
 * Handles the pardon GUI events.
 */
public class VoteListener implements Listener {

	/**
	 * Handles casting a player vote when they make a selection in the GUI.
	 *
	 * @param event The InventoryGUISelectEvent.
	 */
	@EventHandler
	public void onPardonSelect(InventoryGUISelectEvent event) {
		UUID pardonUUID = (UUID) event.getSelectedValue();
		PunishedPlayer punishedPlayer = PunishManager.getPunishedPlayer(pardonUUID);
		if (punishedPlayer != null) {
			PunishManager.playerPardonVote(punishedPlayer, event.getPlayer());
		} else {
			event.getPlayer().sendMessage(ChatColor.GREEN + "That player has just been pardoned.");
		}
	}

}
