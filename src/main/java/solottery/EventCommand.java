package so.max1soft.solottery;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import so.max1soft.Main;

public class EventCommand implements CommandExecutor {
    private Main plugin;

    public EventCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }

        Player player = (Player) sender;
        LotteryListener lotteryListener = plugin.getLotteryListener();
        if (args[0].equalsIgnoreCase("join")) {
            if (!lotteryListener.isEventRunning()) {
                player.sendMessage(ChatColor.RED + "Лотерея не запущена.");
            } else {
                plugin.getServer().getScheduler().runTask(plugin, () -> lotteryListener.joinEvent(player));
            }
            return true;
        }
        if (!player.hasPermission("solottery.start")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды.");
            return true;
        }


        if (args.length == 0) {
            return false;
        }

        if (args[0].equalsIgnoreCase("start")) {
            if (lotteryListener.isEventRunning()) {
                player.sendMessage(ChatColor.RED + "Лотерея уже запущена.");
            } else {
                plugin.getServer().getScheduler().runTask(plugin, () -> lotteryListener.startEvent(player));
            }
            return true;
        }



        return false;
    }
}


