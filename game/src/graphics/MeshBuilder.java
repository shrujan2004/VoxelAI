package graphics;

import world.BlockType;
import world.VoxelChunk;

import java.nio.FloatBuffer;

public class MeshBuilder {

    // position(3), uv(2), normal(3)
    private static final int FLOATS_PER_VERTEX = 8;
    private static final float ATLAS_SIZE = 256.0f;
    private static final int TILE_SIZE = 16;
    private static final int ATLAS_COLS = 16;
    private static final float UV_EPSILON = 0.001f;

    public ChunkMesh build(VoxelChunk chunk) {
        // Worst case: 16^3 blocks * 6 faces * 6 vertices
        int maxVertices = VoxelChunk.SIZE * VoxelChunk.SIZE * VoxelChunk.SIZE * 6 * 6;
        FloatBuffer buffer = FloatBuffer.allocate(maxVertices * FLOATS_PER_VERTEX);

        int baseX = chunk.chunkX() * VoxelChunk.SIZE;
        int baseY = chunk.chunkY() * VoxelChunk.SIZE;
        int baseZ = chunk.chunkZ() * VoxelChunk.SIZE;

        for (int z = 0; z < VoxelChunk.SIZE; z++) {
            for (int y = 0; y < VoxelChunk.SIZE; y++) {
                for (int x = 0; x < VoxelChunk.SIZE; x++) {
                    BlockType block = chunk.block(x, y, z);
                    if (block == BlockType.AIR) {
                        continue;
                    }

                    int wx = baseX + x;
                    int wy = baseY + y;
                    int wz = baseZ + z;

                    // Face culling: only build faces adjacent to air/transparent cells.
                    if (chunk.isTransparent(x, y + 1, z)) putFace(buffer, block, wx, wy, wz, Face.TOP);
                    if (chunk.isTransparent(x, y - 1, z)) putFace(buffer, block, wx, wy, wz, Face.BOTTOM);
                    if (chunk.isTransparent(x + 1, y, z)) putFace(buffer, block, wx, wy, wz, Face.EAST);
                    if (chunk.isTransparent(x - 1, y, z)) putFace(buffer, block, wx, wy, wz, Face.WEST);
                    if (chunk.isTransparent(x, y, z + 1)) putFace(buffer, block, wx, wy, wz, Face.SOUTH);
                    if (chunk.isTransparent(x, y, z - 1)) putFace(buffer, block, wx, wy, wz, Face.NORTH);
                }
            }
        }

        buffer.flip();
        return new ChunkMesh(buffer, buffer.limit() / FLOATS_PER_VERTEX);
    }

    private void putFace(FloatBuffer buffer, BlockType block, int x, int y, int z, Face face) {
        UvRect uv = mapBlockIdToAtlasUV(block.atlasId, face);

        for (int i = 0; i < 6; i++) {
            float[] p = face.positions[i];
            float[] t = face.uvs[i];
            buffer.put(x + p[0]).put(y + p[1]).put(z + p[2]);
            buffer.put(lerp(uv.u0, uv.u1, t[0])).put(lerp(uv.v0, uv.v1, t[1]));
            buffer.put(face.normalX).put(face.normalY).put(face.normalZ);
        }
    }

    // Maps a 1x1x1 block face to a precise 16x16 region in a [0,1] atlas UV space.
    public UvRect mapBlockIdToAtlasUV(int blockId, Face face) {
        int faceOffset = switch (face) {
            case TOP -> 0;
            case BOTTOM -> 2;
            default -> 1;
        };

        int tileIndex = blockId * 3 + faceOffset;
        float tileX = (tileIndex % ATLAS_COLS) * TILE_SIZE;
        float tileY = (tileIndex / ATLAS_COLS) * TILE_SIZE;

        float u0 = (tileX + UV_EPSILON) / ATLAS_SIZE;
        float v0 = (tileY + UV_EPSILON) / ATLAS_SIZE;
        float u1 = (tileX + TILE_SIZE - UV_EPSILON) / ATLAS_SIZE;
        float v1 = (tileY + TILE_SIZE - UV_EPSILON) / ATLAS_SIZE;
        return new UvRect(u0, v0, u1, v1);
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public record UvRect(float u0, float v0, float u1, float v1) {
    }

    public enum Face {
        TOP(0, 1, 0,
                new float[][]{
                        {0, 1, 0}, {1, 1, 0}, {1, 1, 1},
                        {0, 1, 0}, {1, 1, 1}, {0, 1, 1}
                }),
        BOTTOM(0, -1, 0,
                new float[][]{
                        {0, 0, 0}, {1, 0, 1}, {1, 0, 0},
                        {0, 0, 0}, {0, 0, 1}, {1, 0, 1}
                }),
        EAST(1, 0, 0,
                new float[][]{
                        {1, 0, 0}, {1, 0, 1}, {1, 1, 1},
                        {1, 0, 0}, {1, 1, 1}, {1, 1, 0}
                }),
        WEST(-1, 0, 0,
                new float[][]{
                        {0, 0, 0}, {0, 1, 1}, {0, 0, 1},
                        {0, 0, 0}, {0, 1, 0}, {0, 1, 1}
                }),
        SOUTH(0, 0, 1,
                new float[][]{
                        {0, 0, 1}, {0, 1, 1}, {1, 1, 1},
                        {0, 0, 1}, {1, 1, 1}, {1, 0, 1}
                }),
        NORTH(0, 0, -1,
                new float[][]{
                        {0, 0, 0}, {1, 1, 0}, {0, 1, 0},
                        {0, 0, 0}, {1, 0, 0}, {1, 1, 0}
                });

        final float normalX;
        final float normalY;
        final float normalZ;
        final float[][] positions;
        final float[][] uvs;

        private static float[][] baseUvs() {
            return new float[][]{
                    {0, 0}, {1, 0}, {1, 1},
                    {0, 0}, {1, 1}, {0, 1}
            };
        }

        Face(float normalX, float normalY, float normalZ, float[][] positions) {
            this.normalX = normalX;
            this.normalY = normalY;
            this.normalZ = normalZ;
            this.positions = positions;
            this.uvs = baseUvs();
        }
    }
}
