package graphics;

import engine.Player;
import engine.RaycastHit;
import engine.Raycaster;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import world.BlockType;
import world.ChunkWorld;

public class FirstPersonRenderer {

    private final int width;
    private final int height;

    public FirstPersonRenderer(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public RaycastHit render(GraphicsContext g, ChunkWorld world, Player player, double yaw, double pitch, TexturePack textures, double viewBob) {
        renderSkyGradient(g);

        double horizon = height / 2.0 + viewBob * 28.0;
        g.setFill(Color.web("#4f8f3f"));
        g.fillRect(0, horizon, width, height - horizon);

        double fov = Math.toRadians(75);
        for (int x = 0; x < width; x++) {
            double cameraX = (2.0 * x / width) - 1.0;
            double rayYaw = yaw + cameraX * (fov / 2.0);

            double dx = Math.sin(rayYaw);
            double dz = -Math.cos(rayYaw);
            double dy = pitch;

            RaycastHit hit = Raycaster.raycast(world, player.x, player.eyeY() + viewBob, player.z, dx, dy, dz, 40);
            if (hit == null) continue;

            BlockType block = world.getBlock(hit.x, hit.y, hit.z);
            if (block == BlockType.AIR) continue;

            double corrected = hit.distance * Math.cos(rayYaw - yaw);
            double columnHeight = Math.min(height, height / Math.max(0.08, corrected * 0.5));
            double y0 = horizon - columnHeight / 2.0;

            drawColumn(g, textures, block, world, player, hit, dx, dy, dz, x, y0, columnHeight);

            double faceShade = BlockVisuals.shadeForFace(hit.faceX, hit.faceY, hit.faceZ);
            if (faceShade < 1.0) {
                g.setFill(Color.color(0, 0, 0, 1.0 - faceShade));
                g.fillRect(x, y0, 1, columnHeight);
            }

            applyFog(g, x, y0, columnHeight, hit.distance);
        }

        return Raycaster.raycast(
                world,
                player.x, player.eyeY() + viewBob, player.z,
                Math.sin(yaw), pitch, -Math.cos(yaw),
                5.0
        );
    }

    public RaycastHit renderTargetOnly(ChunkWorld world, Player player, double yaw, double pitch) {
        return Raycaster.raycast(
                world,
                player.x, player.eyeY(), player.z,
                Math.sin(yaw), pitch, -Math.cos(yaw),
                5.0
        );
    }

    private void drawColumn(
            GraphicsContext g,
            TexturePack textures,
            BlockType block,
            ChunkWorld world,
            Player player,
            RaycastHit hit,
            double dx,
            double dy,
            double dz,
            int screenX,
            double screenY,
            double screenHeight
    ) {
        Image atlas = textures.atlas();
        if (atlas != null) {
            int tile = textures.atlasIndex(block);
            int tx = sampleTextureX(player, dx, dy, dz, hit.distance, hit.faceX, hit.faceY, hit.faceZ);
            int sx = (tile % TexturePack.ATLAS_COLS) * TexturePack.TILE_SIZE + tx;
            int sy = (tile / TexturePack.ATLAS_COLS) * TexturePack.TILE_SIZE;
            g.drawImage(atlas, sx, sy, 1, TexturePack.TILE_SIZE, screenX, screenY, 1, screenHeight);
            return;
        }

        Image tileImage = textures.tileForFace(block, hit.faceY);
        if (tileImage != null) {
            int tx = sampleTextureX(player, dx, dy, dz, hit.distance, hit.faceX, hit.faceY, hit.faceZ);
            g.drawImage(tileImage, tx, 0, 1, TexturePack.TILE_SIZE, screenX, screenY, 1, screenHeight);
            return;
        }

        g.setFill(BlockVisuals.colorForBlock(block, world.getSurfaceHeight(hit.x, hit.z)));
        g.fillRect(screenX, screenY, 1, screenHeight);
    }

    private void renderSkyGradient(GraphicsContext g) {
        for (int y = 0; y < height / 2; y++) {
            double t = y / (height / 2.0);
            Color c = Color.color(
                    0.36 + 0.20 * (1 - t),
                    0.60 + 0.25 * (1 - t),
                    0.90 + 0.08 * (1 - t)
            );
            g.setFill(c);
            g.fillRect(0, y, width, 1);
        }
    }

    private void applyFog(GraphicsContext g, int x, double y0, double h, double dist) {
        double fogStart = 14.0;
        double fogEnd = 36.0;
        if (dist <= fogStart) return;
        double t = Math.min(1.0, (dist - fogStart) / (fogEnd - fogStart));
        g.setFill(Color.color(0.78, 0.87, 1.0, t * 0.75));
        g.fillRect(x, y0, 1, h);
    }

    private int sampleTextureX(Player player, double dx, double dy, double dz, double dist, int faceX, int faceY, int faceZ) {
        double hx = player.x + dx * dist;
        double hy = player.eyeY() + dy * dist;
        double hz = player.z + dz * dist;

        double frac;
        if (Math.abs(faceX) == 1) {
            frac = hz - Math.floor(hz);
        } else if (Math.abs(faceZ) == 1) {
            frac = hx - Math.floor(hx);
        } else {
            frac = hy - Math.floor(hy);
        }
        return Math.max(0, Math.min(TexturePack.TILE_SIZE - 1, (int) Math.floor(frac * TexturePack.TILE_SIZE)));
    }
}
