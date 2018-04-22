package com.winthier.endrain;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public final class StopRainPlugin extends JavaPlugin implements Listener {
    private Set<UUID> players;
    private boolean supportMiniMap = true;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
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
        setupPlayer(event.getPlayer());
    }

    void setupPlayer(Player player) {
        final UUID uuid = player.getUniqueId();
        if (getPlayers().contains(uuid)) {
            player.setPlayerWeather(WeatherType.CLEAR);
        }
        if (supportMiniMap) {
            Map<String, Object> map = new HashMap<>();
            map.put("Type", "Boolean");
            map.put("Value", players.contains(uuid));
            map.put("DisplayName", "Disable Rain");
            map.put("Priority", 0);
            Runnable onUpdate = () -> {
                boolean v = map.get("Value") == Boolean.TRUE;
                if (v) {
                    players.add(uuid);
                    player.setPlayerWeather(WeatherType.CLEAR);
                } else {
                    players.remove(uuid);
                    player.resetPlayerWeather();
                }
            };
            map.put("OnUpdate", onUpdate);
            List<Map> list = new ArrayList<>();
            list.add(map);
            player.setMetadata("MiniMapSettings", new FixedMetadataValue(this, list));
        }
    }
}
