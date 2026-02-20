package world;

import java.util.HashMap;
import java.util.Map;

public class ChunkWorld {

    public static final int CHUNK_SIZE = 16;
    public static final int WORLD_HEIGHT = 32;

    private final Map<Long, Chunk> chunks = new HashMap<>();

    /* =======================
       PUBLIC API
       ======================= */

    public BlockType getBlock(int x, int y, int z) {
        if (y < 0 || y >= WORLD_HEIGHT) return BlockType.AIR;
        Chunk c = getChunk(x, z);
        return c.get(local(x), y, local(z));
    }

    public void setBlock(int x, int y, int z, BlockType type) {
        if (y < 0 || y >= WORLD_HEIGHT) return;
        Chunk c = getChunk(x, z);
        c.set(local(x), y, local(z), type);
    }

    public boolean isSolid(int x, int y, int z) {
        return getBlock(x, y, z).solid;
    }

    /**
     * ✅ REQUIRED BY FXGame
     * Checks if player can stand/move at X,Z
     */
    public boolean isWalkable(int x, int z) {
        int surface = getSurfaceHeight(x, z);
        // walkable if ground is solid and head space is free
        return isSolid(x, surface, z)
                && !isSolid(x, surface + 1, z)
                && !isSolid(x, surface + 2, z);
    }

    public int getSurfaceHeight(int x, int z) {
        for (int y = WORLD_HEIGHT - 1; y >= 0; y--) {
            if (getBlock(x, y, z).solid) {
                return y;
            }
        }
        return 0;
    }

    /* =======================
       INTERNALS
       ======================= */

    private Chunk getChunk(int x, int z) {
        int cx = floorDiv(x, CHUNK_SIZE);
        int cz = floorDiv(z, CHUNK_SIZE);
        long key = chunkKey(cx, cz);

        Chunk c = chunks.get(key);
        if (c == null) {
            c = new Chunk(cx, cz);
            chunks.put(key, c);
        }
        return c;
    }

    private static int local(int v) {
        int r = v % CHUNK_SIZE;
        return r < 0 ? r + CHUNK_SIZE : r;
    }

    private static int floorDiv(int a, int b) {
        int r = a / b;
        if ((a ^ b) < 0 && a % b != 0) r--;
        return r;
    }

    private static long chunkKey(int cx, int cz) {
        return (((long) cx) << 32) ^ (cz & 0xffffffffL);
    }

    /* =======================
       CHUNK
       ======================= */

    private static class Chunk {

        private final BlockType[][][] blocks =
                new BlockType[CHUNK_SIZE][WORLD_HEIGHT][CHUNK_SIZE];

        Chunk(int cx, int cz) {
            generate(cx, cz);
        }

        BlockType get(int x, int y, int z) {
            BlockType b = blocks[x][y][z];
            return b == null ? BlockType.AIR : b;
        }

        void set(int x, int y, int z, BlockType type) {
            blocks[x][y][z] = type;
        }

        private void generate(int cx, int cz) {
            for (int x = 0; x < CHUNK_SIZE; x++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {

                    int worldX = cx * CHUNK_SIZE + x;
                    int worldZ = cz * CHUNK_SIZE + z;

                    int height = terrainHeight(worldX, worldZ);

                    for (int y = 0; y < WORLD_HEIGHT; y++) {
                        if (y > height) {
                            blocks[x][y][z] = BlockType.AIR;
                        } else if (y == height) {
                            blocks[x][y][z] = BlockType.GRASS;
                        } else if (y >= height - 3) {
                            blocks[x][y][z] = BlockType.DIRT;
                        } else {
                            blocks[x][y][z] = BlockType.STONE;
                        }
                    }
                }
            }
        }

        private int terrainHeight(int x, int z) {
            double n =
                    Math.sin(x * 0.08) * 2.5 +
                    Math.cos(z * 0.08) * 2.5;
            return 8 + (int) n;
        }
    }
}
