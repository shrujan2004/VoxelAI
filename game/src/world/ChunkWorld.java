package world;

import java.util.HashMap;
import java.util.Map;

public class ChunkWorld {

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
        return getChunk(cx, cz).getBlock(lx, lz);
    }

    public void setBlock(int x, int z, BlockType b) {
        int cx = Math.floorDiv(x, Chunk.SIZE);
        int cz = Math.floorDiv(z, Chunk.SIZE);
        int lx = Math.floorMod(x, Chunk.SIZE);
        int lz = Math.floorMod(z, Chunk.SIZE);
        getChunk(cx, cz).setBlock(lx, lz, b);
    }

    public boolean isWalkable(int x, int z) {
        BlockType b = getBlock(x, z);
        return !b.solid;
    }
}
