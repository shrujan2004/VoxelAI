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
        // Phase-1 stabilization: ChunkWorld is a read-only terrain provider.
        // Keep command surface intact, but avoid mutating world data in this mode.
        System.out.println("⚠ ChunkWorld is read-only in stabilization mode; /fill skipped for area "
                + "(" + x1 + "," + y1 + "," + z1 + ") to (" + x2 + "," + y2 + "," + z2 + ") with " + type);
    }
}
