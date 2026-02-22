package world;

import java.util.HashMap;
import java.util.Map;

public class ChunkWorld {

    private final Map<Long, BlockType> edits = new HashMap<>();

    public BlockType getBlock(int x, int y, int z) {
        int surface = getSurfaceHeight(x, z);

        if (y > surface) {
            if (y <= 3 && surface <= 2) return BlockType.WATER;
            return BlockType.AIR;
        }

        if (y <= 0) return BlockType.STONE;

        if (y == surface) {
            int biome = hash(x / 6, z / 6) & 7;
            return switch (biome) {
                case 0 -> BlockType.SAND;
                case 1 -> BlockType.WOOD;
                case 2 -> BlockType.GLASS;
                default -> BlockType.GRASS;
            };
        }

        if (surface - y <= 2) return BlockType.DIRT;

        return BlockType.STONE;
    }

    public boolean isSolid(int x, int y, int z) {
        return getBlock(x, y, z).solid;
    }

    public int getSurfaceHeight(int x, int z) {
        double waveA = Math.sin(x * 0.27) * 1.2;
        double waveB = Math.cos(z * 0.23) * 1.1;
        double waveC = Math.sin((x + z) * 0.11) * 0.9;
        int hills = (int) Math.round(waveA + waveB + waveC);
        return 4 + hills;
    }

    private int hash(int x, int z) {
        int h = x * 73428767 ^ z * 912673;
        h ^= (h >>> 13);
        h *= 1274126177;
        return h;
    }
}
