package world;

public enum BlockType {
    AIR(null, false, ' '),
    DIRT("dirt.png", true, 'D'),
    GRASS("grass_top.png", true, 'G'),
    WOOD("wood.png", true, 'W'),
    STONE("stone.png", true, 'S'),
    GLASS("glass.png", true, 'O'),
    WATER("water.png", false, '~'),
    SAND("sand.png", true, '.');

    public final String texture;
    public final boolean solid;
    private final char minimapChar;

    BlockType(String texture, boolean solid, char minimapChar) {
        this.texture = texture;
        this.solid = solid;
        this.minimapChar = minimapChar;
    }

    public String render() {
        return this == AIR ? "·" : String.valueOf(minimapChar);
    }
}
