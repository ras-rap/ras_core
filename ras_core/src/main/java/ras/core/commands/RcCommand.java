package ras.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ras.core.Plugin;

public class RcCommand implements CommandExecutor {

    private final Plugin plugin;

    public RcCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 3 && args[0].equalsIgnoreCase("api") && args[1].equalsIgnoreCase("mineskin")) {
            String apiKey = args[2];
            plugin.getConfig().set("mineskin-api-key", apiKey);
            plugin.saveConfig();
            sender.sendMessage("Mineskin API key has been set.");
            return true;
        }
        sender.sendMessage("Usage: /rc api mineskin {api key}");
        return false;
    }
}