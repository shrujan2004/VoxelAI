package graphics;

import javafx.scene.image.Image;
import world.BlockType;

import java.io.File;
import java.util.EnumMap;

public class TexturePack {

    public static final int TILE_SIZE = 16;
    public static final int ATLAS_COLS = 4;

    private final Image atlas;
    private final EnumMap<BlockType, Image> topTiles = new EnumMap<>(BlockType.class);
    private final EnumMap<BlockType, Image> sideTiles = new EnumMap<>(BlockType.class);
    private final EnumMap<BlockType, Image> bottomTiles = new EnumMap<>(BlockType.class);

    public TexturePack(String atlasPath) {
        this.atlas = loadImage(atlasPath,
                "../tiles/atlas.png",
                "tiles/atlas.png");

        for (BlockType type : BlockType.values()) {
            if (type.topTexture != null) {
                topTiles.put(type, loadImage(
                        "game/tiles/" + type.topTexture,
                        "../tiles/" + type.topTexture,
                        "tiles/" + type.topTexture
                ));
            }
            if (type.sideTexture != null) {
                sideTiles.put(type, loadImage(
                        "game/tiles/" + type.sideTexture,
                        "../tiles/" + type.sideTexture,
                        "tiles/" + type.sideTexture
                ));
            }
            if (type.bottomTexture != null) {
                bottomTiles.put(type, loadImage(
                        "game/tiles/" + type.bottomTexture,
                        "../tiles/" + type.bottomTexture,
                        "tiles/" + type.bottomTexture
                ));
            }
        }
    }

    public Image atlas() {
        return atlas;
    }

    public Image tile(BlockType type) {
        return sideTiles.get(type);
    }

    public Image tileForFace(BlockType type, int faceY) {
        if (faceY > 0) {
            return topTiles.get(type);
        }
        if (faceY < 0) {
            return bottomTiles.get(type);
        }
        return sideTiles.get(type);
    }

    public int atlasIndex(BlockType type) {
        return switch (type) {
            case GRASS -> 0;
            case DIRT -> 1;
            case STONE -> 2;
            case SAND -> 3;
            case WOOD -> 4;
            case GLASS -> 5;
            case WATER -> 6;
            default -> 1;
        };
    }

    private Image loadImage(String... candidates) {
        for (String candidate : candidates) {
            if (candidate == null) continue;
            File f = new File(candidate);
            if (f.exists()) {
                return new Image(f.toURI().toString());
            }
        }
        return null;
    }
}
