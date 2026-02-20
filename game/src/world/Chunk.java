package world;

public class Chunk {

    private static final int SIZE = 16;
    private static final int HEIGHT = 32;

    private final BlockType[][][] blocks = new BlockType[SIZE][HEIGHT][SIZE];

    public Chunk() {
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                for (int y = 0; y < HEIGHT; y++) {
                    if (y == 0) blocks[x][y][z] = BlockType.STONE;
                    else if (y < 3) blocks[x][y][z] = BlockType.DIRT;
                    else if (y == 3) blocks[x][y][z] = BlockType.GRASS;
                    else blocks[x][y][z] = BlockType.AIR;
                }
            }
        }
    }

    public BlockType get(int x, int y, int z) {
        if (y < 0 || y >= HEIGHT) return BlockType.AIR;
        return blocks[x][y][z];
    }

    public void set(int x, int y, int z, BlockType b) {
        if (y < 0 || y >= HEIGHT) return;
        blocks[x][y][z] = b;
    }
}
