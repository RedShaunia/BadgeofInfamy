package com.simplexservers.minecraft.badgeofinfamy.prompts;

import com.simplexservers.minecraft.promptutils.*;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.simplexservers.minecraft.badgeofinfamy.PunishedPlayer.PunishedPlayerBuilder;
import com.simplexservers.minecraft.bukkitutils.commands.BukkitCommandInvoker;
import com.simplexservers.minecraft.commandutils.CommandInvoker;
import com.simplexservers.minecraft.promptutils.prompts.TimePrompt;

public class PunishPrompt extends Prompt {

	private PunishedPlayerBuilder playerBuilder;
	private ChatListener listener;
	private PunishPromptCallback callback;

	public PunishPrompt(Player punisher, OfflinePlayer player, ChatListener listener, PunishPromptCallback callback) {
		super(new BukkitCommandInvoker(punisher));
		this.playerBuilder = new PunishedPlayerBuilder(player);
		this.listener = listener;
		this.callback = callback;
	}

	@Override
	public void begin() {
		new DescriptionPrompt(getParticipant(), listener).begin();
	}

	public interface PunishPromptCallback {
		void promptComplete(PunishedPlayerBuilder builder);
	}

	private class DescriptionPrompt extends AnswerPrompt {

		public DescriptionPrompt(CommandInvoker participant, ChatListener listener) {
			super(participant, listener);
		}

		@Override
		public String getMessage() {
			return ChatColor.BLUE + "Please enter the reason the player is being punished.";
		}

		@Override
		public void onInput(String input) {
			playerBuilder.setReason(input);
			new ChatMuteDelay(this).begin();
		}

	}

	private class ChatMuteDelay extends TimePrompt {

		public ChatMuteDelay(AnswerPrompt previousPrompt) {
			super(previousPrompt);
		}

		@Override
		public String getMessage() {
			return ChatColor.BLUE + "Please enter the amount of time the player should be muted.\n" + getFormatHelp();
		}

		@Override
		public void onParsedInput(Time input) {
			playerBuilder.setChatMuteDuration(input);
			new VoteOpenDelay(this).begin();
		}

	}

	private class VoteOpenDelay extends TimePrompt {

		public VoteOpenDelay(AnswerPrompt previousPrompt) {
			super(previousPrompt);
		}

		@Override
		public String getMessage() {
			return ChatColor.BLUE + "Please enter the amount of time until the pardon vote opens.\n" + getFormatHelp();
		}

		@Override
		public void onParsedInput(Time input) {
			playerBuilder.setVoteWaitDuration(input);
			new VotePardonPercentage(this).begin();
		}

	}

	private class VotePardonPercentage extends ParsedPrompt<Integer> {

		public VotePardonPercentage(AnswerPrompt previousPrompt) {
			super(previousPrompt, input -> {
				int value = Integer.parseInt(input);
				if (value <= 0) {
					throw new IllegalArgumentException("Value must be greater than 0");
				}
				return value;
			});
		}

		@Override
		public String getMessage() {
			return ChatColor.BLUE + "Please enter the requires number of votes required for the player to be pardoned.";
		}

		@Override
		public void onParsedInput(Integer val) {
			playerBuilder.setRequiredVotePercentage(val);
			callback.promptComplete(playerBuilder);
		}

	}

}
