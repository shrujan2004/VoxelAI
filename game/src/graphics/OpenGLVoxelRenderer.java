package graphics;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * OpenGL textured voxel renderer helper focused on per-tile PNG texture loading.
 *
 * Flow:
 * 1) loadTileTextures(Paths.get("game/tiles"));
 * 2) beginTexturedRenderLoop();
 * 3) bindTileTexture("grass_top.png");
 * 4) drawFaceImmediate(vertices, uvs);
 */
public final class OpenGLVoxelRenderer {

    private final Map<String, Integer> textureIdsByTile = new HashMap<>();

    /**
     * Loads each 16x16 PNG from game/tiles and uploads it as its own GL texture.
     */
    public void loadTileTextures(Path tilesDir) throws IOException {
        textureIdsByTile.clear();

        try (var stream = Files.list(tilesDir)) {
            stream
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".png"))
                    .sorted((a, b) -> a.getFileName().toString().compareTo(b.getFileName().toString()))
                    .forEach(path -> {
                        try {
                            BufferedImage image = ImageIO.read(path.toFile());
                            if (image == null) {
                                return;
                            }
                            if (image.getWidth() <= 0 || image.getHeight() <= 0) {
                                return;
                            }

                            int textureId = uploadTexture(image);
                            if (textureId != 0) {
                                textureIdsByTile.put(path.getFileName().toString(), textureId);
                            }
                        } catch (IOException ignored) {
                            // Skip unreadable textures; continue loading remaining files.
                        }
                    });
        }
    }

    /**
     * Enables GL_TEXTURE_2D and neutralizes tinting color (white).
     */
    public void beginTexturedRenderLoop() {
        invokeEnableTexture2d();
        invokeGlColor3f(1.0f, 1.0f, 1.0f);
    }

    /**
     * Binds a previously loaded tile texture by filename (e.g. grass_top.png).
     */
    public boolean bindTileTexture(String tileFileName) {
        Integer textureId = textureIdsByTile.get(tileFileName);
        if (textureId == null) {
            return false;
        }
        return invokeBindTexture2d(textureId);
    }

    /**
     * Emits glTexCoord2f(u,v) immediately before glVertex3f(x,y,z).
     */
    public void drawFaceImmediate(float[][] vertices, float[] uvs) {
        invokeGlColor3f(1.0f, 1.0f, 1.0f);
        invokeBeginQuads();

        int vertexCount = Math.min(vertices.length, 4);
        for (int i = 0; i < vertexCount; i++) {
            int uvIndex = i * 2;
            invokeGlTexCoord2f(uvs[uvIndex], uvs[uvIndex + 1]);
            float[] v = vertices[i];
            invokeGlVertex3f(v[0], v[1], v[2]);
        }

        invokeEnd();
    }

    /**
     * [u0,v0, u1,v0, u1,v1, u0,v1] for full tile face.
     */
    public float[] unitFaceUVs() {
        return new float[]{0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f};
    }


    /**
     * Fixed-function pointer setup for packed MeshBuilder vertices.
     * Layout: position(3), uv(2), normal(3), ao(1) => stride 36 bytes.
     */
    public void configureFixedFunctionPointers() {
        try {
            Class<?> gl11 = Class.forName("org.lwjgl.opengl.GL11");
            int glFloat = gl11.getField("GL_FLOAT").getInt(null);

            Method vertexPointer = gl11.getMethod("glVertexPointer", int.class, int.class, int.class, long.class);
            Method texCoordPointer = gl11.getMethod("glTexCoordPointer", int.class, int.class, int.class, long.class);
            Method normalPointer = gl11.getMethod("glNormalPointer", int.class, int.class, long.class);

            int stride = MeshBuilder.STRIDE_BYTES;
            vertexPointer.invoke(null, MeshBuilder.POSITION_FLOATS, glFloat, stride, 0L);
            texCoordPointer.invoke(null, MeshBuilder.UV_FLOATS, glFloat, stride, (long) MeshBuilder.POSITION_FLOATS * Float.BYTES);
            normalPointer.invoke(null, glFloat, stride, (long) (MeshBuilder.POSITION_FLOATS + MeshBuilder.UV_FLOATS) * Float.BYTES);
        } catch (Exception ignored) {
            // No-op outside LWJGL runtime.
        }
    }

    public static String voxelVertexShader() {
        return "#version 330 core\n" +
                "layout(location=0) in vec3 a_Position;\n" +
                "layout(location=1) in vec2 a_TexCoord;\n" +
                "layout(location=2) in vec3 a_Normal;\n" +
                "layout(location=3) in float a_AoBrightness;\n" +
                "out vec2 v_TexCoord;\n" +
                "out float v_AoBrightness;\n" +
                "uniform mat4 u_MVP;\n" +
                "void main(){\n" +
                "    v_TexCoord = a_TexCoord;\n" +
                "    v_AoBrightness = a_AoBrightness;\n" +
                "    gl_Position = u_MVP * vec4(a_Position, 1.0);\n" +
                "}\n";
    }

    public static String voxelFragmentShader() {
        return "#version 330 core\n" +
                "in vec2 v_TexCoord;\n" +
                "in float v_AoBrightness;\n" +
                "uniform sampler2D u_Texture;\n" +
                "out vec4 fragColor;\n" +
                "void main(){\n" +
                "    vec4 texColor = texture(u_Texture, v_TexCoord);\n" +
                "    fragColor = vec4(texColor.rgb * v_AoBrightness, texColor.a);\n" +
                "}\n";
    }

    private int uploadTexture(BufferedImage image) {
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

            int textureId = (int) glGenTextures.invoke(null);
            glBindTexture.invoke(null, texture2D, textureId);

            ByteBuffer pixels = imageToRgba(image);
            glTexImage2D.invoke(
                    null,
                    texture2D,
                    0,
                    rgba,
                    image.getWidth(),
                    image.getHeight(),
                    0,
                    rgba,
                    unsignedByte,
                    pixels
            );

            OpenGLTextureAtlasUtil.applyNearestNeighborFiltering(textureId);
            return textureId;
        } catch (Exception ignored) {
            return 0;
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

    private void invokeEnableTexture2d() {
        try {
            Class<?> gl11 = Class.forName("org.lwjgl.opengl.GL11");
            int texture2D = gl11.getField("GL_TEXTURE_2D").getInt(null);
            gl11.getMethod("glEnable", int.class).invoke(null, texture2D);
        } catch (Exception ignored) {
        }
    }

    private boolean invokeBindTexture2d(int textureId) {
        try {
            Class<?> gl11 = Class.forName("org.lwjgl.opengl.GL11");
            int texture2D = gl11.getField("GL_TEXTURE_2D").getInt(null);
            gl11.getMethod("glBindTexture", int.class, int.class).invoke(null, texture2D, textureId);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void invokeBeginQuads() {
        try {
            Class<?> gl11 = Class.forName("org.lwjgl.opengl.GL11");
            int quads = gl11.getField("GL_QUADS").getInt(null);
            gl11.getMethod("glBegin", int.class).invoke(null, quads);
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
}
