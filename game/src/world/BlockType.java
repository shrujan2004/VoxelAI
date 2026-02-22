package world;

public enum BlockType {
    AIR(null, null, null, false, 0.0),

    DIRT("dirt.png", "dirt.png", "dirt.png", true, 1.2),
    GRASS("grass_top.png", "dirt_grass.png", "dirt.png", true, 1.2),
    STONE("stone.png", "stone.png", "stone.png", true, 3.5),
    WOOD("trunk_top.png", "trunk_side.png", "trunk_top.png", true, 2.0),
    SAND("sand.png", "sand.png", "sand.png", true, 0.8),
    GLASS("glass.png", "glass.png", "glass.png", true, 0.4),
    WATER("water.png", "water.png", "water.png", false, 0.0);

    // Legacy single-texture compatibility (kept for existing callers)
    public final String texture;

    public final String topTexture;
    public final String sideTexture;
    public final String bottomTexture;
    public final boolean solid;
    public final double hardness;

    BlockType(String topTexture, String sideTexture, String bottomTexture, boolean solid, double hardness) {
        this.topTexture = topTexture;
        this.sideTexture = sideTexture;
        this.bottomTexture = bottomTexture;
        this.texture = sideTexture;
        this.solid = solid;
        this.hardness = hardness;
    }
}
