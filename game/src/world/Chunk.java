package world;

public class Chunk {

    public static final int SIZE = 16;
    private final BlockType[][] blocks = new BlockType[SIZE][SIZE];

    public Chunk() {
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                if (z == 0) blocks[x][z] = BlockType.GRASS;
                else if (z == 1) blocks[x][z] = BlockType.DIRT;
                else blocks[x][z] = BlockType.AIR;
            }
        }
    }

    public BlockType getBlock(int x, int z) {
        return blocks[x][z];
    }

    public void setBlock(int x, int z, BlockType b) {
        blocks[x][z] = b;
    }
}
