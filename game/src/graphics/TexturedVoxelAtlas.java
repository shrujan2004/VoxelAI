package graphics;

/**
 * Atlas coordinate helper for OpenGL voxel renderers.
 *
 * Assumes a 256x256 atlas built from 16x16 tiles (16 columns x 16 rows).
 */
public final class TexturedVoxelAtlas {

    public static final int ATLAS_SIZE = 256;
    public static final int TILE_SIZE = 16;
    public static final int FACE_TOP = 0;
    public static final int FACE_SIDE = 1;
    public static final int FACE_BOTTOM = 2;

    private static final int ATLAS_COLS = ATLAS_SIZE / TILE_SIZE;

    private TexturedVoxelAtlas() {
    }

    /**
     * Returns 8 UV values as 4 coordinates: [u0,v0, u1,v0, u1,v1, u0,v1].
     */
    public static float[] getUVs(int blockID, int face) {
        int faceOffset = face == FACE_TOP ? FACE_TOP : (face == FACE_BOTTOM ? FACE_BOTTOM : FACE_SIDE);
        int tileIndex = Math.max(0, blockID) * 3 + faceOffset;

        float tileX = (tileIndex % ATLAS_COLS) * TILE_SIZE;
        float tileY = (tileIndex / ATLAS_COLS) * TILE_SIZE;

        float u0 = tileX / ATLAS_SIZE;
        float v0 = tileY / ATLAS_SIZE;
        float u1 = (tileX + TILE_SIZE) / ATLAS_SIZE;
        float v1 = (tileY + TILE_SIZE) / ATLAS_SIZE;
        return new float[]{u0, v0, u1, v0, u1, v1, u0, v1};
    }
}
