package world;

public class ChunkWorld {

    public BlockType getBlock(int x, int y, int z) {

        if (y < 0) return BlockType.STONE;
        if (y == 0) return BlockType.STONE;
        if (y <= 3) return BlockType.DIRT;
        if (y == 4) return BlockType.GRASS;

        return BlockType.AIR;
    }

    public boolean isSolid(int x, int y, int z) {
        return getBlock(x, y, z).solid;
    }

    public int getSurfaceHeight(int x, int z) {
        return 4;
    }
}