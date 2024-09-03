package so.max1soft.chestevent;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ChestEvent {
    private JavaPlugin plugin;
    private Location chestLocation;
    private long chestUpdateInterval;
    private String pluginPrefix;
    private String chestUpdateNotification;
    private Hologram hologram;
    private String chestOpenNotification;
    private Map<String, ItemStack> itemsMap = new HashMap<>();
    private Essentials essentials;

    public ChestEvent(JavaPlugin plugin, Essentials essentials) {
        this.plugin = plugin;
        this.essentials = essentials;
        loadConfigValues();
        loadItemsFromConfig();
        startChestEvent();
        startFlightAndGodModeDisabler();



    }

    private void loadConfigValues() {
        chestLocation = new Location(Bukkit.getWorld(plugin.getConfig().getString("chesteventchestLocation.world", "world")),
                plugin.getConfig().getDouble("chesteventchestLocation.x", 0),
                plugin.getConfig().getDouble("chesteventchestLocation.y", 0),
                plugin.getConfig().getDouble("chesteventchestLocation.z", 0));

        chestUpdateInterval = plugin.getConfig().getLong("chesteventchestUpdateInterval", 30) * 60 * 20;
        pluginPrefix = plugin.getConfig().getString("chesteventpluginPrefix", ChatColor.GOLD + "§x§F§F§8§6§0§0§lС§x§F§F§9§7§0§0§lу§x§F§F§A§7§0§0§lн§x§F§F§B§8§0§0§lд§x§F§F§C§8§0§0§lу§x§F§F§D§9§0§0§lк §x§F§F§B§D§0§0§lГ§x§F§F§B§0§0§0§lр§x§F§F§A§2§0§0§lа§x§F§F§9§4§0§0§lф§x§F§F§8§6§0§0§lа ");
        chestUpdateNotification = plugin.getConfig().getString("chesteventmessages.chestUpdateNotification", ChatColor.YELLOW + "§fЛут §6графа обновится§f через §a5 минут!§7 (/warp pvp)");
        chestOpenNotification = plugin.getConfig().getString("chesteventmessages.chestOpenNotification", ChatColor.GREEN + "§fСундук §6графа §a§lоткрыт! §7(/warp pvp)");
    }

    private void startChestEvent() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateChestContents();
            }
        }.runTaskTimer(plugin, 0, chestUpdateInterval);
    }

    private void updateChestContents() {
        if (chestLocation.getBlock().getType() == Material.CHEST || chestLocation.getBlock().getType() == Material.ENDER_CHEST) {
            Inventory chestInventory = ((Chest) chestLocation.getBlock().getState()).getInventory();
            chestInventory.clear();

            Random random = new Random();

            itemsMap.forEach((itemId, itemStack) -> {
                int chance = plugin.getConfig().getInt("items." + itemId + ".chance", 0);
                int amount = plugin.getConfig().getInt("items." + itemId + ".amount", 1);
                for (int i = 0; i < amount; i++) {
                    if (random.nextInt(100) < chance) {
                        chestInventory.addItem(itemStack.clone());
                    }
                }
            });

            updateHologram();
            notifyPlayers();
        }
    }

    private void loadItemsFromConfig() {
        itemsMap.clear();
        if (plugin.getConfig().contains("items")) {
            Set<String> itemIds = plugin.getConfig().getConfigurationSection("items").getKeys(false);
            for (String itemId : itemIds) {
                String materialName = plugin.getConfig().getString("items." + itemId + ".material");
                Material material = Material.matchMaterial(materialName);
                if (material != null) {
                    ItemStack itemStack = new ItemStack(material);

                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta != null) {
                        if (plugin.getConfig().contains("items." + itemId + ".name")) {
                            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("items." + itemId + ".name")));
                        }
                        if (plugin.getConfig().contains("items." + itemId + ".lore")) {
                            List<String> lore = new ArrayList<>();
                            for (String line : plugin.getConfig().getStringList("items." + itemId + ".lore")) {
                                lore.add(ChatColor.translateAlternateColorCodes('&', line));
                            }
                            meta.setLore(lore);
                        }
                        if (plugin.getConfig().contains("items." + itemId + ".enchants")) {
                            for (String enchantName : plugin.getConfig().getConfigurationSection("items." + itemId + ".enchants").getKeys(false)) {
                                Enchantment enchantment = Enchantment.getByName(enchantName);

                                if (enchantment != null) {
                                    int level = plugin.getConfig().getInt("items." + itemId + ".enchants." + enchantName);
                                    meta.addEnchant(enchantment, level, true);
                                }
                            }
                        }
                        if (meta instanceof PotionMeta && plugin.getConfig().contains("items." + itemId + ".potion_type")) {
                            PotionMeta potionMeta = (PotionMeta) meta;
                            PotionType potionType = PotionType.valueOf(plugin.getConfig().getString("items." + itemId + ".potion_type"));
                            int potionLevel = plugin.getConfig().getInt("items." + itemId + ".potion_level", 1);
                            PotionData potionData = new PotionData(potionType, false, potionLevel > 1);
                            potionMeta.setBasePotionData(potionData);
                        }

                        itemStack.setItemMeta(meta);
                    }

                    itemsMap.put(itemId, itemStack);
                }
            }
        }
    }

    public void deleteHologram() {
        if (hologram != null) {
            hologram.delete();
        }
    }

    private void updateHologram() {
        if (hologram != null) {
            hologram.delete();
        }
        Location hologramLocation = chestLocation.clone().add(0.0, 1.5, 0.0);
        hologram = DHAPI.createHologram("chesteventchest_hologram", hologramLocation);
        DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("chesteventhologramname")));
        DHAPI.addHologramLine(hologram, ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("chesteventhologramsub")));

        new BukkitRunnable() {
            int countdown = (int) chestUpdateInterval / 20;

            @Override
            public void run() {
                if (countdown > 0) {
                    countdown--;
                    DHAPI.setHologramLine(hologram, 1, ChatColor.GREEN + "§fЛут §6обновится §aчерез " + ChatColor.RED + countdown + " секунд");
                } else {
                    DHAPI.setHologramLine(hologram, 1, ChatColor.GREEN + "Сундук обновлен");
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void notifyPlayers() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(pluginPrefix + chestUpdateNotification);
                }
            }
        }.runTaskLater(plugin, (25 * 60 * 20));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(pluginPrefix + chestOpenNotification);
                spawnFireworkExplosion(chestLocation);
            }
        }, chestUpdateInterval);
    }

    private void spawnFireworkExplosion(Location location) {
        location.getWorld().spawn(location, Firework.class, firework -> {
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder()
                    .withColor(Color.RED)
                    .with(FireworkEffect.Type.BURST)
                    .build());
            firework.setFireworkMeta(meta);
        });
    }

    private void startFlightAndGodModeDisabler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (essentials == null) {
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().equals(chestLocation.getWorld()) && player.getLocation().distance(chestLocation) <= 10) {
                        User user = essentials.getUser(player);
                        if (user != null) {
                            if (player.hasPermission("sochestevent.bypass")) {
                                return;
                            }
                            if (user.isGodModeEnabled()) {
                                user.setGodModeEnabled(false);
                            }
                            if (player.getAllowFlight()) {
                                player.setAllowFlight(false);
                                player.setFlying(false);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }
}
