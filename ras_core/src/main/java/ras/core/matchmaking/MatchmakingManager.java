package ras.core.matchmaking;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import ras.core.dd.Lobby;

import java.util.*;

public class MatchmakingManager {
    private final JavaPlugin plugin;
    private final Map<String, Lobby> lobbies = new HashMap<>();
    private final Queue<Player> queue = new LinkedList<>();

    public MatchmakingManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void addPlayerToQueue(Player player) {
        queue.add(player);
        checkQueue();
    }

    private void checkQueue() {
        while (!queue.isEmpty()) {
            Player player = queue.poll();
            Lobby lobby = findOrCreateLobby();
            lobby.addPlayer(player);
        }
    }

    private Lobby findOrCreateLobby() {
        for (Lobby lobby : lobbies.values()) {
            if (!lobby.isFull() && !lobby.isStarted()) {
                return lobby;
            }
        }
        return createNewLobby();
    }

    private Lobby createNewLobby() {
        String lobbyId = UUID.randomUUID().toString();
        Lobby lobby = new Lobby(plugin, lobbyId);
        lobbies.put(lobbyId, lobby);
        return lobby;
    }
}