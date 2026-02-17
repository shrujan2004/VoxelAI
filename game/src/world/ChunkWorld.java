package world;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ChunkWorld {

    private static final int WORLD_HEIGHT = 6;
    private static final long SEED = 42L;

    private final Map<String, Chunk> chunks = new HashMap<>();

    private String key(int cx, int cz) {
        return cx + "," + cz;
    }

    private Chunk getChunk(int cx, int cz) {
        return chunks.computeIfAbsent(key(cx, cz), k -> new Chunk());
    }

    public BlockType getBlock(int x, int z) {
        int cx = Math.floorDiv(x, Chunk.SIZE);
        int cz = Math.floorDiv(z, Chunk.SIZE);
        int lx = Math.floorMod(x, Chunk.SIZE);
        int lz = Math.floorMod(z, Chunk.SIZE);
        BlockType manual = getChunk(cx, cz).getBlock(lx, lz);
        if (manual != BlockType.AIR) {
            return manual;
        }

        int surfaceY = getSurfaceHeight(x, z);
        if (surfaceY <= 0) {
            return BlockType.WATER;
        }
        return (x + z) % 11 == 0 ? BlockType.SAND : BlockType.GRASS;
    }

    public void setBlock(int x, int z, BlockType b) {
        int cx = Math.floorDiv(x, Chunk.SIZE);
        int cz = Math.floorDiv(z, Chunk.SIZE);
        int lx = Math.floorMod(x, Chunk.SIZE);
        int lz = Math.floorMod(z, Chunk.SIZE);
        getChunk(cx, cz).setBlock(lx, lz, b);
    }

    public boolean isWalkable(int x, int z) {
        return getBlock(x, z) != BlockType.WATER;
    }

    public int getSurfaceHeight(int x, int z) {
        long localSeed = SEED + (x * 73428767L) + (z * 912931L);
        Random r = new Random(localSeed);
        double waves = Math.sin(x * 0.15) + Math.cos(z * 0.12);
        int noise = r.nextInt(3) - 1;
        int y = 2 + (int) Math.round(waves) + noise;
        return Math.max(0, Math.min(WORLD_HEIGHT, y));
    }
}
