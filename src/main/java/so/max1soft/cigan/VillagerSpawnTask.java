package so.max1soft.cigan;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import so.max1soft.Main;

import java.util.List;
import java.util.Random;

public class VillagerSpawnTask extends BukkitRunnable {
    private final Main plugin;
    private final Random random = new Random();
    private final BossBarManager bossBarManager;

    public boolean enabled = false;

    public VillagerSpawnTask(Main plugin, BossBarManager bossBarManager) {
        this.plugin = plugin;
        this.bossBarManager = bossBarManager;
    }

    @Override
    public void run() {
        plugin.getBossBarManager().hideBossBar();
        Location loc = getValidRandomLocation();
        if (loc == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "Не удалось найти подходящее место для спавна Цыгана.");
            return;
        }
        Villager villager = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
        villager.setCustomName(plugin.getConfig().getString("CiganVillager.Name"));
        villager.setCustomNameVisible(true);
        villager.getEquipment().setItemInMainHand(new ItemStack(Material.EMERALD));
        villager.setAI(false);
        villager.setGlowing(true);
        villager.setInvulnerable(true);
        villager.setCanPickupItems(false);
        sendBroadcastMessage(loc);
        bossBarManager.startBossBarCountdown(loc, this);
        enabled = true;
    }

    private void sendBroadcastMessage(Location loc) {
        List<String> messages = plugin.getConfig().getStringList("CiganMessages.Startmessage");
        for (String msg : messages) {
            String formattedMsg = msg
                    .replace("%x", String.valueOf(loc.getBlockX()))
                    .replace("%y", String.valueOf(loc.getBlockY()))
                    .replace("%z", String.valueOf(loc.getBlockZ()));
            Bukkit.broadcastMessage(ChatColor.YELLOW + formattedMsg);
        }
    }

    private Location getValidRandomLocation() {
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(Bukkit.getWorld("world")));
        for (int i = 0; i < 100; i++) {
            Location loc = getRandomLocation();
            Block block = loc.getBlock();
            Block belowBlock = block.getRelative(BlockFace.DOWN);
            Block aboveBlock = block.getRelative(BlockFace.UP);
            Material blockType = block.getType();
            Material belowBlockType = belowBlock.getType();
            Material aboveBlockType = aboveBlock.getType();
            Biome biome = loc.getWorld().getBiome(loc.getBlockX(), loc.getBlockZ());
            if (blockType == Material.WATER || blockType == Material.LAVA ||
                    blockType.name().contains("LEAVES") || belowBlockType == Material.WATER ||
                    belowBlockType == Material.LAVA || aboveBlockType == Material.WATER ||
                    aboveBlockType == Material.LAVA || biome == Biome.OCEAN ||
                    biome == Biome.RIVER || biome == Biome.BEACH| biome == Biome.DEEP_OCEAN|| biome == Biome.WARM_OCEAN|| biome == Biome.FROZEN_OCEAN|| biome == Biome.JUNGLE) {
                continue;
            }
            int x = loc.getBlockX();
            int z = loc.getBlockZ();
            BlockVector3 min = BlockVector3.at(x - 10, 0, z - 10);
            BlockVector3 max = BlockVector3.at(x + 10, 255, z + 10);
            ProtectedRegion newRegion = new ProtectedCuboidRegion(plugin.getConfig().getString("CiganVillager.regioname"),
                    min,
                    max);
            try {
                regionManager.addRegion(newRegion);
                return loc;
            } catch (Exception e) {
                Bukkit.getLogger().info(ChatColor.RED + "Не получилось создать регион для жителя.");
                return null;
            }
        }
        return null;
    }

    private Location getRandomLocation() {
        int x = random.nextInt(2000) - 500;
        int z = random.nextInt(2000) - 500;
        Location loc = new Location(Bukkit.getWorld("world"), x, 0, z);
        int y = loc.getWorld().getHighestBlockYAt(loc);
        loc.setY(y);
        return loc;
    }
}
