package graphics;

import javafx.scene.image.Image;
import world.BlockType;

import java.util.EnumMap;

public class TexturePack {

    public static final int TILE_SIZE = 16;
    public static final int ATLAS_COLS = 4;

    private final Image atlas;
    private final EnumMap<BlockType, Image> blockTiles = new EnumMap<>(BlockType.class);

    public TexturePack(String atlasPath) {
        this.atlas = loadImage(atlasPath);

        for (BlockType type : BlockType.values()) {
            if (type.texture != null) {
                blockTiles.put(type, loadImage("game/tiles/" + type.texture));
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

    private Image loadImage(String path) {
        try {
            return new Image(java.nio.file.Path.of(path).toUri().toString());
        } catch (Exception ignored) {
            return null;
        }
    }
}
