# Minecraft-style Texture Standard

- **World units:** 1 block is exactly **1.0 x 1.0 x 1.0** units.
- **Resolution:** Every block face must be exactly **16x16** pixels.
- **Magic ratio:** **16 pixels : 1 block unit**.
- **No mixels:** Do not mix 16x16 with other resolutions in the same pack.
- **Palette guidance:** Use hue-shifted shadows (slightly cooler/purple shadows) instead of only darker values.
- **Filtering:** Rendering code must use nearest-neighbor / point filtering (`GraphicsContext#setImageSmoothing(false)`).
- **Atlas-first:** Keep textures in a single atlas where possible.
- **Runtime UV mapping:** Compute atlas UVs from `BlockType.atlasId` and face (top/side/bottom) at runtime.

## AI texture-generation prompt templates

1) "Professional 16x16 pixel art texture for a voxel game block, [Material Name], seamless tiling on all axes, 1:1 aspect ratio. Use a limited 8-bit color palette with hue shifting (e.g., purples in shadows, yellows in highlights). Ensure sharp nearest-neighbor edges, zero blur, and no baked-in 3D lighting or shadows. Output should look like a flat, clean asset from a high-quality Minecraft texture pack."

2) "Texture sheet for a 3D voxel game, [Material], 16x16 pixel art resolution, perfectly tileable/seamless on all sides, flat lighting, limited color palette with vibrant hue shifting, sharp pixel edges, no 3D shadows or blur, high contrast, top-down perspective, clean grid layout."
