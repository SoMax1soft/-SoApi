package so.max1soft.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import so.max1soft.Main;

public class BedGardenBlocker implements Listener {

    private final Main plugin;

    public BedGardenBlocker(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Material blockType = block.getType();

        if (isBed(blockType)) {
            if (isNearGarden(block)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Вы не можете разместить кровать рядом с грядкой!");
            }
        } else if (blockType == Material.FARMLAND) {
            if (isNearBed(block)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Вы не можете разместить грядку рядом с кроватью!");
            }
        }
    }

    private boolean isBed(Material material) {
        return material.name().endsWith("_BED");
    }

    private boolean isNearGarden(Block block) {
        return checkBlocksInRadius(block, Material.FARMLAND, 3);
    }

    private boolean isNearBed(Block block) {
        return checkBlocksInRadius(block, null, 3, this::isBed);
    }

    private boolean checkBlocksInRadius(Block block, Material targetType, int radius) {
        int startX = block.getX() - radius;
        int startY = block.getY() - radius;
        int startZ = block.getZ() - radius;

        int endX = block.getX() + radius;
        int endY = block.getY() + radius;
        int endZ = block.getZ() + radius;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    if (block.getWorld().getBlockAt(x, y, z).getType() == targetType) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkBlocksInRadius(Block block, Material targetType, int radius, MaterialChecker checker) {
        int startX = block.getX() - radius;
        int startY = block.getY() - radius;
        int startZ = block.getZ() - radius;

        int endX = block.getX() + radius;
        int endY = block.getY() + radius;
        int endZ = block.getZ() + radius;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    Material material = block.getWorld().getBlockAt(x, y, z).getType();
                    if (checker.check(material)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @FunctionalInterface
    private interface MaterialChecker {
        boolean check(Material material);
    }
}
