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
     * Sets both GL_TEXTURE_MAG_FILTER and GL_TEXTURE_MIN_FILTER to GL_NEAREST for sharp pixels.
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
            gl11.getMethod("glBindTexture", int.class, int.class).invoke(null, texture2D, textureId);
            gl11.getMethod("glTexParameteri", int.class, int.class, int.class).invoke(null, texture2D, textureMinFilter, nearest);
            gl11.getMethod("glTexParameteri", int.class, int.class, int.class).invoke(null, texture2D, textureMagFilter, nearest);
        } catch (Exception ignored) {
            // Safe no-op when LWJGL is not on classpath (e.g., JavaFX/headless validation container).
        }
    }

    /**
     * Alias kept for renderer initialization code that calls initTextureState(textureId).
     */
    public static void initTextureState(int textureId) {
        applyNearestNeighborFiltering(textureId);
    }
}
