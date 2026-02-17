package world;

public class World {

    private final int sizeX = 16;
    private final int sizeY = 8;
    private final int sizeZ = 16;

    private BlockType[][][] blocks;

    public World() {
        blocks = new BlockType[sizeX][sizeY][sizeZ];
        generateFlatWorld();
    }

    private void generateFlatWorld() {
        for (int x = 0; x < sizeX; x++) {
            for (int z = 0; z < sizeZ; z++) {
                blocks[x][0][z] = BlockType.DIRT;
                for (int y = 1; y < sizeY; y++) {
                    blocks[x][y][z] = BlockType.AIR;
                }
            }
        }
    }

    public void setBlock(int x, int y, int z, BlockType type) {
        if (inBounds(x, y, z)) {
            blocks[x][y][z] = type;
        }
    }

    public BlockType getBlock(int x, int y, int z) {
        if (inBounds(x, y, z)) {
            return blocks[x][y][z];
        }
        return BlockType.AIR;
    }

    private boolean inBounds(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 &&
               x < sizeX && y < sizeY && z < sizeZ;
    }
    public int getSizeX() {
    return sizeX;
}

public int getSizeY() {
    return sizeY;
}

public int getSizeZ() {
    return sizeZ;
}

}
