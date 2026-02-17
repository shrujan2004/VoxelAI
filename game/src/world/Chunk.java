package world;

public class Chunk {
    public static final int SIZE = 16;
    private final BlockType[][] blocks = new BlockType[SIZE][SIZE];

    public Chunk() {
        generate();
    }

    private void generate() {
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                blocks[x][z] = BlockType.DIRT;
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
