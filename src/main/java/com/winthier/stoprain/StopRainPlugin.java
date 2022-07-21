package com.winthier.stoprain;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.WeatherType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class StopRainPlugin extends JavaPlugin implements Listener {
    private final Set<UUID> players = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        loadPlayers();
        for (Player player: getServer().getOnlinePlayers()) {
            setupPlayer(player);
        }
    }

    @Override
    public void onDisable() {
        for (Player player: getServer().getOnlinePlayers()) {
            player.resetPlayerWeather();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        if (args.length == 0) {
            turnPlayer(player, true);
        } else {
            String cmd = args[0].toLowerCase();
            if (cmd.equals("on")) {
                turnPlayer(player, true);
            } else if (cmd.equals("off")) {
                turnPlayer(player, false);
            } else {
                return false;
            }
        }
        return true;
    }

    private void turnPlayer(Player player, boolean on) {
        final UUID uuid = player.getUniqueId();
        if (on) {
            if (players.contains(uuid)) {
                player.setPlayerWeather(WeatherType.CLEAR);
                player.sendMessage(ChatColor.DARK_AQUA + "Rain is already turned off.");
            } else {
                players.add(uuid);
                savePlayers();
                player.setPlayerWeather(WeatherType.CLEAR);
                player.sendMessage(ChatColor.DARK_AQUA + "The rain will stop in a jiffy.");
            }
        } else {
            if (players.contains(uuid)) {
                players.remove(uuid);
                savePlayers();
                player.resetPlayerWeather();
                player.sendMessage(ChatColor.DARK_AQUA + "Rain turned back on.");
            } else {
                player.resetPlayerWeather();
                player.sendMessage(ChatColor.DARK_AQUA + "Rain already turned on.");
            }
        }
    }

    private File getSaveFile() {
        getDataFolder().mkdirs();
        return new File(getDataFolder(), "players.yml");
    }

    private void loadPlayers() {
        players.clear();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(getSaveFile());
        for (String string : config.getStringList("players")) {
            try {
                players.add(UUID.fromString(string));
            } catch (IllegalArgumentException iae) {
                iae.printStackTrace();
            }
        }
    }

    private void savePlayers() {
        if (players == null) return;
        YamlConfiguration config = new YamlConfiguration();
        List<String> list = new ArrayList<>();
        for (UUID uuid: players) list.add(uuid.toString());
        config.set("players", list);
        try {
            config.save(getSaveFile());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        setupPlayer(event.getPlayer());
    }

    private void setupPlayer(Player player) {
        final UUID uuid = player.getUniqueId();
        if (players.contains(uuid)) {
            player.setPlayerWeather(WeatherType.CLEAR);
        }
    }
}
