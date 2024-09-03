package so.max1soft.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WhiteList implements Listener {

    private FileConfiguration whitelistConfig;
    private File whitelistFile;
    private List<String> whitelist;
    private boolean whitelistEnabled;
    private String deniedMessage;
    private final JavaPlugin plugin;

    public WhiteList(JavaPlugin plugin) {
        this.plugin = plugin;
        this.whitelist = new ArrayList<>();
        this.whitelistEnabled = false;
        this.deniedMessage = plugin.getConfig().getString("Utils-Message.kick-whitelist").replace("&", "§");

        whitelistFile = new File(plugin.getDataFolder(), "whitelist.yml");
        if (!whitelistFile.exists()) {
            plugin.saveResource("whitelist.yml", false);
        }
        whitelistConfig = YamlConfiguration.loadConfiguration(whitelistFile);

        updateWhitelist(); // Load whitelist from config file initially
        whitelistEnabled = plugin.getConfig().getBoolean("Utils.whitelist-enabled", false);
    }

    private void updateWhitelist() {
        whitelist = whitelistConfig.getStringList("whitelist");
    }

    public void addPlayerToWhitelist(String playerName) {
        whitelist.add(playerName.toLowerCase());
        saveWhitelist();
        updateWhitelist(); // Update in-memory whitelist after modification
    }

    public void removePlayerFromWhitelist(String playerName) {
        whitelist.remove(playerName.toLowerCase());
        saveWhitelist();
        updateWhitelist(); // Update in-memory whitelist after modification
    }

    private void saveWhitelist() {
        whitelistConfig.set("whitelist", whitelist);
        try {
            whitelistConfig.save(whitelistFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save whitelist to " + whitelistFile.getName());
        }
    }

    public void setWhitelistEnabled(boolean enabled) {
        this.whitelistEnabled = enabled;
        plugin.getConfig().set("Utils.whitelist-enabled", enabled);
        plugin.saveConfig();
    }

    public boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }

    public boolean isPlayerWhitelisted(String playerName) {
        return whitelist.contains(playerName.toLowerCase());
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (whitelistEnabled && !isPlayerWhitelisted(event.getPlayer().getName())) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, deniedMessage);
        }
    }

    // Method to reload the whitelist from file
    public void reloadWhitelist() {
        whitelistConfig = YamlConfiguration.loadConfiguration(whitelistFile);
        updateWhitelist(); // Reload in-memory whitelist
    }
}
