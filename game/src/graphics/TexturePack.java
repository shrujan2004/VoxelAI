package graphics;

import javafx.scene.image.Image;
import world.BlockType;

import java.io.File;
import java.util.EnumMap;

public class TexturePack {

    public static final int TILE_SIZE = 16;
    public static final int ATLAS_COLS = 4; // legacy fallback for callers that still rely on fixed atlas layout
    private static final int FACE_VARIANTS = 3; // top, side, bottom

    // Prompt used for creating additional atlas tiles in the same style.
    public static final String TEXTURE_PROMPT_TEMPLATE =
            "Professional 16x16 pixel art texture for a voxel game block, [Material Name], seamless tiling on all axes, " +
                    "1:1 aspect ratio. Use a limited 8-bit color palette with hue shifting (e.g., purples in shadows, " +
                    "yellows in highlights). Ensure sharp nearest-neighbor edges, zero blur, and no baked-in 3D lighting " +
                    "or shadows. Output should look like a flat, clean asset from a high-quality Minecraft texture pack.";

    public static final String TEXTURE_SHEET_PROMPT_TEMPLATE =
            "Texture sheet for a 3D voxel game, [Material], 16x16 pixel art resolution, perfectly tileable/seamless " +
                    "on all sides, flat lighting, limited color palette with vibrant hue shifting, sharp pixel edges, " +
                    "no 3D shadows or blur, high contrast, top-down perspective, clean grid layout.";

    private final Image atlas;
    private final int atlasCols;
    private final int atlasRows;

    private final EnumMap<BlockType, Image> topTiles = new EnumMap<>(BlockType.class);
    private final EnumMap<BlockType, Image> sideTiles = new EnumMap<>(BlockType.class);
    private final EnumMap<BlockType, Image> bottomTiles = new EnumMap<>(BlockType.class);

    public TexturePack(String atlasPath) {
        this.atlas = loadImage(atlasPath,
                "../tiles/atlas.png",
                "tiles/atlas.png");

        this.atlasCols = atlas == null ? 0 : Math.max(1, (int) Math.floor(atlas.getWidth() / TILE_SIZE));
        this.atlasRows = atlas == null ? 0 : Math.max(1, (int) Math.floor(atlas.getHeight() / TILE_SIZE));

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

            validateTextureStandard(type, topTiles.get(type));
            validateTextureStandard(type, sideTiles.get(type));
            validateTextureStandard(type, bottomTiles.get(type));
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

    public AtlasUV atlasUvForFace(BlockType type, int faceY) {
        if (atlas == null) {
            return null;
        }

        int faceOffset = faceY > 0 ? 0 : (faceY < 0 ? 2 : 1);
        int tileIndex = type.atlasId * FACE_VARIANTS + faceOffset;
        int tileCount = atlasCols * atlasRows;

        if (tileIndex < 0 || tileIndex >= tileCount) {
            tileIndex = atlasIndex(type);
        }
        if (tileIndex < 0 || tileIndex >= tileCount) {
            return null;
        }

        int sx = (tileIndex % atlasCols) * TILE_SIZE;
        int sy = (tileIndex / atlasCols) * TILE_SIZE;
        return new AtlasUV(sx, sy, TILE_SIZE, TILE_SIZE);
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

    private void validateTextureStandard(BlockType type, Image texture) {
        if (texture == null || type == BlockType.AIR) {
            return;
        }

        int w = (int) Math.round(texture.getWidth());
        int h = (int) Math.round(texture.getHeight());
        if (w != TILE_SIZE || h != TILE_SIZE) {
            System.err.println("[TexturePack] Non-standard texture size for " + type +
                    ": " + w + "x" + h + ". Expected 16x16 to avoid mixels.");
        }
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

    public record AtlasUV(int sx, int sy, int sw, int sh) {
    }
}
