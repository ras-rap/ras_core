package ras.core.mechanisms;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Switch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class DoorMechanism implements Listener {
    private final JavaPlugin plugin;
    private final Block buttonBlock;
    private final Block[] closedBlocks;
    private final Block[] openBlocks;
    private final Block[] slabBlocks;

    public DoorMechanism(JavaPlugin plugin, Block buttonBlock, Block[] closedBlocks, Block[] openBlocks, Block[] slabBlocks) {
        this.plugin = plugin;
        this.buttonBlock = buttonBlock;
        this.closedBlocks = closedBlocks;
        this.openBlocks = openBlocks;
        this.slabBlocks = slabBlocks;
        closeDoor(); // Ensure the door starts in the closed state
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.equals(buttonBlock)) {
                Switch button = (Switch) buttonBlock.getBlockData();
                if (button.isPowered()) {
                    openDoor();
                } else {
                    closeDoor();
                }
            }
        }
    }

    private void openDoor() {
        for (Block block : closedBlocks) {
            block.setType(Material.AIR);
        }
        for (Block block : slabBlocks) {
            block.setType(Material.WAXED_EXPOSED_CUT_COPPER_SLAB);
            Slab slab = (Slab) block.getBlockData();
            slab.setType(Slab.Type.TOP);
            block.setBlockData(slab);
        }
        plugin.getLogger().info("Door opened.");
    }

    private void closeDoor() {
        for (Block block : closedBlocks) {
            block.setType(Material.WAXED_EXPOSED_CUT_COPPER);
        }
        for (Block block : slabBlocks) {
            block.setType(Material.WAXED_EXPOSED_CUT_COPPER);
        }
        plugin.getLogger().info("Door closed.");
    }
}