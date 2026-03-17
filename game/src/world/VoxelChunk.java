package world;

public class VoxelChunk {
    public static final int SIZE = 16;

    private final int chunkX;
    private final int chunkY;
    private final int chunkZ;
    private final byte[] blockIds = new byte[SIZE * SIZE * SIZE];

    public VoxelChunk(int chunkX, int chunkY, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
    }

    public int chunkX() {
        return chunkX;
    }

    public int chunkY() {
        return chunkY;
    }

    public int chunkZ() {
        return chunkZ;
    }

    public byte id(int x, int y, int z) {
        if (!inBounds(x, y, z)) {
            return 0;
        }
        return blockIds[index(x, y, z)];
    }

    public BlockType block(int x, int y, int z) {
        byte id = id(x, y, z);
        BlockType[] values = BlockType.values();
        int idx = Math.max(0, Math.min(values.length - 1, id & 0xFF));
        return values[idx];
    }

    public void setId(int x, int y, int z, byte id) {
        if (!inBounds(x, y, z)) {
            return;
        }
        blockIds[index(x, y, z)] = id;
    }

    public void setBlock(int x, int y, int z, BlockType type) {
        setId(x, y, z, (byte) type.ordinal());
    }

    public boolean isAir(int x, int y, int z) {
        return block(x, y, z) == BlockType.AIR;
    }

    public boolean isTransparent(int x, int y, int z) {
        BlockType block = block(x, y, z);
        return block == BlockType.AIR || block == BlockType.WATER || block == BlockType.GLASS;
    }

    private boolean inBounds(int x, int y, int z) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE && z >= 0 && z < SIZE;
    }

    private int index(int x, int y, int z) {
        return x + SIZE * (y + SIZE * z);
    }
}
