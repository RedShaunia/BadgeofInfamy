package com.simplexservers.minecraft.badgeofinfamy;

import com.simplexservers.minecraft.badgeofinfamy.commands.AdminCommands;
import com.simplexservers.minecraft.badgeofinfamy.commands.GeneralCommands;
import com.simplexservers.minecraft.badgeofinfamy.config.Config;
import com.simplexservers.minecraft.badgeofinfamy.listeners.PunishedPlayerListener;
import com.simplexservers.minecraft.badgeofinfamy.listeners.VoteListener;
import com.simplexservers.minecraft.badgeofinfamy.votes.VoteNotificationTimer;
import com.simplexservers.minecraft.bukkitutils.commands.BukkitCommandManager;
import com.simplexservers.minecraft.bukkitutils.gui.InventoryGUIListener;
import com.simplexservers.minecraft.bukkitutils.gui.InventoryGUIManager;
import com.simplexservers.minecraft.bukkitutils.players.NameCache;
import com.simplexservers.minecraft.bukkitutils.players.NameResolver;
import com.simplexservers.minecraft.bukkitutils.prompts.PromptListener;
import com.simplexservers.minecraft.commandutils.ParameterType;
import com.simplexservers.minecraft.fileutils.db.SQLiteDBConnection;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * The entry point for the plugin.
 */
public class Main extends JavaPlugin {

	/**
	 * The singleton instance of the plugin loaded by Bukkit.
	 */
	private static Main instance = null;
	/**
	 * The plugin configuration.
	 */
	private Config config;
	/**
	 * The hook into the Permisisons management system.
	 */
	private Permission permissionsHook = null;
	/**
	 * The cache of Player data.
	 */
	private NameResolver nameResolver = new NameResolver(new NameCache(this, new SQLiteDBConnection(new File(getDataFolder(), "playercache.db"))));
	/**
	 * The listener handling prompt input.
	 */
	private PromptListener promptListener;
	/**
	 * The manager to handle inventory GUI's.
	 */
	private InventoryGUIManager guiManager = new InventoryGUIManager();

	@Override
	public void onEnable() {
		instance = this;

		// Load the plugin config
		try {
			config = new Config();
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Could not load the plugin config.", e);
			Bukkit.getPluginManager().disablePlugin(this);
		}

		// Get the permissions hook
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		permissionsHook = rsp.getProvider();
		if (permissionsHook == null) {
			Bukkit.getLogger().severe("Could not hook into permissions system. Is a Vault supported permissions plugin installed?");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		if (!permissionsHook.hasGroupSupport()) {
			Bukkit.getLogger().severe("The permissions system installed does not support groups.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		// Initialize the NameResolver's cache
		nameResolver.initializeCache();

		// Register the InventoryGUI listener
		Bukkit.getPluginManager().registerEvents(new InventoryGUIListener(this, guiManager), this);
		// Register the PromptListener
		Bukkit.getPluginManager().registerEvents(promptListener = new PromptListener(), this);
		// Register the punish restrictions listener
		Bukkit.getPluginManager().registerEvents(new PunishedPlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new VoteListener(), this);

		// COMMANDS

		// Register the OfflinePlayer type with our NameResolver instance.
		ParameterType.registerParameterType(OfflinePlayer.class, nameResolver::getOfflinePlayer);

		// Register the commands
		BukkitCommandManager cmdManager = new BukkitCommandManager(this);
		cmdManager.registerHandler(new GeneralCommands(cmdManager));
		cmdManager.registerHandler(new AdminCommands(cmdManager));

		// Load the cached players
		PunishManager.loadCachedPlayers();
		// Load player settings
		PlayerSettings.loadPlayerSettings();

		// Start the vote notification interval
		VoteNotificationTimer.scheduleNextNotification();
	}

	@Override
	public void onDisable() {
		// Close the NameResolver's cache
		nameResolver.closeCache();
	}

	/**
	 * Gets the singleton instance of the plugin that's loaded by Bukkit or null if one is not loaded.
	 *
	 * @return The instance of the plugin loaded by Bukkit.
	 */
	public static Main getInstance() {
		return instance;
	}

	/**
	 * Gets the configuration for the plugin.
	 *
	 * @return The plugin's config.
	 */
	public Config getPluginConfig() {
		return config;
	}

	/**
	 * Gets the NameResolver for the plugin.
	 *
	 * @return The resolver for player names associated with the plugin.
	 */
	public NameResolver getNameResolver() {
		return nameResolver;
	}

	/**
	 * Gets the InventoryGUIManager for the plugin.
	 *
	 * @return The manager to handle inventory based GUI's.
	 */
	public InventoryGUIManager getGUIManager() {
		return guiManager;
	}

	/**
	 * Gets the PlayerListener handling prompt input.
	 *
	 * @return The PlayerListener for the plugin.
	 */
	public PromptListener getPromptListener() {
		return promptListener;
	}

	/**
	 * Gets the hook into the Permissions management system.
	 *
	 * @return The Permission hook.
	 */
	public Permission getPermissionsHook() {
		return permissionsHook;
	}

}
