package so.max1soft.solottery;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class LotteryListener implements Listener {
    private boolean eventRunning = false;
    private List<Player> participants = new ArrayList<>();
    private BossBar bossBar;
    private int taskId = -1;
    private Plugin plugin;

    public LotteryListener(Plugin plugin) {

        this.plugin = plugin;
    }

    public boolean isEventRunning() {
        return eventRunning;
    }
    private boolean sent120SecondsMessage = false; // Переменная для отслеживания отправки сообщения о 2 минутах

    public void startEvent(Player player) {
        if (eventRunning) {
            sendMessage(player, "startAlreadyRunning");
            return;
        }

        eventRunning = true;

        for (String message : plugin.getConfig().getStringList("slwmessages.startAnnouncement")) {
            Bukkit.broadcastMessage(c(message));
        }

        bossBar = Bukkit.createBossBar("Время до конца лотореи", BarColor.YELLOW, BarStyle.SEGMENTED_6, BarFlag.PLAY_BOSS_MUSIC);
        bossBar.setProgress(1.0);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(onlinePlayer);
        }

        int eventDurationSeconds = 180;
        int ticksPerSecond = 20;

        double decrementPerTick = 1.0 / (eventDurationSeconds * ticksPerSecond);

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            int remainingSeconds = (int) (bossBar.getProgress() * eventDurationSeconds);


            if (remainingSeconds <= 0) {
                if (participants.isEmpty()) {
                    for (String message : plugin.getConfig().getStringList("slwmessages.endNoParticipants")) {
                        Bukkit.broadcastMessage(c(message));
                    }
                } else {
                    Player winner = pickWinner();

                    for (String message : plugin.getConfig().getStringList("slwmessages.endWinner")) {
                        Bukkit.broadcastMessage(c(message.replace("%s", winner.getName())));
                    }

                    for (String getReward : plugin.getConfig().getStringList("reward")) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), getReward.replace("%player", winner.getName()));
                    }
                }

                participants.clear();
                bossBar.removeAll();
                eventRunning = false;
                Bukkit.getScheduler().cancelTask(taskId);
            } else {
                bossBar.setTitle(getConfigString("slwbossBarTitle", remainingSeconds));
                bossBar.setProgress(bossBar.getProgress() - decrementPerTick);
            }
        }, 0L, 1L);
    }




    public void joinEvent(Player player) {
        if (!eventRunning) {
            sendMessage(player, "joinErrorNotRunning");
        } else if (participants.contains(player)) {
            sendMessage(player, "joinErrorAlreadyJoined");
        } else {
            participants.add(player);
            sendMessage(player, "joinSuccess");
        }
    }

    public Player pickWinner() {
        int index = (int) (Math.random() * participants.size());
        return participants.get(index);
    }

    public void sendMessage(Player player, String key, Object... args) {
        String message = getConfigString("slwmessages." + key, args);
        if (player != null) {
            player.sendMessage(c(message));
        } else {
            Bukkit.broadcastMessage(c(message));
        }
    }

    public String getConfigString(String path, Object... args) {
        String message = plugin.getConfig().getString(path, "");
        if (!message.isEmpty()) {
            message = String.format(message, args);
        }
        return message;
    }

    public static void startAutomaticEvent(Plugin plugin) {
        int repeatIntervalMinutes = plugin.getConfig().getInt("slweventIntervalMinutes", 1);
        int repeatTicks = repeatIntervalMinutes * 60 * 20;
        BukkitScheduler scheduler = Bukkit.getScheduler();

        LotteryListener listener = new LotteryListener(plugin); // Создаём один экземпляр

        scheduler.scheduleSyncRepeatingTask(plugin, () -> listener.startEvent(null), repeatTicks, repeatTicks);
    }


    public static String c(String color) {
        return ChatColor.translateAlternateColorCodes('&', color);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (eventRunning == false) {
            return;
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> bossBar.addPlayer(player));

        }

    }


}
