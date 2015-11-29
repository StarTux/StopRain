package com.winthier.endrain;

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

public class StopRainPlugin extends JavaPlugin implements Listener {
    Set<UUID> players;
    
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        for (Player player: getServer().getOnlinePlayers()) {
            if (getPlayers().contains(player.getUniqueId())) {
                player.setPlayerWeather(WeatherType.CLEAR);
            }
        }
    }

    @Override
    public void onDisable() {
        for (Player player: getServer().getOnlinePlayers()) {
            player.resetPlayerWeather();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player)sender;
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

    void turnPlayer(Player player, boolean on) {
        final UUID uuid = player.getUniqueId();
        if (on) {
            if (getPlayers().contains(uuid)) {
                player.setPlayerWeather(WeatherType.CLEAR);
                player.sendMessage(ChatColor.DARK_AQUA + "Rain is already turned off.");
            } else {
                getPlayers().add(uuid);
                savePlayers();
                player.setPlayerWeather(WeatherType.CLEAR);
                player.sendMessage(ChatColor.DARK_AQUA + "The rain will stop in a jiffy.");
            }
        } else {
            if (getPlayers().contains(uuid)) {
                getPlayers().remove(uuid);
                savePlayers();
                player.resetPlayerWeather();
                player.sendMessage(ChatColor.DARK_AQUA + "Rain turned back on.");
            } else {
                player.resetPlayerWeather();
                player.sendMessage(ChatColor.DARK_AQUA + "Rain already turned on.");
            }
        }
    }

    File getSaveFile() {
        getDataFolder().mkdirs();
        return new File(getDataFolder(), "players.yml");
    }

    Set<UUID> getPlayers() {
        if (players == null) {
            players = new HashSet<>();
            YamlConfiguration config = YamlConfiguration.loadConfiguration(getSaveFile());
            for (String string : config.getStringList("players")) {
                try {
                    players.add(UUID.fromString(string));
                } catch (IllegalArgumentException iae) {
                    iae.printStackTrace();
                }
            }
        }
        return players;
    }

    void savePlayers() {
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

    @EventHandler public void onPlayerJoin(PlayerJoinEvent event) {
        if (getPlayers().contains(event.getPlayer().getUniqueId())) {
            event.getPlayer().setPlayerWeather(WeatherType.CLEAR);
        }
    }
}
