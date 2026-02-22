# Minecraft-style Texture Standard

- **Resolution:** Every block face must be exactly **16x16** pixels.
- **No mixels:** Do not mix 16x16 with other resolutions in the same pack.
- **Palette guidance:** Use hue-shifted shadows (slightly cooler/purple shadows) instead of only darker values.
- **Filtering:** Rendering code must use nearest-neighbor / point filtering (`GraphicsContext#setImageSmoothing(false)`).
- **Atlas-first:** Keep textures in a single atlas where possible.
- **Runtime UV mapping:** Compute atlas UVs from `BlockType.atlasId` and face (top/side/bottom) at runtime.

## AI texture-generation prompt template

"Professional 16x16 pixel art texture for a voxel game, [BLOCK TYPE], top-down view, seamless tiling, limited color palette, vibrant hue shifting, sharp edges, no blur, Minecraft aesthetic, high contrast."
