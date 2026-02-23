package world;

import java.util.HashMap;
import java.util.Map;

public class ChunkWorld {

    private final Map<Long, BlockType> edits = new HashMap<>();

    public BlockType getBlock(int x, int y, int z) {
        BlockType edited = edits.get(key(x, y, z));
        if (edited != null) return edited;

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


    public VoxelChunk buildVoxelChunk(int chunkX, int chunkY, int chunkZ) {
        VoxelChunk chunk = new VoxelChunk(chunkX, chunkY, chunkZ);
        int baseX = chunkX * VoxelChunk.SIZE;
        int baseY = chunkY * VoxelChunk.SIZE;
        int baseZ = chunkZ * VoxelChunk.SIZE;

        for (int z = 0; z < VoxelChunk.SIZE; z++) {
            for (int y = 0; y < VoxelChunk.SIZE; y++) {
                for (int x = 0; x < VoxelChunk.SIZE; x++) {
                    BlockType b = getBlock(baseX + x, baseY + y, baseZ + z);
                    chunk.setBlock(x, y, z, b);
                }
            }
        }

        return chunk;
    }

    public void setBlock(int x, int y, int z, BlockType type) {
        edits.put(key(x, y, z), type);
    }

    public void breakBlock(int x, int y, int z) {
        edits.put(key(x, y, z), BlockType.AIR);
    }

    public Map<Long, BlockType> snapshotEdits() {
        return new HashMap<>(edits);
    }

    public void restoreEdits(Map<Long, BlockType> saved) {
        edits.clear();
        edits.putAll(saved);
    }

    public long key(int x, int y, int z) {
        return (((long) x) << 42) ^ (((long) y) << 21) ^ (z & 0x1FFFFF);
    }

    public int keyX(long key) {
        return (int) (key >> 42);
    }

    public int keyY(long key) {
        return (int) ((key >> 21) & 0x1FFFFF);
    }

    public int keyZ(long key) {
        int z = (int) (key & 0x1FFFFF);
        if ((z & 0x100000) != 0) z |= ~0x1FFFFF;
        return z;
    }

    private int hash(int x, int z) {
        int h = x * 73428767 ^ z * 912673;
        h ^= (h >>> 13);
        h *= 1274126177;
        return h;
    }
}
