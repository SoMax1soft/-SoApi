package so.max1soft.utils;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.List;
import java.util.logging.Logger;

public class BowUsageRestrictor implements Listener {

    private final List<String> restrictedWorlds;

    public BowUsageRestrictor(List<String> restrictedWorlds, Logger logger) {
        this.restrictedWorlds = restrictedWorlds;
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {

        if (event.getProjectile() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getProjectile();
            ProjectileSource shooter = projectile.getShooter();

            if (shooter instanceof Player) {
                Player player = (Player) shooter;
                World world = player.getWorld();
                if (restrictedWorlds.contains(world.getName())) {
                    event.setCancelled(true);
                    player.sendMessage("Использование лука запрещено в этом мире!");
                }
            }
        }
    }
}
