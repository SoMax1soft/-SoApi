package so.max1soft.utils;

import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ShulkerDeletor implements Listener {

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        // Проверяем, что инвентарь, который открывают, является инвентарем шалкера
        if (event.getInventory().getHolder() instanceof ShulkerBox) {
            Inventory shulkerInventory = event.getInventory();
            if (containsForbiddenItems(shulkerInventory)) {
                // Удаляем все предметы из инвентаря шалкера
                shulkerInventory.clear();
            }
        }
    }

    private boolean isShulkerBox(ItemStack item) {
        return item != null && item.getType().name().endsWith("_SHULKER_BOX");
    }

    private boolean isForbiddenItem(ItemStack item) {
        return item != null && (item.getType() == Material.NETHERITE_INGOT ||
                item.getType() == Material.DRAGON_EGG ||
                isShulkerBox(item));
    }

    private boolean containsForbiddenItems(Inventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            if (item != null && isForbiddenItem(item)) {
                return true;
            }
        }
        return false;
    }
}
