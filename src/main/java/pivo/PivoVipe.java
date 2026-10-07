package pivo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitRunnable;

public class PivoVipe extends JavaPlugin implements CommandExecutor {

    private BukkitTask countdownTask;
    private boolean running = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("pivovipe").setExecutor(this);
        getLogger().info("PivoVipe включен!");
    }

    @Override
    public void onDisable() {
        if (countdownTask != null) {
            countdownTask.cancel();
        }
    }

    private String msg(String path) {
        return color(getConfig().getString(path, ""));
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(msg("messages.usage"));
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            if (running) {
                sender.sendMessage(msg("messages.already-running"));
                return true;
            }
            int seconds = getConfig().getInt("countdown-seconds", 120);
            startCountdown(seconds);
            sender.sendMessage(msg("messages.started").replace("%time%", formatTime(seconds)));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            sender.sendMessage(msg("messages.reloaded"));
            return true;
        }

        sender.sendMessage(msg("messages.unknown"));
        return true;
    }

    private void startCountdown(int seconds) {
        running = true;
        final int[] timeLeft = {seconds};

        String titleMain = msg("title.main");
        String titleSubTemplate = getConfig().getString("title.sub", "&fДо закрытие вайпа осталось %time%");
        String endMain = msg("end-title.main");
        String endSub = msg("end-title.sub");
        String broadcast = msg("broadcast-message");

        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (timeLeft[0] <= 0) {
                    running = false;

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendTitle(endMain, endSub, 0, 40, 10);
                    }

                    Bukkit.broadcastMessage(broadcast);

                    // Включаем whitelist через консоль — сервер закрыт на вайп
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist on");

                    this.cancel();
                    return;
                }

                String subtitle = color(titleSubTemplate.replace("%time%", formatTime(timeLeft[0])));

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle(titleMain, subtitle, 0, 25, 5);
                }

                timeLeft[0]--;
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        if (minutes > 0) {
            return minutes + "м " + seconds + "с";
        }
        return seconds + "с";
    }
}
