   package so.max1soft;

   import com.earth2me.essentials.Essentials;
   import com.sk89q.worldedit.WorldEdit;
   import com.sk89q.worldedit.bukkit.BukkitAdapter;
   import com.sk89q.worldedit.world.World;
   import com.sk89q.worldguard.WorldGuard;
   import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
   import com.sk89q.worldguard.protection.managers.RegionManager;
   import org.bukkit.Bukkit;
   import org.bukkit.ChatColor;
   import org.bukkit.configuration.file.FileConfiguration;
   import org.bukkit.configuration.file.YamlConfiguration;
   import org.bukkit.entity.Entity;
   import org.bukkit.entity.Player;
   import org.bukkit.entity.Villager;
   import org.bukkit.plugin.Plugin;
   import org.bukkit.plugin.java.JavaPlugin;
   import org.bukkit.scheduler.BukkitTask;
   import so.max1soft.border.BorderRtpFix;
   import so.max1soft.chestevent.ChestEvent;
   import so.max1soft.cigan.BossBarManager;
   import so.max1soft.cigan.VillagerListener;
   import so.max1soft.cigan.VillagerSpawnTask;
   import so.max1soft.crystal.CrystalDamageReducer;
   import so.max1soft.fix.NoMountEvent;
   import so.max1soft.fix.RegionInfo;
   import so.max1soft.fix.RegionLength;
   import so.max1soft.invsblock.BlockEventListener;
   import so.max1soft.nohead.NoHead;
   import so.max1soft.solottery.EventCommand;
   import so.max1soft.solottery.LotteryListener;
   import so.max1soft.spawnutils.TeleportOnJoin;
   import so.max1soft.spawnutils.WeatherTask;
   import so.max1soft.utils.*;

   import java.io.*;
   import java.net.HttpURLConnection;
   import java.net.URL;
   import java.nio.charset.StandardCharsets;
   import java.util.HashMap;
   import java.util.List;
   import java.util.Map;
   import java.util.Scanner;

   public class Main extends JavaPlugin {
      private BukkitTask villagerSpawnTask;
      private BossBarManager bossBarManager;
      private FileConfiguration blocksConfig;
      private FileConfiguration config;
      private File blocksFile;
      private WorldGuard worldGuard;
      private WorldEdit worldEdit;
      private Map<Player, Long> cooldowns = new HashMap<>();
      private static Main instance;
      private Essentials essentials;
      private ChestEvent chestEvent;
      private WeatherTask weatherTask;
      private LotteryListener lotteryListener;

      String prefix = getConfig().getString("Utils-Message.prefix").replace("&", "§");

      @Override
      public void onEnable() {

         instance = this;
         startAutomaticEvent();
         this.saveDefaultConfig();
         getCommand("lottery").setExecutor(new EventCommand(this));
         lotteryListener = new LotteryListener(this); // Создаем экземпляр LotteryListener
         Bukkit.getPluginManager().registerEvents(lotteryListener, this); // Регистрируем его как слушателя
         this.getCommand("sotrapreplace").setExecutor(new SCommandExecutor(this));
         this.getServer().getPluginManager().registerEvents(new BedGardenBlocker(this), this);
         Bukkit.getPluginManager().registerEvents(new so.max1soft.trapreplace.trapreplace(this), this);
         this.getCommand("soapi").setExecutor(new SCommandExecutor(this));
         this.getServer().getPluginManager().registerEvents(new ShulkerDeletor(), this);
         this.getCommand("ytb").setExecutor(new BroadcastListener(this));
         this.getCommand("soinvsblock").setExecutor(new SCommandExecutor(this));
         this.getServer().getPluginManager().registerEvents(new CrystalDamageReducer(), this);
         this.getServer().getPluginManager().registerEvents(new BorderRtpFix(this), this);
         this.getServer().getPluginManager().registerEvents(new NoHead(this), this);
         this.getServer().getPluginManager().registerEvents(new TeleportOnJoin(this), this);
         this.getServer().getPluginManager().registerEvents(new RegionInfo(), this);
         this.getServer().getPluginManager().registerEvents(new GameMode(this), this);
         this.getServer().getPluginManager().registerEvents(new automessage(this), this);
         this.getServer().getPluginManager().registerEvents(new RegionLength(this), this);
         this.getServer().getPluginManager().registerEvents(new NoMountEvent(), this);
         this.getServer().getPluginManager().registerEvents(new WhiteList(this), this);
         Bukkit.getPluginManager().registerEvents(new BlockEventListener(this, essentials), this);
         this.config = getConfig();
         List<String> restrictedWorlds = getConfig().getStringList("Utils.fix.restricted-worlds");

         // Регистрируем слушателя событий для ограничения использования лука
         getServer().getPluginManager().registerEvents(new BowUsageRestrictor(restrictedWorlds, getLogger()), this);

         createBlocksFile();

         this.weatherTask = new WeatherTask();
         this.weatherTask.runTaskTimer(this, 0L, 100L);

         Plugin essentialsPlugin = getServer().getPluginManager().getPlugin("Essentials");
         Essentials essentials = null;
         if (essentialsPlugin instanceof Essentials) {
            essentials = (Essentials) essentialsPlugin;
         }


         chestEvent = new ChestEvent(this, essentials);
         getLogger().info("");
         getLogger().info("§fПлагин: §aЗапущен");
         getLogger().info("§fСоздатель: §b@max1soft");
         getLogger().info("§fВерсия: §c2.5");
         getLogger().info("");
         int interval = getConfig().getInt("CiganVillager.spawn-interval", 7200);
         bossBarManager = new BossBarManager(this);

         VillagerSpawnTask villager = new VillagerSpawnTask(this, bossBarManager);
         villagerSpawnTask = villager.runTaskTimer(this, interval * 20L, interval * 20L);

         Bukkit.getPluginManager().registerEvents(new VillagerListener(this, essentials, villager), this);

      }
      public LotteryListener getLotteryListener() {
         return lotteryListener;
      }
      @Override
      public void onDisable() {
         if (chestEvent != null) {
            chestEvent.deleteHologram();

            getLogger().info(ChatColor.RED + "Плагин отключен.");
            if (villagerSpawnTask != null) {
               villagerSpawnTask.cancel();
            }
            anus();
         }
      }

      String name = this.getDescription().getName();


      public static Main getInstance() {
         return instance;
      }

      public WeatherTask getWeatherTask() {
         return this.weatherTask;
      }
      public static String c(String color) {
         return ChatColor.translateAlternateColorCodes('&', color);
      }
      public void anus() {
         removeVillagers();
         if (worldGuard != null) {
            RegionManager regionManager = worldGuard.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(Bukkit.getWorld("world")));
            if (regionManager != null) {
               regionManager.removeRegion(getConfig().getString("CiganVillager.regioname"));
            }
         }
      }
      private void startAutomaticEvent() {
         int intervalMinutes = getConfig().getInt("slweventIntervalMinutes", 10); // Интервал в минутах
         long intervalTicks = intervalMinutes * 60L * 20L; // Перевод минут в тики

         getServer().getScheduler().runTaskTimer(this, () -> {
            if (!lotteryListener.isEventRunning()) {
               lotteryListener.startEvent(null); // Запуск ивента
            }
         }, intervalTicks, intervalTicks); // Запуск с начальной задержкой и интервалом
      }
      public void removeVillagers() {
         for (Entity entity : Bukkit.getWorld("world").getEntities()) {
            if (entity instanceof Villager) {
               Villager villager = (Villager) entity;
               if (villager.getCustomName() != null && villager.getCustomName().contains(getConfig().getString("CiganVillager.Name"))) {
                  villager.remove();
               }
            }
         }
      }

      private void createBlocksFile() {
         blocksFile = new File(getDataFolder(), "blocks.yml");
         if (!blocksFile.exists()) {
            blocksFile.getParentFile().mkdirs();
            try {
               blocksFile.createNewFile();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }

         blocksConfig = YamlConfiguration.loadConfiguration(blocksFile);
      }

      public FileConfiguration getBlocksConfig() {
         return blocksConfig;
      }

      public void saveBlocksConfig() {
         try {
            blocksConfig.save(blocksFile);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }

      public void startVillagerSpawn() {
         if (villagerSpawnTask != null && !villagerSpawnTask.isCancelled()) {
            villagerSpawnTask.cancel();
         }
         int interval = getConfig().getInt("CiganVillager.spawn-interval", 7200);
         villagerSpawnTask = new VillagerSpawnTask(this, bossBarManager).runTaskTimer(this, 1, interval * 20L);
      }

      public BossBarManager getBossBarManager() {
         return bossBarManager;
      }

      public void addCooldown(Player player, long cooldownEndTime) {
         cooldowns.put(player, cooldownEndTime);
      }

      public boolean checkCooldown(Player player) {
         if (cooldowns.containsKey(player)) {
            long endTime = cooldowns.get(player);
            return System.currentTimeMillis() < endTime;
         }
         return false;
      }

      public void startCooldown(Player player) {
         cooldowns.put(player, System.currentTimeMillis() + 10000L);
      }
   }
