package world;

public class ChunkWorld {

    private static final int SIZE_X = 64;
    private static final int SIZE_Y = 8;
    private static final int SIZE_Z = 64;

    private final BlockType[][][] blocks = new BlockType[SIZE_X][SIZE_Y][SIZE_Z];

    public ChunkWorld() {
        generate();
    }

    private void generate() {
        for (int x = 0; x < SIZE_X; x++) {
            for (int z = 0; z < SIZE_Z; z++) {
                blocks[x][0][z] = BlockType.GRASS;
                for (int y = 1; y < SIZE_Y; y++) {
                    blocks[x][y][z] = BlockType.AIR;
                }
            }
        }
    }

    public BlockType get(int x, int y, int z) {
        if (x < 0 || z < 0 || y < 0) return BlockType.AIR;
        if (x >= SIZE_X || z >= SIZE_Z || y >= SIZE_Y) return BlockType.AIR;
        return blocks[x][y][z];
    }

    public boolean isSolid(int x, int y, int z) {
        return get(x, y, z) != BlockType.AIR;
    }
}
