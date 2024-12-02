package ras.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import ras.core.commands.RcCommand;
import ras.core.commands.WearSuitCommand;
import ras.core.matchmaking.MatchmakingManager;
import ras.core.mechanisms.DoorMechanism;
import ras.core.tasks.ResetPlayerSkinTask;
import ras.core.utils.AsciiEmbed;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Plugin extends JavaPlugin implements Listener {
    private MatchmakingManager matchmakingManager;
    private FileConfiguration config;
    private final Map<String, Map<String, String>> originalSkins = new HashMap<>();
    public static final String MINESKIN_API_URL = "https://api.mineskin.org/generate/upload";
    public static final String MOJANG_API_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final List<String> SUIT_TYPES = Arrays.asList("default", "purple", "green", "pajamas", "pj", "reset");

    @Override
    public void onEnable() {
        createConfig();
        long startTime = System.currentTimeMillis();
        PluginCommand wearsuit = getCommand("wearsuit");
        if (wearsuit != null) {
            wearsuit.setExecutor(new WearSuitCommand(this));
            wearsuit.setTabCompleter(this);
        }
        PluginCommand rc = getCommand("rc");
        if (rc != null) {
            rc.setExecutor(new RcCommand(this));
        }
        
        matchmakingManager = new MatchmakingManager(this);
        PluginCommand joinlobby = getCommand("joinlobby");
        if (joinlobby != null) {
            joinlobby.setExecutor(this);
        }

        getServer().getPluginManager().registerEvents(this, this);
        long loadTime = System.currentTimeMillis() - startTime;
        AsciiEmbed embed = new AsciiEmbed("Ras Core", "Ras Core has loaded.", "single")
        .addSection("Loaded in " + loadTime + "ms")
        .addField("Version", getDescription().getVersion())
        .addField("Developer", "Ras_rap")
        .addSection("")
        .addSection("https://ras-rap.click");
        
        getLogger().info("\n" + embed.generate());
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("joinlobby")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                matchmakingManager.addPlayerToQueue(player);
                player.sendMessage("You have been added to the matchmaking queue.");
                return true;
            }
        }
        return false;
    }

    private void createConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getDataFolder().mkdirs();
            saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        storeOriginalSkin(player);
    }

    private void storeOriginalSkin(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(this, new ResetPlayerSkinTask(this, player));
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public Map<String, Map<String, String>> getOriginalSkins() {
        return originalSkins;
    }

    public static List<String> getSuitTypes() {
        return SUIT_TYPES;
    }
}