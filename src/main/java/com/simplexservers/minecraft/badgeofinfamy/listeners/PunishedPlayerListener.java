package com.simplexservers.minecraft.badgeofinfamy.listeners;

import com.simplexservers.minecraft.badgeofinfamy.PunishManager;
import com.simplexservers.minecraft.badgeofinfamy.PunishedPlayer;
import com.simplexservers.minecraft.promptutils.ChatColor;
import com.simplexservers.minecraft.promptutils.Time;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Bukkit listener to impose restrictions on punished players.
 */
public class PunishedPlayerListener implements Listener {

	/**
	 * Handles blocking the player from chat while they're muted under punishment.
	 *
	 * @param event The Bukkit chat event.
	 */
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		PunishedPlayer punishedPlayer = PunishManager.getPunishedPlayer(event.getPlayer());

		if (punishedPlayer == null) {
			// The player is not being punished
			return;
		}

		if (!punishedPlayer.isAllowedToSpeak()) {
			event.setCancelled(true);

			Time duration = Time.epochTimeToDuration(punishedPlayer.getChatSpeakTime());
			event.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to speak in chat for " +
					ChatColor.WHITE + duration.formatRounded() + ChatColor.RED + ".");
		}
	}

}
