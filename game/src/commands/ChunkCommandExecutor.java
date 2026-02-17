package commands;

import world.BlockType;
import world.ChunkWorld;

public class ChunkCommandExecutor {

    private final ChunkWorld world;

    public ChunkCommandExecutor(ChunkWorld world) {
        this.world = world;
    }

    public void fill(
            int x1, int y1, int z1,
            int x2, int y2, int z2,
            BlockType type
    ) {
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    world.setBlock(x, y, z, type);
                }
            }
        }

        System.out.println("✅ Filled " + ((maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1)) + " blocks with " + type);
    }
}
