package com.simplexservers.minecraft.badgeofinfamy.config;

import com.simplexservers.minecraft.badgeofinfamy.Main;
import com.simplexservers.minecraft.fileutils.FileUtils;
import com.simplexservers.minecraft.fileutils.configuration.YAMLConfiguration;
import com.simplexservers.minecraft.promptutils.Time;

import java.io.File;
import java.io.IOException;

public class Config {

	private static final File CONFIG_FILE = new File(Main.getInstance().getDataFolder(), "config.yml");

	private YAMLConfiguration config;

	public final String PUNISHED_GROUP;
	public final long VOTE_NOTIFICATION_DURATION;
	public final long VOTE_NOTIFICATION_INTERVAL;

	public Config() throws IOException {
		if (!CONFIG_FILE.exists()) {
			CONFIG_FILE.getParentFile().mkdirs();
			FileUtils.saveResource("config.yml", CONFIG_FILE);
		}

		config = new YAMLConfiguration(CONFIG_FILE);

		// Get the config settings
		PUNISHED_GROUP = config.getString("PunishGroup");
		VOTE_NOTIFICATION_DURATION = Time.parseTime(config.getString("VoteNotifications.Duration")).getSeconds();
		VOTE_NOTIFICATION_INTERVAL = Time.parseTime(config.getString("VoteNotifications.Interval")).getSeconds();
	}

}
