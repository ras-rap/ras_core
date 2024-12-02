package ras.core.tasks;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;

import ras.core.Plugin;
import ras.core.utils.SkinUtils;

public class ResetPlayerSkinTask implements Runnable {

    private final Plugin plugin;
    private final Player player;

    public ResetPlayerSkinTask(Plugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void run() {
        try {
            Map<String, String> skinData = SkinUtils.getOriginalSkinData(plugin, player.getUniqueId().toString());
            if (skinData == null) {
                return;
            }
            String originalTexture = skinData.get("value");
            String originalSignature = skinData.get("signature");

            Bukkit.getScheduler().runTask(plugin, () -> {
                PlayerProfile profile = player.getPlayerProfile();
                profile.getProperties().clear();
                profile.getProperties().add(new ProfileProperty("textures", originalTexture, originalSignature));

                player.setPlayerProfile(profile);
                SkinUtils.refreshPlayer(plugin, player);
            });
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to reset skin for player: " + player.getName());
        }
    }
}