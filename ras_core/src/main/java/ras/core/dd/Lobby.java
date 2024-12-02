package ras.core.dd;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.EditSession;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ras.core.utils.VoidChunkGenerator;
import ras.core.mechanisms.DoorMechanism;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class Lobby {
    private final JavaPlugin plugin;
    private final String id;
    private final List<Player> players = new ArrayList<>();
    private boolean started = false;
    private org.bukkit.World lobbyWorld;
    private DoorMechanism doorMechanism;

    public Lobby(JavaPlugin plugin, String id) {
        this.plugin = plugin;
        this.id = id;
        createLobbyWorld();
        startCountdown();
    }

    private void createLobbyWorld() {
        WorldCreator worldCreator = new WorldCreator(id);
        worldCreator.generator(new VoidChunkGenerator()); // Use custom VoidChunkGenerator
        lobbyWorld = Bukkit.createWorld(worldCreator);
        loadSchematic();
        setupDoorMechanism();
    }

    private void loadSchematic() {
        Clipboard clipboard = null;
        try {
            WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
            if (worldEditPlugin == null) {
                throw new IllegalStateException("WorldEdit plugin not found");
            }

            File schematicFile = new File(plugin.getDataFolder(), "ship.schem");
            if (!schematicFile.exists()) {
                throw new IllegalStateException("Schematic file not found: " + schematicFile.getAbsolutePath());
            }

            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                throw new IllegalStateException("Unsupported schematic format");
            }

            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                clipboard = reader.read();
            }

            if (clipboard != null) {
                World world = BukkitAdapter.adapt(lobbyWorld);
                try (EditSession editSession = worldEditPlugin.getWorldEdit().newEditSession(world)) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(BlockVector3.at(0, 64, 0))
                            .ignoreAirBlocks(false)
                            .build();
                    Operations.complete(operation);
                    plugin.getLogger().info("Schematic loaded successfully at (0, 64, 0).");
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load schematic: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupDoorMechanism() {
        Location buttonLocation = new Location(lobbyWorld, -7, 65, -1);
        Block buttonBlock = buttonLocation.getBlock();
        buttonBlock.setType(Material.STONE_BUTTON);
        BlockData buttonData = buttonBlock.getBlockData();
        if (buttonData instanceof Directional) {
            ((Directional) buttonData).setFacing(BlockFace.EAST);
        }
        ((Directional) buttonData).setFacing(BlockFace.EAST);
        buttonBlock.setBlockData(buttonData);

        Block[] closedBlocks = {
            new Location(lobbyWorld, -8, 64, 0).getBlock(),
            new Location(lobbyWorld, -8, 64, -1).getBlock(),
            new Location(lobbyWorld, -9, 64, 0).getBlock(),
            new Location(lobbyWorld, -9, 64, -1).getBlock(),
            new Location(lobbyWorld, -8, 65, 0).getBlock(),
            new Location(lobbyWorld, -8, 65, -1).getBlock(),
            new Location(lobbyWorld, -9, 65, 0).getBlock(),
            new Location(lobbyWorld, -9, 65, -1).getBlock(),
            new Location(lobbyWorld, -8, 66, 0).getBlock(),
            new Location(lobbyWorld, -8, 66, -1).getBlock(),
            new Location(lobbyWorld, -9, 66, 0).getBlock(),
            new Location(lobbyWorld, -9, 66, -1).getBlock()
        };

        Block[] openBlocks = {
            new Location(lobbyWorld, -8, 64, 0).getBlock(),
            new Location(lobbyWorld, -8, 64, -1).getBlock(),
            new Location(lobbyWorld, -9, 64, 0).getBlock(),
            new Location(lobbyWorld, -9, 64, -1).getBlock(),
            new Location(lobbyWorld, -8, 65, 0).getBlock(),
            new Location(lobbyWorld, -8, 65, -1).getBlock(),
            new Location(lobbyWorld, -9, 65, 0).getBlock(),
            new Location(lobbyWorld, -9, 65, -1).getBlock()
        };

        Block[] slabBlocks = {
            new Location(lobbyWorld, -8, 66, 0).getBlock(),
            new Location(lobbyWorld, -8, 66, -1).getBlock(),
            new Location(lobbyWorld, -9, 66, 0).getBlock(),
            new Location(lobbyWorld, -9, 66, -1).getBlock()
        };

        doorMechanism = new DoorMechanism(plugin, buttonBlock, closedBlocks, openBlocks, slabBlocks);
        plugin.getServer().getPluginManager().registerEvents(doorMechanism, plugin);
    }

    public void addPlayer(Player player) {
        players.add(player);
        player.sendMessage("You have been added to lobby " + id);
        player.teleport(new Location(lobbyWorld, 0, 64, 0)); // Teleport player to the lobby spawn location
        if (players.size() >= 4) {
            startLobby();
        }
    }

    public boolean isFull() {
        return players.size() >= 4;
    }

    public boolean isStarted() {
        return started;
    }

    private void startCountdown() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (players.size() >= 2) {
                    startLobby();
                } else {
                    startCountdown();
                }
            }
        }.runTaskLater(plugin, 1200L); // 60 seconds
    }

    private void startLobby() {
        if (started) return;
        started = true;
        for (Player player : players) {
            player.sendMessage("Lobby " + id + " is starting!");
        }
    }
}