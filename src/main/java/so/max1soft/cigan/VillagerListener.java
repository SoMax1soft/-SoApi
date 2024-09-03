package so.max1soft.cigan;

import com.earth2me.essentials.Essentials;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import so.max1soft.Main;
import com.sk89q.worldguard.WorldGuard;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VillagerListener implements Listener {
    private final Main plugin;
    private final FileConfiguration config;
    private final Object lock = new Object();
    private final Essentials essentials;
    private final Map<UUID, Long> villagerCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> rewardGiven = new ConcurrentHashMap<>();


    private final long interactionCooldown = 3000; // 3 seconds
    private final Map<UUID, Boolean> playerCoinStatus = new ConcurrentHashMap<>();
    private volatile boolean isCoinTaken = false;
    private final Map<UUID, Boolean> dropMessageSent = new ConcurrentHashMap<>();

    private VillagerSpawnTask villagerSpawnTask;
    private Villager villager;

    public VillagerListener(Main plugin, Essentials essentials, VillagerSpawnTask villagerSpawnTask) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.essentials = essentials;
        this.villagerSpawnTask = villagerSpawnTask;
    }
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Villager) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractVillager(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager) {
            villager = (Villager) event.getRightClicked();
            Player player = event.getPlayer();
            UUID playerUUID = player.getUniqueId();

            if (villager.getCustomName() != null && villager.getCustomName().contains(config.getString("CiganVillager.Name"))) {
                event.setCancelled(true);

                synchronized (lock) {
                    if (villagerCooldowns.containsKey(playerUUID)) {
                        long lastInteraction = villagerCooldowns.get(playerUUID);
                        if (System.currentTimeMillis() - lastInteraction < interactionCooldown) {
                            player.sendMessage("§cПожалуйста, подождите перед повторным взаимодействием с жителем.");
                            return;
                        }
                    }
                    villagerCooldowns.put(playerUUID, System.currentTimeMillis());

                    if (isCoinTaken) {
                        player.sendMessage("§cМонетку уже кто-то забрал!");
                        return;
                    }

                    if (playerCoinStatus.getOrDefault(playerUUID, false)) {
                        player.sendMessage("§cВы уже взяли монетку! Верните её, чтобы взять новую.");
                        return;
                    }

                    isCoinTaken = true;

                    ItemStack emerald = new ItemStack(Material.EMERALD);
                    ItemMeta meta = emerald.getItemMeta();
                    meta.setDisplayName(config.getString("CiganVillager.coinname"));
                    meta.setLore(Collections.singletonList(ChatColor.GRAY + "Я в тбилиси воровал не мало!"));
                    emerald.setItemMeta(meta);
                    player.getInventory().addItem(emerald);
                    playerCoinStatus.put(playerUUID, true);

                    sendMessageToNearbyPlayers(villager.getLocation(), ChatColor.translateAlternateColorCodes('&', config.getString("CiganMessages.CoinTaken").replace("%player%", player.getName())));
                    villager.getEquipment().setItemInMainHand(null);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            synchronized (lock) {
                                if (villagerSpawnTask.enabled) {
                                    if (player.isOnline() && player.getInventory().contains(emerald)) {
                                        if (player.getLocation().distance(villager.getLocation()) > config.getInt("CiganVillager.Distance")) {
                                            sendMessageToNearbyPlayers(villager.getLocation(), ChatColor.translateAlternateColorCodes('&', config.getString("CiganMessages.TooFar").replace("%player%", player.getName())));
                                            player.getInventory().remove(emerald);
                                            villager.getEquipment().setItemInMainHand(emerald);
                                            playerCoinStatus.put(playerUUID, false);
                                            isCoinTaken = false;
                                            this.cancel();
                                        } else {
                                            if (!rewardGiven.getOrDefault(playerUUID, false)) {
                                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), config.getString("CiganVillager.RewardCommand").replace("%player%", player.getName()));
                                                rewardGiven.put(playerUUID, true);
                                                Bukkit.getScheduler().runTaskLater(plugin, () -> rewardGiven.put(playerUUID, false), 20L); // 20L = 1 секунда
                                            }
                                        }
                                    } else {
                                        sendMessageToNearbyPlayers(villager.getLocation(), ChatColor.translateAlternateColorCodes('&', config.getString("CiganMessages.State").replace("%player%", player.getName()).replace("%state%", "выкинул монетку")));
                                        player.getInventory().remove(emerald);
                                        villager.getEquipment().setItemInMainHand(emerald);
                                        playerCoinStatus.put(playerUUID, false);
                                        isCoinTaken = false;
                                        this.cancel();
                                    }
                                } else {
                                    if (!player.isOnline() || !player.getInventory().contains(emerald)) {
                                        sendMessageToNearbyPlayers(villager.getLocation(), ChatColor.translateAlternateColorCodes('&', config.getString("CiganMessages.State").replace("%player%", player.getName()).replace("%state%", "выкинул монетку")));
                                        player.getInventory().remove(emerald);
                                        villager.getEquipment().setItemInMainHand(emerald);
                                        playerCoinStatus.put(playerUUID, false);
                                        isCoinTaken = false;
                                        this.cancel();
                                    }
                                }
                            }
                        }
                    }.runTaskTimer(plugin, 0, 20L);
                }
            }
        }
    }
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().equals(config.getString("CiganVillager.coinname"))) {
            event.getItemDrop().remove();
            player.getInventory().remove(item);
            synchronized (lock) {
                if (playerCoinStatus.getOrDefault(playerUUID, false)) {
                    if (villager != null && villager.getEquipment() != null) {
                        villager.getEquipment().setItemInMainHand(item);
                    } else {
                        plugin.getLogger().warning("В руках у жителя NULL.");
                    }

                    if (!dropMessageSent.getOrDefault(playerUUID, false)) {
                        sendMessageToNearbyPlayers(player.getLocation(), ChatColor.translateAlternateColorCodes('&', config.getString("CiganMessages.State")
                                .replace("%player%", player.getName())
                                .replace("%state%", "выкинул монетку")));
                        dropMessageSent.put(playerUUID, true);
                    }

                    playerCoinStatus.put(playerUUID, false);
                    isCoinTaken = false;

                    new CooldownTask(playerUUID).runTaskLater(plugin, 60L);
                }
            }
        }
    }




    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player == null) {
            plugin.getLogger().warning("Player in onPlayerDeath is null.");
            return;
        }
        UUID playerUUID = player.getUniqueId();
        ItemStack emerald = null;

        for (ItemStack item : event.getDrops()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equalsIgnoreCase(config.getString("CiganVillager.coinname"))) {
                    emerald = item;
                    break;
                }
            }
        }

        if (emerald != null) {
            event.getDrops().remove(emerald);
            player.getInventory().remove(emerald);

            sendMessageToNearbyPlayers(player.getLocation(), ChatColor.translateAlternateColorCodes('&',
                    config.getString("CiganMessages.State").replace("%player%", player.getName()).replace("%state%", "умер")));
            playerCoinStatus.put(playerUUID, false);
            isCoinTaken = false;
            if (villager != null) {
                if (villager.getEquipment() != null) {
                    villager.getEquipment().setItemInMainHand(emerald);
                } else {
                    plugin.getLogger().warning("Villager's equipment is null when trying to set item in main hand.");
                }
            } else {
                plugin.getLogger().warning("Villager is null when trying to set item in main hand.");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            Bukkit.getLogger().severe("PlayerQuitEvent triggered but player is null");
            return;
        }
        UUID playerUUID = player.getUniqueId();
        ItemStack emerald = null;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equalsIgnoreCase(config.getString("CiganVillager.coinname"))) {
                    emerald = item;
                    break;
                }
            }
        }
        if (emerald != null) {
            player.getInventory().remove(emerald);
            sendMessageToNearbyPlayers(player.getLocation(), ChatColor.translateAlternateColorCodes('&',
                    config.getString("CiganMessages.State").replace("%player%", player.getName()).replace("%state%", "ливнул с позором")));
            playerCoinStatus.put(playerUUID, false);
            isCoinTaken = false;
            if (villager != null) {
                if (villager.getEquipment() != null) {
                    villager.getEquipment().setItemInMainHand(emerald);
                } else {
                    Bukkit.getLogger().severe("Villager equipment is null");
                }
            } else {
                Bukkit.getLogger().severe("Villager is null");
            }
        } else {
            Bukkit.getLogger().info("No emerald found in player's inventory");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (isPlayerInYourRegion(player.getLocation())) {
            if (!player.hasPermission(config.getString("CiganBypass.Permission"))) {
                essentials.getUser(player).setGodModeEnabled(false);
                essentials.getUser(player).getBase().setFlying(false);
                essentials.getUser(player).setVanished(false);
                PotionEffect glowing = new PotionEffect(PotionEffectType.GLOWING, 100, 1);
                player.addPotionEffect(glowing);
                player.setGameMode(GameMode.SURVIVAL);
            }
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        ItemStack emerald = null;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equalsIgnoreCase(config.getString("CiganVillager.coinname"))) {
                    emerald = item;
                    break;
                }
            }
        }
        if (emerald != null) {
            player.getInventory().remove(emerald);
            sendMessageToNearbyPlayers(player.getLocation(), ChatColor.translateAlternateColorCodes('&',
                    config.getString("CiganMessages.State").replace("%player%", player.getName()).replace("%state%", "сменил мир и потерял монетку")));
            playerCoinStatus.put(playerUUID, false);
            isCoinTaken = false;
        }
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        Entity entered = event.getEntered();
        if (entered instanceof Villager && event.getVehicle() instanceof Boat) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getCaught() instanceof Villager) {
            event.setCancelled(true);
        }
    }

    private boolean isPlayerInYourRegion(Location location) {
        String chestRegionName = config.getString("CiganVillager.regioname");
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
        if (regionManager != null) {
            for (ProtectedRegion region : regionManager.getApplicableRegions(BlockVector3.at(location.getX(),location.getY(),location.getZ()))){
                if (region.getId().equalsIgnoreCase(chestRegionName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void sendMessageToNearbyPlayers(Location location, String message) {
        int radius = 100; // Радиус в блоках
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) <= radius) {
                player.sendMessage(message);
            }
        }
    }

    private class CooldownTask extends BukkitRunnable {
        private final UUID playerUUID;

        public CooldownTask(UUID playerUUID) {
            this.playerUUID = playerUUID;
        }

        @Override
        public void run() {
            villagerCooldowns.put(playerUUID, System.currentTimeMillis());
        }
    }
}
