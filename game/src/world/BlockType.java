package world;

public enum BlockType {
    AIR(null, false),

    DIRT("dirt.png", true),
    GRASS("grass_top.png", true),
    STONE("stone.png", true),
    WOOD("wood.png", true),
    SAND("sand.png", true),
    GLASS("glass.png", true),
    WATER("water.png", false);

    public final String texture;
    public final boolean solid;

    BlockType(String texture, boolean solid) {
        this.texture = texture;
        this.solid = solid;
    }
}