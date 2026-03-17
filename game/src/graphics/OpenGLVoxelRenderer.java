package graphics;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenGL textured voxel renderer helper that:
 * - loads .png tiles from game/tiles
 * - stitches them into a single texture atlas
 * - uploads/binds atlas with GL_TEXTURE_2D
 * - draws textured faces with glTexCoord2f before glVertex3f
 */
public final class OpenGLVoxelRenderer {

    private static final int TILES_PER_ROW = 16;

    private int textureId;
    private final Map<String, Integer> tileIndexByName = new HashMap<>();

    public int textureId() {
        return textureId;
    }

    /**
     * Initialization phase for texture binding pipeline.
     */
    public void initializeTextureAtlas(Path tilesDir) throws IOException {
        AtlasBuild atlas = buildAtlasFromTiles(tilesDir);
        this.tileIndexByName.clear();
        this.tileIndexByName.putAll(atlas.tileIndexByName());
        this.textureId = uploadAtlasAndBind(atlas.image());
        OpenGLTextureAtlasUtil.applyNearestNeighborFiltering(textureId);
        enableTexturing();
    }

    public float[] getUVsForTile(String tileFileName, int face) {
        Integer tileIndex = tileIndexByName.get(tileFileName);
        if (tileIndex == null) {
            tileIndex = 0;
        }

        int blockId = tileIndex / 3;
        int faceOffset = face == TexturedVoxelAtlas.FACE_TOP
                ? TexturedVoxelAtlas.FACE_TOP
                : (face == TexturedVoxelAtlas.FACE_BOTTOM ? TexturedVoxelAtlas.FACE_BOTTOM : TexturedVoxelAtlas.FACE_SIDE);
        return TexturedVoxelAtlas.getUVs(blockId, faceOffset);
    }

    /**
     * Immediate mode helper: emits glTexCoord2f(u,v) before glVertex3f(x,y,z).
     */
    public void drawFaceImmediate(float[][] vertices, float[] uvs) {
        invokeGlColor3f(1.0f, 1.0f, 1.0f);
        invokeBeginTriangles();
        for (int i = 0; i < vertices.length; i++) {
            int uvIndex = Math.min(3, i % 4) * 2;
            invokeGlTexCoord2f(uvs[uvIndex], uvs[uvIndex + 1]);
            float[] v = vertices[i];
            invokeGlVertex3f(v[0], v[1], v[2]);
        }
        invokeEnd();
    }

    public static String texturedFragmentShader() {
        return "#version 330 core\n" +
                "in vec2 v_TexCoord;\n" +
                "uniform sampler2D u_Texture;\n" +
                "out vec4 fragColor;\n" +
                "void main(){\n" +
                "    fragColor = texture(u_Texture, v_TexCoord);\n" +
                "}\n";
    }

