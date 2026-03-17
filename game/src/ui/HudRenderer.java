package ui;

import engine.Player;
import engine.RaycastHit;
import gameplay.Inventory;
import graphics.BlockVisuals;
import graphics.TexturePack;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import world.BlockType;
import world.ChunkWorld;

public class HudRenderer {

    private final int width;
    private final int height;

    public HudRenderer(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void renderCrosshair(GraphicsContext g) {
        double cx = width / 2.0;
        double cy = height / 2.0;
        g.setStroke(Color.WHITE);
        g.setLineWidth(2);
        g.strokeLine(cx - 8, cy, cx + 8, cy);
        g.strokeLine(cx, cy - 8, cx, cy + 8);
    }

    public void renderHotbar(GraphicsContext g, BlockType[] hotbar, int selectedSlot, TexturePack textures, Inventory inventory) {
        g.setImageSmoothing(false);
        double slotW = 52;
        double slotH = 52;
        double totalW = slotW * hotbar.length;
        double startX = (width - totalW) / 2.0;
        double y = height - 78;

        for (int i = 0; i < hotbar.length; i++) {
            boolean selected = i == selectedSlot;
            g.setFill(selected ? Color.rgb(255, 255, 255, 0.35) : Color.rgb(0, 0, 0, 0.45));
            g.fillRoundRect(startX + i * slotW, y, slotW - 4, slotH, 8, 8);

            g.setStroke(selected ? Color.GOLD : Color.DARKGRAY);
            g.setLineWidth(selected ? 3 : 1.5);
            g.strokeRoundRect(startX + i * slotW, y, slotW - 4, slotH, 8, 8);

            drawHotbarIcon(g, textures, hotbar[i], startX + i * slotW + 12, y + 10);

            g.setFill(Color.WHITE);
            g.setFont(Font.font("Consolas", 11));
            g.fillText(Integer.toString(i + 1), startX + i * slotW + 4, y + 47);
            g.fillText("x" + inventory.get(hotbar[i]), startX + i * slotW + 24, y + 47);
        }
    }

    public void renderPlayerHand(GraphicsContext g, Image maleArm, double walkTime) {
        if (maleArm == null) return;

        double bob = Math.sin(walkTime) * 8.0;
        double x = width - 220 + bob * 0.4;
        double y = height - 260 + Math.abs(bob);
        g.drawImage(maleArm, x, y, 170, 220);
    }

    public void renderTerrainMiniView(GraphicsContext g, ChunkWorld world, Player player,
                                      double startX, double startY, double viewWidth, double viewHeight) {
        int range = 12;
        double cellW = viewWidth / (range * 2 + 1);
        double cellH = viewHeight / (range * 2 + 1);

        g.setFill(Color.rgb(0, 0, 0, 0.45));
        g.fillRoundRect(startX - 8, startY - 8, viewWidth + 16, viewHeight + 16, 12, 12);

        int px = (int) Math.floor(player.x);
        int pz = (int) Math.floor(player.z);

        for (int dz = -range; dz <= range; dz++) {
            for (int dx = -range; dx <= range; dx++) {
                int wx = px + dx;
                int wz = pz + dz;
                int topY = world.getSurfaceHeight(wx, wz);
                BlockType top = world.getBlock(wx, topY, wz);

                g.setFill(BlockVisuals.colorForBlock(top, topY));
                g.fillRect(startX + (dx + range) * cellW, startY + (dz + range) * cellH, cellW + 1, cellH + 1);
            }
        }

        double cx = startX + range * cellW;
        double cz = startY + range * cellH;
        g.setFill(Color.RED);
        g.fillOval(cx - 4, cz - 4, 8, 8);
    }

    public void renderStats(GraphicsContext g, Player player, double yaw, boolean sprint, RaycastHit targetHit,
                            BlockType selectedBlock, double breakProgress) {
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Consolas", 17));
        g.fillText(String.format("X %.2f  Y %.2f  Z %.2f  Yaw %.0f°", player.x, player.y, player.z, Math.toDegrees(yaw)), 20, 30);
        g.fillText(String.format("Velocity %.2f  Sprint %s", Math.hypot(player.velocityX, player.velocityZ), sprint ? "ON" : "OFF"), 20, 52);
        g.fillText(String.format("Health %.1f/%.0f", player.health, player.maxHealth), 20, 74);
        g.fillText("Selected: " + selectedBlock.name(), 20, 96);

        if (targetHit != null) {
            g.fillText(String.format("Ray hit: (%d, %d, %d) dist %.2f", targetHit.x, targetHit.y, targetHit.z, targetHit.distance), 20, 118);
        } else {
            g.fillText("Ray hit: none", 20, 118);
        }

        if (breakProgress > 0) {
            g.setFill(Color.rgb(0, 0, 0, 0.55));
            g.fillRoundRect(width / 2.0 - 120, height / 2.0 + 28, 240, 16, 8, 8);
            g.setFill(Color.LIMEGREEN);
            g.fillRoundRect(width / 2.0 - 120, height / 2.0 + 28, 240 * Math.min(1.0, breakProgress), 16, 8, 8);
        }
    }

    private void drawHotbarIcon(GraphicsContext g, TexturePack textures, BlockType type, double x, double y) {
        if (textures.atlas() != null) {
            TexturePack.AtlasUV uv = textures.atlasUvForFace(type, 0);
            if (uv != null) {
                g.drawImage(textures.atlas(), uv.sx(), uv.sy(), TexturePack.TILE_SIZE, TexturePack.TILE_SIZE, x, y, 24, 24);
                return;
            }
        }

        Image tile = textures.tile(type);
        if (tile != null) {
            g.drawImage(tile, x, y, 24, 24);
            return;
        }

        g.setFill(BlockVisuals.colorForBlock(type, 4));
        g.fillRect(x, y, 24, 24);
    }
}
