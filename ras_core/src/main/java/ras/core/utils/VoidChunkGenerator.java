package ras.core.utils;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.BiomeProvider;

import java.util.Random;

public class VoidChunkGenerator extends ChunkGenerator {
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeProvider biomeProvider) {
        // Create an empty chunk data object
        @SuppressWarnings("deprecation")
        ChunkData chunkData = createChunkData(world);  // No need for getWorldInfo()
        return chunkData;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return false;
    }
}