    private AtlasBuild buildAtlasFromTiles(Path tilesDir) throws IOException {
        List<Path> tilePaths = new ArrayList<>();
        try (var stream = Files.list(tilesDir)) {
            stream
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".png"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .forEach(tilePaths::add);
        }

        if (tilePaths.size() > TexturedVoxelAtlas.ATLAS_SIZE) {
            throw new IOException("Too many tiles for 16x16 atlas grid: " + tilePaths.size());
        }

        BufferedImage atlas = new BufferedImage(
                TexturedVoxelAtlas.ATLAS_SIZE,
                TexturedVoxelAtlas.ATLAS_SIZE,
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g2d = atlas.createGraphics();

        Map<String, Integer> indices = new HashMap<>();
        int tileCounter = 0;
        for (Path tilePath : tilePaths) {
            BufferedImage tile = ImageIO.read(tilePath.toFile());
            if (tile == null) {
                continue;
            }

            int tx = (tileCounter % TILES_PER_ROW) * TexturedVoxelAtlas.TILE_SIZE;
            int ty = (tileCounter / TILES_PER_ROW) * TexturedVoxelAtlas.TILE_SIZE;
            g2d.drawImage(tile, tx, ty, TexturedVoxelAtlas.TILE_SIZE, TexturedVoxelAtlas.TILE_SIZE, null);
            indices.put(tilePath.getFileName().toString(), tileCounter);
            tileCounter++;
        }

        g2d.dispose();
        return new AtlasBuild(atlas, indices);
    }

    private int uploadAtlasAndBind(BufferedImage atlasImage) {
        try {
            Class<?> gl11 = Class.forName("org.lwjgl.opengl.GL11");

            int texture2D = gl11.getField("GL_TEXTURE_2D").getInt(null);
            int rgba = gl11.getField("GL_RGBA").getInt(null);
            int unsignedByte = gl11.getField("GL_UNSIGNED_BYTE").getInt(null);

            Method glGenTextures = gl11.getMethod("glGenTextures");
            Method glBindTexture = gl11.getMethod("glBindTexture", int.class, int.class);
            Method glTexImage2D = gl11.getMethod(
                    "glTexImage2D",
                    int.class, int.class, int.class, int.class, int.class,
                    int.class, int.class, int.class, ByteBuffer.class
            );

            int generatedId = (int) glGenTextures.invoke(null);
            glBindTexture.invoke(null, texture2D, generatedId);

            ByteBuffer pixels = imageToRgba(atlasImage);
            glTexImage2D.invoke(
                    null,
                    texture2D,
                    0,
                    rgba,
                    atlasImage.getWidth(),
                    atlasImage.getHeight(),
                    0,
                    rgba,
                    unsignedByte,
                    pixels
            );

            try {
                Class<?> gl30 = Class.forName("org.lwjgl.opengl.GL30");
                Method glGenerateMipmap = gl30.getMethod("glGenerateMipmap", int.class);
                glGenerateMipmap.invoke(null, texture2D);
            } catch (Exception ignored) {
                // Optional in this engine; filtering fallback still works.
            }

            return generatedId;
        } catch (Exception e) {
            return 0;
        }
    }

    private void enableTexturing() {
        try {
            Class<?> gl11 = Class.forName("org.lwjgl.opengl.GL11");
            int texture2D = gl11.getField("GL_TEXTURE_2D").getInt(null);
            gl11.getMethod("glEnable", int.class).invoke(null, texture2D);
        } catch (Exception ignored) {
            // Safe in JavaFX/non-LWJGL contexts.
        }
    }

    private ByteBuffer imageToRgba(BufferedImage image) {
        int[] argb = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        ByteBuffer buffer = ByteBuffer
                .allocateDirect(image.getWidth() * image.getHeight() * 4)
                .order(ByteOrder.nativeOrder());

        for (int pixel : argb) {
            buffer.put((byte) ((pixel >> 16) & 0xFF));
            buffer.put((byte) ((pixel >> 8) & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) ((pixel >> 24) & 0xFF));
        }

        buffer.flip();
        return buffer;
    }

    private void invokeBeginTriangles() {
        try {
            Class<?> gl11 = Class.forName("org.lwjgl.opengl.GL11");
            int triangles = gl11.getField("GL_TRIANGLES").getInt(null);
            gl11.getMethod("glBegin", int.class).invoke(null, triangles);
        } catch (Exception ignored) {
        }
    }

    private void invokeEnd() {
        try {
            Class<?> gl11 = Class.forName("org.lwjgl.opengl.GL11");
            gl11.getMethod("glEnd").invoke(null);
        } catch (Exception ignored) {
        }
    }

    private void invokeGlColor3f(float r, float g, float b) {
        try {
            Class<?> gl11 = Class.forName("org.lwjgl.opengl.GL11");
            gl11.getMethod("glColor3f", float.class, float.class, float.class).invoke(null, r, g, b);
        } catch (Exception ignored) {
        }
    }

    private void invokeGlTexCoord2f(float u, float v) {
        try {
            Class<?> gl11 = Class.forName("org.lwjgl.opengl.GL11");
            gl11.getMethod("glTexCoord2f", float.class, float.class).invoke(null, u, v);
        } catch (Exception ignored) {
        }
    }

    private void invokeGlVertex3f(float x, float y, float z) {
        try {
            Class<?> gl11 = Class.forName("org.lwjgl.opengl.GL11");
            gl11.getMethod("glVertex3f", float.class, float.class, float.class).invoke(null, x, y, z);
        } catch (Exception ignored) {
        }
    }

    private record AtlasBuild(BufferedImage image, Map<String, Integer> tileIndexByName) {
    }
}
