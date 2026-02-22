package world;

public enum BlockType {
    AIR(0, null, null, null, false, 0.0),

    DIRT(1, "dirt.png", "dirt.png", "dirt.png", true, 1.2),
    GRASS(2, "grass_top.png", "dirt_grass.png", "dirt.png", true, 1.2),
    STONE(3, "stone.png", "stone.png", "stone.png", true, 3.5),
    WOOD(4, "trunk_top.png", "trunk_side.png", "trunk_top.png", true, 2.0),
    SAND(5, "sand.png", "sand.png", "sand.png", true, 0.8),
    GLASS(6, "glass.png", "glass.png", "glass.png", true, 0.4),
    WATER(7, "water.png", "water.png", "water.png", false, 0.0);

    // Legacy single-texture compatibility (kept for existing callers)
    public final String texture;

    public final int atlasId;

    public final String topTexture;
    public final String sideTexture;
    public final String bottomTexture;
    public final boolean solid;
    public final double hardness;

    BlockType(int atlasId, String topTexture, String sideTexture, String bottomTexture, boolean solid, double hardness) {
        this.atlasId = atlasId;
        this.topTexture = topTexture;
        this.sideTexture = sideTexture;
        this.bottomTexture = bottomTexture;
        this.texture = sideTexture;
        this.solid = solid;
        this.hardness = hardness;
    }
}
