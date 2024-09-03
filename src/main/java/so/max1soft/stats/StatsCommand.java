package so.max1soft.stats;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;

import java.lang.reflect.Field;

public class StatsCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public StatsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("stats")) {

            Player player = (Player) sender;
            if (!player.hasPermission("soapi.command.stats")) {
                player.sendMessage(ChatColor.RED + "У вас нет прав.");
                return true;
            }

            int totalPing = 0;
            int playerCount = 0;

            for (Player p : Bukkit.getOnlinePlayers()) {
                totalPing += getPing(p);
                playerCount++;
            }

            int averagePing = playerCount > 0 ? totalPing / playerCount : 0;

            int entityCount = 0;
            for (World world : Bukkit.getWorlds()) {
                entityCount += world.getEntities().size();
            }

            int chunkCount = 0;
            for (World world : Bukkit.getWorlds()) {
                chunkCount += world.getLoadedChunks().length;
            }


            int playerTotalCount = Bukkit.getOnlinePlayers().size();

            player.sendMessage("§7Техническая статистика:");
            player.sendMessage("§6| §fСредний пинг: §a" + averagePing + " ms");
            player.sendMessage( "§6| §fОнлайн: §a" + playerTotalCount);
            player.sendMessage( "§6| §fКоличество мобов: §a" + entityCount);
            player.sendMessage( "§6| §fПрогруженых чанков: §a" + chunkCount);

            return true;
        }
        return false;
    }

    private int getPing(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            int ping = (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
            return ping;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}
