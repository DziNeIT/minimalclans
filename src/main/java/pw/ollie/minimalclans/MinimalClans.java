package pw.ollie.minimalclans;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import pw.ollie.minimalclans.clan.ClanManager;

public final class MinimalClans extends JavaPlugin {
	private ClanManager clanManager;

	// Files / config
	private File configFile;
	private YamlConfiguration config;
	private File clansDirectory;

	@Override
	public void onEnable() {
		clanManager = new ClanManager(this);

		getCommand("clan").setExecutor(new MCCommands(this));
		getServer().getPluginManager().registerEvents(new MCListener(this),
				this);

		configFile = new File(getDataFolder(), "config.yml");
		config = YamlConfiguration.loadConfiguration(configFile);
		clansDirectory = new File(getDataFolder(), "clans");

		if (!clansDirectory.exists()) {
			clansDirectory.mkdirs();
		}

		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
			} catch (IOException e) {
			}
		}

		try {
			clanManager.loadClans();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		if (clanManager != null) {
			try {
				clanManager.saveClans();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public ClanManager getClanManager() {
		return clanManager;
	}

	public YamlConfiguration getConfig() {
		return config;
	}

	public File getClansDirectory() {
		return clansDirectory;
	}
}
