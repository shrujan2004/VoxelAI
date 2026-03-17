package graphics;

/**
 * LWJGL/OpenGL atlas helper for pixel-perfect voxel textures.
 *
 * Usage in your OpenGL setup:
 * <pre>
 *     int textureId = ...
 *     OpenGLTextureAtlasUtil.applyNearestNeighborFiltering(textureId);
 * </pre>
 */
public final class OpenGLTextureAtlasUtil {

    private OpenGLTextureAtlasUtil() {
    }

    /**
     * Sets GL_TEXTURE_MAG_FILTER to GL_NEAREST and GL_TEXTURE_MIN_FILTER to GL_NEAREST_MIPMAP_LINEAR.
     *
     * This method uses reflection to avoid a hard compile-time dependency in non-LWJGL environments.
     */
    public static void applyNearestNeighborFiltering(int textureId) {
        try {
            Class<?> gl11 = Class.forName("org.lwjgl.opengl.GL11");

            int texture2D = gl11.getField("GL_TEXTURE_2D").getInt(null);
            int textureMinFilter = gl11.getField("GL_TEXTURE_MIN_FILTER").getInt(null);
            int textureMagFilter = gl11.getField("GL_TEXTURE_MAG_FILTER").getInt(null);
            int nearest = gl11.getField("GL_NEAREST").getInt(null);
            int nearestMipmapLinear = gl11.getField("GL_NEAREST_MIPMAP_LINEAR").getInt(null);

            gl11.getMethod("glBindTexture", int.class, int.class).invoke(null, texture2D, textureId);
            gl11.getMethod("glTexParameteri", int.class, int.class, int.class).invoke(null, texture2D, textureMinFilter, nearestMipmapLinear);
            gl11.getMethod("glTexParameteri", int.class, int.class, int.class).invoke(null, texture2D, textureMagFilter, nearest);
        } catch (Exception ignored) {
            // Safe no-op when LWJGL is not on classpath (e.g., JavaFX/headless validation container).
        }
    }
}
