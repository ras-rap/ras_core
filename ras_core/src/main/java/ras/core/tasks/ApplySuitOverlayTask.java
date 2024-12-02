package ras.core.tasks;

import java.awt.image.BufferedImage;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;

import ras.core.Plugin;
import ras.core.utils.SkinUtils;

public class ApplySuitOverlayTask implements Runnable {

    private final Plugin plugin;
    private final Player player;
    private final String suitType;

    public ApplySuitOverlayTask(Plugin plugin, Player player, String suitType) {
        this.plugin = plugin;
        this.player = player;
        this.suitType = suitType;
    }

    @Override
    public void run() {
        Map<String, String> skinData = SkinUtils.getCurrentSkinData(player);
        if (skinData == null) {
            return;
        }
        String skinUrl = skinData.get("url");

        try {
            BufferedImage suitOverlay = SkinUtils.getSuitOverlayImage(suitType);
            String combinedTexture = SkinUtils.mergeSkinWithSuit(skinUrl, suitOverlay);

            String[] signedTextureData = SkinUtils.uploadToMineskin(plugin, combinedTexture);
            if (signedTextureData == null) {
                return;
            }

            String signedTexture = signedTextureData[0];
            String signedSignature = signedTextureData[1];

            Bukkit.getScheduler().runTask(plugin, () -> {
                PlayerProfile profile = player.getPlayerProfile();
                profile.getProperties().clear();
                profile.getProperties().add(new ProfileProperty("textures", signedTexture, signedSignature));

                player.setPlayerProfile(profile);
                SkinUtils.refreshPlayer(plugin, player);
            });
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to apply " + suitType + " suit overlay: " + e.getMessage());
        }
    }
}