package commands;

import world.World;
import world.BlockType;

public class CommandExecutor {

    private World world;

    public CommandExecutor(World world) {
        this.world = world;
    }

    /**
     * Fill a rectangular area with a block type
     */
    public void fill(
            int x1, int y1, int z1,
            int x2, int y2, int z2,
            BlockType type
    ) {
        // Normalize coordinates (important!)
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

        System.out.println("✅ Filled area with " + type);
    }
}
