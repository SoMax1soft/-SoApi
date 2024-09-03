package so.max1soft.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import so.max1soft.Main;
import so.max1soft.stats.StatsCommand;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SCommandExecutor implements CommandExecutor {

    private final Main plugin;
    private final StatsCommand statsCommand;
    private final DynamicSlot dynamicSlot;
    private final WhiteList whiteList;

    public SCommandExecutor(Main plugin) {
        this.plugin = plugin;
        this.statsCommand = new StatsCommand(plugin);
        this.dynamicSlot = new DynamicSlot(plugin);
        this.whiteList = new WhiteList(plugin);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("soapi")) {
            if (args[0].equalsIgnoreCase("core")) {
                core(sender);
            }

            if (!sender.hasPermission("soapi.command")) {
                sender.sendMessage(ChatColor.RED + "У вас нет прав.");
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage("Команда не найдена или неверный аргумент. /soapi info");
            }
            if (args[0].equalsIgnoreCase("info")) {
                core(sender);
            }

            if (args[0].equalsIgnoreCase("stats")) {
                return statsCommand.onCommand(sender, command, label, args);
            } else if (args[0].equalsIgnoreCase("reload")) {
                sender.sendMessage(plugin.getConfig().getString("Utils-Message.prefix").replace("&", "§") + "§6Конфиг перезагружен.");
                plugin.reloadConfig();
                return true;
            } else if (args[0].equalsIgnoreCase("slot")) {
                handleSlotCommand(sender, args);
                return true;
            } else if (args[0].equalsIgnoreCase("wl")) {
                handleWhiteListCommand(sender, args);
                return true;
            }

            return true;
        }

        if (command.getName().equalsIgnoreCase("soinvsblock")) {
            if (!sender.hasPermission("soinvsblock.command")) {
                return true;
            } else {
                if (args.length != 4) {
                    sender.sendMessage("Usage: /soinvsblock give <player> <amount> <type>");
                    return false;
                }

                String subCommand = args[0];
                if (!subCommand.equalsIgnoreCase("give")) {
                    sender.sendMessage("Invalid sub-command. Use: give");
                    return false;
                }

                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer == null) {
                    sender.sendMessage("Player not found.");
                    return false;
                }

                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid amount.");
                    return false;
                }

                Material material;
                try {
                    material = Material.valueOf(args[3].toUpperCase());
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(plugin.getConfig().getString("invsItem.errortype").replace("&", "§"));
                    return false;
                }

                if (material != Material.DIRT && material != Material.GRASS_BLOCK && material != Material.STONE) {
                    sender.sendMessage(plugin.getConfig().getString("invsItem.errortype").replace("&", "§"));
                    return false;
                }


                ItemStack kineticBlock = new ItemStack(material, amount);
                ItemMeta meta = kineticBlock.getItemMeta();
                meta.setDisplayName(plugin.getConfig().getString("invsItem.name").replace("&", "§"));
                meta.setLore(Collections.singletonList(plugin.getConfig().getString("invsItem.lore").replace("&", "§")));
                kineticBlock.setItemMeta(meta);


                targetPlayer.getInventory().addItem(kineticBlock);

                sender.sendMessage("Given " + amount + " " + material.toString() + " to " + targetPlayer.getName());
                return true;
            }
        }

        // Обработка команды "sotrapleave"
        if (command.getName().equalsIgnoreCase("sotrapreplace")) {
            if (!sender.hasPermission("sotrapreplace.command")) {
                sender.sendMessage(ChatColor.RED + "Сперма африканца сказал что пошел ка ты нахуй");
                return true;
            } else {
                if (args.length != 3) {
                    sender.sendMessage(ChatColor.GOLD + "§fДля выдачи используйте: §6/sotrapreplace give §7(§6ник§7) §7(§6кол-во§7)");
                    return false;
                }

                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getConfig().getString("strpUtils-Message.nofoundplayer").replace("&", "§"));
                    return false;
                }

                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getConfig().getString("strpUtils-Message.invalidamount").replace("&", "§"));
                    return false;
                }

                ItemStack item = new ItemStack(Material.BLAZE_POWDER, amount);
                ItemMeta meta = item.getItemMeta();
                if (meta == null) {
                    sender.sendMessage(ChatColor.RED + "Ошибка: не удалось создать метаданные для предмета.");
                    return false;
                }

                String displayName = plugin.getConfig().getString("strpItem.name");
                if (displayName != null) {
                    meta.setDisplayName(displayName.replace("&", "§"));
                } else {
                    sender.sendMessage(ChatColor.RED + "Ошибка: название предмета не найдено в конфигурации.");
                    return false;
                }

                List<String> lore = plugin.getConfig().getStringList("strpItem.lore").stream()
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                        .collect(Collectors.toList());

                if (lore.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "Ошибка: лор предмета не найден в конфигурации.");
                    return false;
                }

                meta.setLore(lore);
                item.setItemMeta(meta);

                target.getInventory().addItem(item);
                sender.sendMessage(plugin.getConfig().getString("strpUtils-Message.success").replace("&", "§")
                        .replace("%player%", target.getName()).replace("%amount%", String.valueOf(amount)));
                return true;
            }
        }

        return false;
    }
    private void core(CommandSender sender) {
        sender.sendMessage("§7———————————————————————————————————");
        sender.sendMessage("§fСервер запущен с использыванием §x§F§F§8§6§0§0§l§ns§x§F§F§B§0§0§0§l§nᴏ§x§F§F§D§9§0§0§l§nᴀ§x§F§F§B§0§0§0§l§nᴘ§x§F§F§8§6§0§0§l§nɪ");
        sender.sendMessage("§fВерсия билда: §x§F§F§8§6§0§0S§x§F§F§9§B§0§0P§x§F§F§B§0§0§02§x§F§F§C§4§0§0E§x§F§F§D§9§0§0R§x§F§F§C§4§0§06§x§F§F§B§0§0§01§x§F§F§9§B§0§0M§x§F§F§8§6§0§0A");
        sender.sendMessage("§7———————————————————————————————————");
        sender.sendMessage("§fСоздатель: §x§F§F§8§6§0   §0S§x§F§F§9§B§0§0o§x§F§F§B§0§0§0M§x§F§F§C§4§0§0a§x§F§F§D§9§0§0x§x§F§F§C§8§0§01§x§F§F§B§8§0§0s§x§F§F§A§7§0§0o§x§F§F§9§7§0§0f§x§F§F§8§6§0§0t");
        sender.sendMessage("§fСвязь: §x§F§F§8§6§0§0t§x§F§F§9§4§0§0.§x§F§F§A§2§0§0m§x§F§F§B§0§0§0e§x§F§F§B§D§0§0/§x§F§F§C§B§0§0m§x§F§F§D§9§0§0a§x§F§F§C§B§0§0x§x§F§F§B§D§0§01§x§F§F§B§0§0§0s§x§F§F§A§2§0§0o§x§F§F§9§4§0§0f§x§F§F§8§6§0§0t");
        sender.sendMessage("§fВерсия: §62.5");
        sender.sendMessage("§7———————————————————————————————————");
    }

    private void sendPluginInfo(CommandSender sender) {
        sender.sendMessage("§7———————————————————————————————————");
        sender.sendMessage("§fПлагин §6SoApi§f, имеет §6широкий§f функционал,");
        sender.sendMessage("§fдля §6серверов§f майнкрафт.");
        sender.sendMessage("§7———————————————————————————————————");
        sender.sendMessage("§fСоздатель: §6SoMax1soft");
        sender.sendMessage("§fСвязь: §6t.me/max1soft");
        sender.sendMessage("§fВерсия: §62.5");
        sender.sendMessage("§7———————————————————————————————————");
        sender.sendMessage("§eСписок доступных команд:");
        sender.sendMessage("§7- §6stats: §7Техническая статистика сервера.");
        sender.sendMessage("§7- §6reload: §7Перезагрузка конфига.");
        sender.sendMessage("§eКастомные слоты:");
        sender.sendMessage("§7- §6slot set <количество>: §7Установить лимит игроков.");
        sender.sendMessage("§eВайтлист:");
        sender.sendMessage("§7- §6wl add (ник): §7Добавить игрока в вайтлист.");
        sender.sendMessage("§7- §6wl del (ник): §7Удалить игрока из вайтлиста.");
        sender.sendMessage("§7- §6wl on: §7Включить вайтлист.");
        sender.sendMessage("§7- §6wl off: §7Выключить вайтлист.");
        sender.sendMessage("§eКинетические блоки:");
        sender.sendMessage("§7- §6Если игрок наступит на блок то он пропадёт.");
        sender.sendMessage("§7- §6soinvsblock give §7(§6ник§7) §7(§6тип§7).");
        sender.sendMessage("§7- §6Типы:§7 GRASS,DIRT,STONE");
        sender.sendMessage("§eИвент лотерея:");
        sender.sendMessage("§7- §6Лотерея на сервере рандомный игрок получит приз.");
        sender.sendMessage("§7- §6lottery give §7(§6start§7) §7(§6join§7).");
        sender.sendMessage("§eПредмет замены:");
        sender.sendMessage("§7- §6При использывании меняет местами,");
        sender.sendMessage("§7- §6вас и ближайшего игрока в радиусе 10 блоков.");
        sender.sendMessage("§7- §6sotrapreplace give §7(§6ник§7) §7(§6кол-во§7).");
        sender.sendMessage("§eИвент - сундук:");
        sender.sendMessage("§7- §6На координатах в конфиге сундук, каждое");
        sender.sendMessage("§7- §6определеное время обновляет лут в сундуке,");
        sender.sendMessage("§7- §6голограма над сундуком и партиклы при открытии,");
        sender.sendMessage("§7- §6предметы,сообщения и тд настраиваються в конфиге.");
        sender.sendMessage("§eИвент - цыган:");
        sender.sendMessage("§7- §6На рандомных координатах,спавниться житель который,");
        sender.sendMessage("§7- §6в радиусе 10 блоков выдает награду через команду в конфиге,");
        sender.sendMessage("§7- §6интервал и переодичность настраиваеться в конфиге.,");
        sender.sendMessage("§7———————————————————————————————————");
    }

    private void handleSlotCommand(CommandSender sender, String[] args) {
        if (args.length >= 3 && args[1].equalsIgnoreCase("set")) {
            try {
                int newLimit = Integer.parseInt(args[2]);
                dynamicSlot.setPlayerLimit(sender, newLimit);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Неверное число: " + args[2]);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Использование: /soapi slot set <количество>");
        }
    }

    private void handleWhiteListCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendWhiteListUsage(sender);
            return;
        }

        if (args[1].equalsIgnoreCase("on")) {
            whiteList.setWhitelistEnabled(true);
            sender.sendMessage(plugin.getConfig().getString("Utils-Message.prefix").replace("&", "§") + "§fВайтлист §aвключен, чужеземцы не пройдут!");
        } else if (args[1].equalsIgnoreCase("off")) {
            whiteList.setWhitelistEnabled(false);
            sender.sendMessage(plugin.getConfig().getString("Utils-Message.prefix").replace("&", "§") + "§fВайтлист §cвыключен, мы в опасности?");
        } else if (args[1].equalsIgnoreCase("add")) {
            if (args.length < 3) {
                sender.sendMessage(plugin.getConfig().getString("Utils-Message.prefix").replace("&", "§") + "§fИспользуйте:§6 /soapi wl add <ник>");
                return;
            }
            String playerName = args[2];
            whiteList.addPlayerToWhitelist(playerName);
            sender.sendMessage(plugin.getConfig().getString("Utils-Message.prefix").replace("&", "§") + playerName + " §aдобавлен в вайтлист.");
        } else if (args[1].equalsIgnoreCase("del")) {
            if (args.length < 3) {
                sender.sendMessage(plugin.getConfig().getString("Utils-Message.prefix").replace("&", "§") + "§fИспользуйте:§6 /soapi wl del <ник>");
                return;
            }
            String playerName = args[2];
            whiteList.removePlayerFromWhitelist(playerName);
            sender.sendMessage(plugin.getConfig().getString("Utils-Message.prefix").replace("&", "§") + playerName + " §cудален из вайтлиста.");
        } else {
            sendWhiteListUsage(sender);
        }
    }

    private void sendWhiteListUsage(CommandSender sender) {
        sender.sendMessage("§fИспользуйте:§6 /soapi wl <add/del/on/off> §7<ник>");
    }
}
