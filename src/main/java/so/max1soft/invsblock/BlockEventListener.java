package so.max1soft.invsblock;

import com.earth2me.essentials.Essentials;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.GameMode;
import so.max1soft.Main;

public class BlockEventListener implements Listener {

    private final Main plugin;
    private final Essentials essentials;

    public BlockEventListener(Main plugin, Essentials essentials) {
        this.plugin = plugin;
        this.essentials = essentials;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        ItemMeta meta = event.getItemInHand().getItemMeta();

        if (meta != null && plugin.getConfig().getString("invsItem.name").replace("&", "§").equals(meta.getDisplayName())) {
            addKineticBlock(block.getLocation());
            event.getPlayer().sendMessage(plugin.getConfig().getString("invsItem.messageplace").replace("&", "§"));
        }
    }

    @EventHandler
    public void onPlayerStep(PlayerMoveEvent event) {
        Player player = event.getPlayer();


        Location loc = event.getTo().clone();
        loc.setY(loc.getY() - 1);
        Block block = loc.getBlock();

        if (isKineticBlock(loc)) {
            if (player.isFlying() || (essentials != null && essentials.getUser(player).isGodModeEnabled()) || player.getGameMode() == GameMode.CREATIVE) {
                return; 
            }
            player.sendMessage(plugin.getConfig().getString("invsItem.message").replace("&", "§"));
            block.setType(Material.AIR);
            removeKineticBlock(loc);
        }
    }

    private boolean isKineticBlock(Location loc) {
        FileConfiguration config = plugin.getBlocksConfig();
        String locString = locToString(loc);
        return config.contains("blocks." + locString);
    }

    private void removeKineticBlock(Location loc) {
        FileConfiguration config = plugin.getBlocksConfig();
        String locString = locToString(loc);
        config.set("blocks." + locString, null);
        plugin.saveBlocksConfig();
    }

    private void addKineticBlock(Location loc) {
        FileConfiguration config = plugin.getBlocksConfig();
        String locString = locToString(loc);
        config.set("blocks." + locString, true);
        plugin.saveBlocksConfig();
    }

    private String locToString(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }
}
