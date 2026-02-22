package world;

public enum BlockType {
    AIR(null, false, 0.0),

    DIRT("dirt.png", true, 1.2),
    GRASS("grass_top.png", true, 1.2),
    STONE("stone.png", true, 3.5),
    WOOD("wood.png", true, 2.0),
    SAND("sand.png", true, 0.8),
    GLASS("glass.png", true, 0.4),
    WATER("water.png", false, 0.0);

    // Texture filename relative to game/tiles/
    public final String texture;
    public final boolean solid;
    public final double hardness;

    BlockType(String texture, boolean solid, double hardness) {
        this.texture = texture;
        this.solid = solid;
        this.hardness = hardness;
    }
}
