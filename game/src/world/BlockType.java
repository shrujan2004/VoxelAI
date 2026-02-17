package world;

public enum BlockType {
    AIR(null, false),
    DIRT("dirt.png", true),
    GRASS("grass.png", true),
    WOOD("wood.png", true),
    STONE("stone.png", true);

    public final String texture;
    public final boolean solid;

    BlockType(String texture, boolean solid) {
        this.texture = texture;
        this.solid = solid;
    }
}
