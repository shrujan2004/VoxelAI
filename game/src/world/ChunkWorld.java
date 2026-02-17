package world;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ChunkWorld {

    private static final int MAX_Y = 32;
    private static final long SEED = 42L;

    private final Map<String, BlockType> modifiedBlocks = new HashMap<>();

    private String key(int x, int y, int z) {
        return x + ":" + y + ":" + z;
    }

    public BlockType getBlock(int x, int z) {
        return getBlock(x, getSurfaceHeight(x, z), z);
    }

    public BlockType getBlock(int x, int y, int z) {
        BlockType override = modifiedBlocks.get(key(x, y, z));
        if (override != null) {
            return override;
        }

        int surface = getSurfaceHeight(x, z);
        if (y < 0 || y > MAX_Y) {
            return BlockType.AIR;
        }

        if (y > surface) {
            return surface <= 1 && y <= 1 ? BlockType.WATER : BlockType.AIR;
        }

        if (y == surface) {
            if (surface <= 1) {
                return BlockType.SAND;
            }
            return ((x + z) % 11 == 0) ? BlockType.SAND : BlockType.GRASS;
        }

        if (y < surface - 3) {
            return BlockType.STONE;
        }

        return BlockType.DIRT;
    }

    public void setBlock(int x, int y, int z, BlockType type) {
        modifiedBlocks.put(key(x, y, z), type);
    }

    public void setBlock(int x, int z, BlockType type) {
        int y = getSurfaceHeight(x, z);
        setBlock(x, y, z, type);
    }

    public boolean isSolid(int x, int y, int z) {
        return getBlock(x, y, z).solid;
    }

    public int getSurfaceHeight(int x, int z) {
        long localSeed = SEED + (x * 73428767L) + (z * 912931L);
        Random random = new Random(localSeed);

        double baseWave = Math.sin(x * 0.11) * 2.0 + Math.cos(z * 0.13) * 1.6;
        int noise = random.nextInt(3) - 1;
        int hill = (int) Math.round(baseWave) + noise;

        return Math.max(0, Math.min(MAX_Y - 2, 5 + hill));
    }
}
