package ras.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ras.core.Plugin;
import ras.core.tasks.ApplySuitOverlayTask;
import ras.core.tasks.ResetPlayerSkinTask;

public class WearSuitCommand implements CommandExecutor {

    private final Plugin plugin;

    public WearSuitCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can run this command!");
            return true;
        }

        String suitType = args.length > 0 ? args[0].toLowerCase() : "default";
        if (!Plugin.getSuitTypes().contains(suitType)) {
            player.sendMessage("Invalid suit type! Available types: " + String.join(", ", Plugin.getSuitTypes()));
            return true;
        }

        if (suitType.equals("reset")) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new ResetPlayerSkinTask(plugin, player));
            player.sendMessage("Your skin has been reset to the original.");
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new ApplySuitOverlayTask(plugin, player, suitType));
            player.sendMessage("You are now wearing a " + suitType + " suit!");
        }
        return true;
    }
}