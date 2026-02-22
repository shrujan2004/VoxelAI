package graphics;

import javafx.scene.image.Image;
import world.BlockType;

import java.io.File;
import java.util.EnumMap;

public class TexturePack {

    public static final int TILE_SIZE = 16;
    public static final int ATLAS_COLS = 4;

    private final Image atlas;
    private final EnumMap<BlockType, Image> blockTiles = new EnumMap<>(BlockType.class);

    public TexturePack(String atlasPath) {
        this.atlas = loadImage(atlasPath,
                "../tiles/atlas.png",
                "tiles/atlas.png");

        for (BlockType type : BlockType.values()) {
            if (type.texture != null) {
                blockTiles.put(type, loadImage(
                        "game/tiles/" + type.texture,
                        "../tiles/" + type.texture,
                        "tiles/" + type.texture
                ));
            }
        }
    }

    public Image atlas() {
        return atlas;
    }

    public Image tile(BlockType type) {
        return blockTiles.get(type);
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
