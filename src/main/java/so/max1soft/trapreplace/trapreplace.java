package so.max1soft.trapreplace;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import so.max1soft.Main;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class trapreplace implements Listener {
    private Main plugin;
    private HashMap<UUID, Long> cooldowns = new HashMap<>();
    private List<String> lore;

    public trapreplace(Main plugin) {
        this.plugin = plugin;
        this.lore = plugin.getConfig().getStringList("strpItem.lore").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null || !event.getItem().hasItemMeta()) return;
        ItemMeta meta = event.getItem().getItemMeta();
        if (meta.hasLore() && meta.getLore().equals(lore)) {
            if (event.getPlayer().getWorld().getName().equalsIgnoreCase(plugin.getConfig().getString("strpItem.blockedworld"))) {
                event.getPlayer().sendMessage(plugin.getConfig().getString("strpUtils-Message.blockedworld").replace("&", "§"));
                return;
            }

            UUID playerId = event.getPlayer().getUniqueId();
            if (cooldowns.containsKey(playerId)) {
                long timeLeft = (cooldowns.get(playerId) - System.currentTimeMillis()) / 1000;
                if (timeLeft > 0) {
                    String message = plugin.getConfig().getString("strpUtils-Message.cooldown");
                    event.getPlayer().sendMessage(message.replace("%cooldown%", String.valueOf(timeLeft)).replace("&", "§"));
                    return;
                }
            }

            Player closestPlayer = null;
            double closestDistance = 10.0;
            for (Player other : event.getPlayer().getWorld().getPlayers()) {
                if (other == event.getPlayer()) continue;
                double distance = other.getLocation().distance(event.getPlayer().getLocation());
                if (distance <= 10.0 && (closestPlayer == null || distance < closestDistance)) {
                    closestPlayer = other;
                    closestDistance = distance;
                }
            }

            if (closestPlayer != null) {
                Location playerLocation = event.getPlayer().getLocation();
                Location otherLocation = closestPlayer.getLocation();
                event.getPlayer().teleport(otherLocation);
                closestPlayer.teleport(playerLocation);

                String forPlayerMessage = plugin.getConfig().getString("strpUtils-Message.playerswap");
                event.getPlayer().sendMessage(forPlayerMessage.replace("%player%", closestPlayer.getName()).replace("&", "§"));
                String closeMessage = plugin.getConfig().getString("strpUtils-Message.closeswap");
                closestPlayer.sendMessage(closeMessage.replace("%player%", event.getPlayer().getName()).replace("&", "§"));

                event.getItem().setAmount(event.getItem().getAmount() - 1);
                cooldowns.put(playerId, System.currentTimeMillis() + (250 * 1000));
            } else {
                event.getPlayer().sendMessage(plugin.getConfig().getString("strpUtils-Message.noplayerradius").replace("&", "§"));
            }
        }
    }
}
