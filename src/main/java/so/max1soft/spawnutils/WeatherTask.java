package so.max1soft.spawnutils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import so.max1soft.Main;

public class WeatherTask extends BukkitRunnable {
   @Override
   public void run() {
      this.changeWeatherAndTime();
   }

   private void changeWeatherAndTime() {
      FileConfiguration config = Main.getInstance().getConfig();
      World spawnWorld = Bukkit.getWorld(config.getString("Utils.weather.world"));
      if (spawnWorld != null) {
         spawnWorld.setStorm(false);
         spawnWorld.setThundering(false);
         spawnWorld.setTime(0L);
      }
   }
}
