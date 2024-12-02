package ras.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import okhttp3.*;
import org.json.JSONObject;
import ras.core.Plugin;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class SkinUtils {

    public static Map<String, String> getCurrentSkinData(Player player) {
        PlayerProfile profile = player.getPlayerProfile();
        for (ProfileProperty property : profile.getProperties()) {
            if (property.getName().equals("textures")) {
                String value = property.getValue();
                String signature = property.getSignature();
                String decoded = new String(Base64.getDecoder().decode(value));
                JSONObject json = new JSONObject(decoded);
                String url = json.getJSONObject("textures").getJSONObject("SKIN").getString("url");
                Map<String, String> skinData = new HashMap<>();
                skinData.put("url", url);
                skinData.put("signature", signature);
                return skinData;
            }
        }
        return null;
    }

    public static BufferedImage getSuitOverlayImage(String suitType) throws Exception {
        String suitFileName;
        switch (suitType) {
            case "purple":
            suitFileName = "/suits/purple-suit.png";
            break;
            case "green":
            suitFileName = "/suits/green-suit.png";
            break;
            case "pajamas":
            case "pj":
            suitFileName = "/suits/pj-suit.png";
            break;
            case "default":
            default:
            suitFileName = "/suits/suit.png";
            break;
        }
        try (InputStream in = SkinUtils.class.getResourceAsStream(suitFileName)) {
            return ImageIO.read(in);
        }
    }

    public static String mergeSkinWithSuit(String originalSkinUrl, BufferedImage suitOverlay) throws Exception {
        BufferedImage originalSkin;
        try (InputStream in = new URL(originalSkinUrl).openStream()) {
            originalSkin = ImageIO.read(in);
        }

        Graphics2D g = originalSkin.createGraphics();
        g.setComposite(AlphaComposite.SrcOver);
        g.drawImage(suitOverlay, 0, 0, null);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(originalSkin, "png", baos);
        byte[] skinBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(skinBytes);
    }

    public static String[] uploadToMineskin(Plugin plugin, String base64Texture) throws Exception {
        String apiKey = plugin.getConfig().getString("mineskin-api-key");
        if (apiKey == null || apiKey.isEmpty()) {
            plugin.getLogger().severe("Mineskin API key is not set.");
            return null;
        }

        OkHttpClient client = new OkHttpClient();
        File tempFile = File.createTempFile("skin", ".png");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Texture);
            fos.write(decodedBytes);
        }

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", tempFile.getName(),
                        RequestBody.create(tempFile, MediaType.parse("image/png")))
                .build();

        Request request = new Request.Builder()
                .url(Plugin.MINESKIN_API_URL)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                plugin.getLogger().severe("Failed to upload skin to Mineskin: " + response.message());
                return null;
            }

            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);
            String texture = jsonResponse.getJSONObject("data").getJSONObject("texture").getString("value");
            String signature = jsonResponse.getJSONObject("data").getJSONObject("texture").getString("signature");
            return new String[]{texture, signature};
        } finally {
            tempFile.delete();
        }
    }

    public static Map<String, String> getOriginalSkinData(Plugin plugin, String uuid) throws Exception {
        URL url = new URL(Plugin.MOJANG_API_URL + uuid + "?unsigned=false");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            plugin.getLogger().severe("Failed to fetch skin data from Mojang API. Response code: " + responseCode);
            return null;
        }

        InputStream inputStream = connection.getInputStream();
        StringBuilder response = new StringBuilder();
        int read;
        while ((read = inputStream.read()) != -1) {
            response.append((char) read);
        }
        inputStream.close();

        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONObject properties = jsonResponse.getJSONArray("properties").getJSONObject(0);
        String value = properties.getString("value");
        String signature = properties.getString("signature");

        Map<String, String> skinData = new HashMap<>();
        skinData.put("value", value);
        skinData.put("signature", signature);
        return skinData;
    }

    public static void refreshPlayer(Plugin plugin, Player player) {
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            onlinePlayer.hidePlayer(plugin, player);
            Bukkit.getScheduler().runTaskLater(plugin, () -> onlinePlayer.showPlayer(plugin, player), 5L);
        });
    }
}