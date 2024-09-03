package so.max1soft.cigan;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import so.max1soft.Main;

import java.util.HashSet;
import java.util.Set;

public class BossBarManager implements Listener {
    private final Main plugin;
    private final BossBar bossBar;
    private final Set<Player> players;
    private boolean eventActive;

    public BossBarManager(Main plugin) {
        this.plugin = plugin;
        this.bossBar = Bukkit.createBossBar("Цыган", BarColor.GREEN, BarStyle.SOLID);
        this.players = new HashSet<>();
        this.eventActive = false;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void startBossBarCountdown(Location loc, VillagerSpawnTask villagerSpawnTask) {
        bossBar.setVisible(true);
        eventActive = true;
        plugin.getLogger().info("Ивент запустился!");
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
            players.add(player);
        }
        new BukkitRunnable() {
            int countdown = plugin.getConfig().getInt("CiganVillager.event-duration", 18000);

            public void removeVillagers() {
                for (Villager villager : loc.getWorld().getEntitiesByClass(Villager.class)) {
                    if (villager.getCustomName() != null && villager.getCustomName().contains(plugin.getConfig().getString("CiganVillager.Name"))) {
                        villager.remove();
                    }
                }
            }

            @Override
            public void run() {
                if (countdown <= 0) {
                    bossBar.setVisible(false);
                    eventActive = false;
                    removeVillagers();
                    MessageUtil.sendAll(plugin.getConfig().getStringList("CiganMessages.Stopevent"));
                    for (Player player : players) {
                        bossBar.removePlayer(player);
                    }
                    players.clear();
                    this.cancel();
                    villagerSpawnTask.enabled = false;
                } else {
                    bossBar.setStyle(BarStyle.SEGMENTED_12);
                    bossBar.setColor(BarColor.RED);
                    bossBar.setTitle("§e§l⛂ §6§lЦыган §f- Спрятался тут:§6 " + "X: " + loc.getX() + " Y: " + loc.getY() + " Z: " + loc.getZ() + " §e§l⛂");
                    bossBar.setProgress((double) countdown / plugin.getConfig().getInt("CiganVillager.event-duration", 18000));
                    countdown--;
                }
            }
        }.runTaskTimer(plugin, 0, 20L);
    }

    public void hideBossBar() {
        bossBar.setVisible(false);
        for (Player player : players) {
            bossBar.removePlayer(player);
        }
        players.clear();
        eventActive = false;
    }

    public boolean isEventActive() {
        return eventActive;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (eventActive) {
            bossBar.addPlayer(player);
            players.add(player);
        }
    }
}
