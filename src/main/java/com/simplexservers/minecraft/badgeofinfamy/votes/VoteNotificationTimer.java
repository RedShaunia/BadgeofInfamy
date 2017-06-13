package com.simplexservers.minecraft.badgeofinfamy.votes;

import com.simplexservers.minecraft.badgeofinfamy.Main;
import com.simplexservers.minecraft.badgeofinfamy.PlayerSettings;
import com.simplexservers.minecraft.badgeofinfamy.PunishManager;
import com.simplexservers.minecraft.badgeofinfamy.PunishedPlayer;
import com.simplexservers.minecraft.bukkitutils.nms.PlayerUtils;
import com.simplexservers.minecraft.promptutils.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class VoteNotificationTimer {

	/**
	 * The epoch time stamp for the next notification. -1 if one is not scheduled.
	 */
	private static long nextNotificationTime = -1;
	/**
	 * The task to send the notification.
	 */
	private static BukkitTask notificationTask = null;

	/**
	 * The task to run when a notification should be sent.
	 */
	private static Runnable notificationRunnable = () -> {
		// Check if there are still open votes
		if (!hasOpenVotes()) {
			return;
		}

		// Notify the online players
		String message = ChatColor.YELLOW + "There are open votes to pardon " + Main.getInstance().getPluginConfig().PUNISHED_GROUP +
				"s! Vote now with " + ChatColor.WHITE + "/pardon";
		Bukkit.getOnlinePlayers().parallelStream()
				.filter(player -> PunishManager.getPunishedPlayer(player) == null)
				.filter(player -> PlayerSettings.getPlayerSettings(player).showVoteNotification())
				.forEach(player -> {
					try {
						PlayerUtils.sendActionBarMessage(Main.getInstance(), player, message, Main.getInstance().getPluginConfig().VOTE_NOTIFICATION_DURATION * 20L);
					} catch (Exception e) {
						player.sendMessage(message);
					}
				});

		// Reset the notification task
		cancelNotification();

		// Schedule the next notification
		scheduleNextNotification();
	};

	/**
	 * Cancels/unschedules the notification task from running.
	 */
	public static void cancelNotification() {
		if (notificationTask != null) {
			notificationTask.cancel();
		}

		notificationTask = null;
		nextNotificationTime = -1;
	}

	/**
	 * Polls the upcoming votes and schedules the nearest one to run. Will cancel the previous notification if the next
	 * vote time is closer.
	 */
	public static void scheduleNextNotification() {
		long nextNotificationTime = getNextNotificationTime();

		if (nextNotificationTime != -1) {
			updateNotificationTime(nextNotificationTime);
		}
	}

	/**
	 * Updates the time for the next notification to run.
	 *
	 * @param nextNotificationTime The epoch time stamp for when to run the notification runnable next.
	 */
	public static void updateNotificationTime(long nextNotificationTime) {
		cancelNotification();

		if (nextNotificationTime <= System.currentTimeMillis() / 1000L) {
			// Send the notification now
			notificationRunnable.run();
			return;
		}

		VoteNotificationTimer.nextNotificationTime = nextNotificationTime;
		long secondsFromNow = nextNotificationTime - System.currentTimeMillis() / 1000L;
		notificationTask = new BukkitRunnable() {
			@Override
			public void run() {
				notificationRunnable.run();
			}
		}.runTaskLater(Main.getInstance(), secondsFromNow * 20L);
	}

	/**
	 * Gets the epoch time stamp for when the next notification should be run.
	 *
	 * @return The time stamp for the next notification.
	 */
	public static long getNextNotificationTime() {
		long nextVoteOpenTime = getNextVoteOpenTime();
		if (hasOpenVotes() || nextVoteOpenTime != -1) { // Has notification pending
			long intervalNotificationTime = System.currentTimeMillis() / 1000L + Main.getInstance().getPluginConfig().VOTE_NOTIFICATION_INTERVAL;

			if (nextVoteOpenTime == -1) {
				return intervalNotificationTime;
			}

			return Math.min(nextVoteOpenTime, intervalNotificationTime);
		}

		return -1;
	}

	public static boolean hasOpenVotes() {
		for (PunishedPlayer player : PunishManager.getPunishedPlayers()) {
			if (player.isVoteOpen()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Gets the epoch time stamp for the next time a vote is opened.
	 *
	 * @return The next epoch time stamp for a new vote.
	 */
	public static long getNextVoteOpenTime() {
		long currentTime = System.currentTimeMillis() / 1000L;
		long nextVoteOpenTime = -1;
		for (PunishedPlayer player : PunishManager.getPunishedPlayers()) {
			if (currentTime < player.getVoteOpenTime() && (nextVoteOpenTime == -1 || nextVoteOpenTime > player.getVoteOpenTime())) {
				nextVoteOpenTime = player.getVoteOpenTime();
			}
		}

		return nextVoteOpenTime;
	}

}
