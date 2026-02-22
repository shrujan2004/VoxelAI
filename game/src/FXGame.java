import engine.PhysicsEngine;
import engine.Player;
import engine.RaycastHit;
import engine.Raycaster;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import world.BlockType;
import world.ChunkWorld;

public class FXGame extends Application {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    private final ChunkWorld world = new ChunkWorld();
    private final Player player = new Player(10, 7, 10);

    private boolean w, a, s, d, sprint;
    private boolean jumpRequest;

    private double yaw = 0;
    private double walkTime = 0;
    private RaycastHit targetHit;

    private Image maleArm;
    private Image atlas;

    private static final int TILE_SIZE = 16;
    private static final int ATLAS_COLS = 4;

    private final BlockType[] hotbar = {
            BlockType.GRASS, BlockType.DIRT, BlockType.STONE,
            BlockType.SAND, BlockType.WOOD, BlockType.GLASS,
            BlockType.WATER, BlockType.GRASS, BlockType.STONE
    };
    private int selectedSlot = 0;

    @Override
    public void start(Stage stage) {

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext g = canvas.getGraphicsContext2D();
        Scene scene = new Scene(new javafx.scene.layout.StackPane(canvas));

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.W) w = true;
            if (e.getCode() == KeyCode.S) s = true;
            if (e.getCode() == KeyCode.A) a = true;
            if (e.getCode() == KeyCode.D) d = true;
            if (e.getCode() == KeyCode.SHIFT) sprint = true;
            if (e.getCode() == KeyCode.LEFT) yaw -= Math.toRadians(6);
            if (e.getCode() == KeyCode.RIGHT) yaw += Math.toRadians(6);
            if (e.getCode() == KeyCode.SPACE) jumpRequest = true;

            if (e.getCode().isDigitKey()) {
                String name = e.getCode().getName();
                if (name.length() == 1) {
                    int idx = Integer.parseInt(name) - 1;
                    if (idx >= 0 && idx < hotbar.length) {
                        selectedSlot = idx;
                    }
                }
            }
        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.W) w = false;
            if (e.getCode() == KeyCode.S) s = false;
            if (e.getCode() == KeyCode.A) a = false;
            if (e.getCode() == KeyCode.D) d = false;
            if (e.getCode() == KeyCode.SHIFT) sprint = false;
        });

        maleArm = loadImage("game/Player male/male_arm.png");
        atlas = loadImage("game/tiles/atlas.png");

        stage.setTitle("VoxelAI – FPV + Atlas + Hotbar");
        stage.setScene(scene);
        stage.show();

        new AnimationTimer() {
            long last;

            @Override
            public void handle(long now) {
                if (last == 0) {
                    last = now;
                    return;
                }
                double dt = (now - last) / 1_000_000_000.0;
                update(dt);
                render(g);
                last = now;
            }
        }.start();
    }

    private void update(double dt) {
        double forwardX = Math.sin(yaw);
        double forwardZ = -Math.cos(yaw);
        double rightX = Math.cos(yaw);
        double rightZ = Math.sin(yaw);

        double moveX = 0, moveZ = 0;
        if (w) {
            moveX += forwardX;
            moveZ += forwardZ;
        }
        if (s) {
            moveX -= forwardX;
            moveZ -= forwardZ;
        }
        if (a) {
            moveX -= rightX;
            moveZ -= rightZ;
        }
        if (d) {
            moveX += rightX;
            moveZ += rightZ;
        }

        PhysicsEngine.updateHorizontal(player, world, dt, moveX, moveZ, sprint);
        PhysicsEngine.update(player, world, dt, jumpRequest);
        jumpRequest = false;

        double speed = Math.sqrt(player.velocityX * player.velocityX + player.velocityZ * player.velocityZ);
        walkTime += speed * dt * 5.5;

        targetHit = Raycaster.raycast(
                world,
                player.x, player.eyeY(), player.z,
                Math.sin(yaw), -0.22, -Math.cos(yaw),
                12.0
        );
    }

    private void render(GraphicsContext g) {
        renderFirstPerson(g);
        renderCrosshair(g);
        renderHotbar(g);
        renderPlayerHand(g);
        renderTerrainMiniView(g, 20, HEIGHT - 260, 320, 220);

        g.setFill(Color.WHITE);
        g.setFont(Font.font("Consolas", 18));
        g.fillText(
                String.format("X %.2f  Y %.2f  Z %.2f  Yaw %.0f°", player.x, player.y, player.z, Math.toDegrees(yaw)),
                20, 30
        );
        g.fillText(
                String.format("Velocity %.2f  Sprint %s", Math.hypot(player.velocityX, player.velocityZ), sprint ? "ON" : "OFF"),
                20, 55
        );
        if (targetHit != null) {
            g.fillText(
                    String.format("Ray hit: (%d, %d, %d) dist %.2f", targetHit.x, targetHit.y, targetHit.z, targetHit.distance),
                    20, 80
            );
        } else {
            g.fillText("Ray hit: none", 20, 80);
        }
    }

    private void renderFirstPerson(GraphicsContext g) {
        g.setFill(Color.SKYBLUE);
        g.fillRect(0, 0, WIDTH, HEIGHT / 2.0);
        g.setFill(Color.web("#4f8f3f"));
        g.fillRect(0, HEIGHT / 2.0, WIDTH, HEIGHT / 2.0);

        double fov = Math.toRadians(75);
        for (int x = 0; x < WIDTH; x++) {
            double cameraX = (2.0 * x / WIDTH) - 1.0;
            double rayYaw = yaw + cameraX * (fov / 2.0);

            double dx = Math.sin(rayYaw);
            double dz = -Math.cos(rayYaw);
            double dy = -0.22;

            RaycastHit hit = Raycaster.raycast(world, player.x, player.eyeY(), player.z, dx, dy, dz, 40);
            if (hit == null) continue;

            BlockType block = world.getBlock(hit.x, hit.y, hit.z);
            if (block == BlockType.AIR) continue;

            double corrected = hit.distance * Math.cos(rayYaw - yaw);
            double columnHeight = Math.min(HEIGHT, HEIGHT / Math.max(0.08, corrected * 0.5));
            double y0 = HEIGHT / 2.0 - columnHeight / 2.0;

            if (atlas != null) {
                int tile = atlasIndex(block);
                int tx = sampleTextureX(dx, dy, dz, hit.distance, hit.faceX, hit.faceY, hit.faceZ);
                int sx = (tile % ATLAS_COLS) * TILE_SIZE + tx;
                int sy = (tile / ATLAS_COLS) * TILE_SIZE;
                g.drawImage(atlas, sx, sy, 1, TILE_SIZE, x, y0, 1, columnHeight);
            } else {
                g.setFill(colorForBlock(block, world.getSurfaceHeight(hit.x, hit.z)));
                g.fillRect(x, y0, 1, columnHeight);
            }

            double shade = shadeForFace(hit.faceX, hit.faceY, hit.faceZ);
            if (shade < 1.0) {
                g.setFill(Color.color(0, 0, 0, 1.0 - shade));
                g.fillRect(x, y0, 1, columnHeight);
            }
        }
    }

    private int sampleTextureX(double dx, double dy, double dz, double dist, int faceX, int faceY, int faceZ) {
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
        return Math.max(0, Math.min(TILE_SIZE - 1, (int) Math.floor(frac * TILE_SIZE)));
    }

    private double shadeForFace(int fx, int fy, int fz) {
        if (fy > 0) return 1.0;     // top face
        if (fy < 0) return 0.55;    // bottom face
        if (Math.abs(fx) == 1) return 0.75;
        if (Math.abs(fz) == 1) return 0.85;
        return 0.8;
    }

    private void renderCrosshair(GraphicsContext g) {
        double cx = WIDTH / 2.0;
        double cy = HEIGHT / 2.0;
        g.setStroke(Color.WHITE);
        g.setLineWidth(2);
        g.strokeLine(cx - 8, cy, cx + 8, cy);
        g.strokeLine(cx, cy - 8, cx, cy + 8);
    }

    private void renderHotbar(GraphicsContext g) {
        double slotW = 52;
        double slotH = 52;
        double totalW = slotW * hotbar.length;
        double startX = (WIDTH - totalW) / 2.0;
        double y = HEIGHT - 78;

        for (int i = 0; i < hotbar.length; i++) {
            boolean selected = i == selectedSlot;
            g.setFill(selected ? Color.rgb(255, 255, 255, 0.35) : Color.rgb(0, 0, 0, 0.45));
            g.fillRoundRect(startX + i * slotW, y, slotW - 4, slotH, 8, 8);

            g.setStroke(selected ? Color.GOLD : Color.DARKGRAY);
            g.setLineWidth(selected ? 3 : 1.5);
            g.strokeRoundRect(startX + i * slotW, y, slotW - 4, slotH, 8, 8);

            if (atlas != null) {
                int idx = atlasIndex(hotbar[i]);
                int sx = (idx % ATLAS_COLS) * TILE_SIZE;
                int sy = (idx / ATLAS_COLS) * TILE_SIZE;
                g.drawImage(atlas, sx, sy, TILE_SIZE, TILE_SIZE,
                        startX + i * slotW + 12, y + 10, 24, 24);
            }

            g.setFill(Color.WHITE);
            g.setFont(Font.font("Consolas", 12));
            g.fillText(Integer.toString(i + 1), startX + i * slotW + 5, y + 47);
        }
    }

    private void renderPlayerHand(GraphicsContext g) {
        if (maleArm == null) return;

        double bob = Math.sin(walkTime) * 8.0;
        double x = WIDTH - 220 + bob * 0.4;
        double y = HEIGHT - 260 + Math.abs(bob);
        g.drawImage(maleArm, x, y, 170, 220);
    }

    private void renderTerrainMiniView(GraphicsContext g, double startX, double startY, double width, double height) {
        int range = 12;
        double cellW = width / (range * 2 + 1);
        double cellH = height / (range * 2 + 1);

        g.setFill(Color.rgb(0, 0, 0, 0.45));
        g.fillRoundRect(startX - 8, startY - 8, width + 16, height + 16, 12, 12);

        int px = (int) Math.floor(player.x);
        int pz = (int) Math.floor(player.z);

        for (int dz = -range; dz <= range; dz++) {
            for (int dx = -range; dx <= range; dx++) {
                int wx = px + dx;
                int wz = pz + dz;
                int topY = world.getSurfaceHeight(wx, wz);
                BlockType top = world.getBlock(wx, topY, wz);

                g.setFill(colorForBlock(top, topY));
                g.fillRect(startX + (dx + range) * cellW, startY + (dz + range) * cellH, cellW + 1, cellH + 1);
            }
        }

        double cx = startX + range * cellW;
        double cz = startY + range * cellH;
        g.setFill(Color.RED);
        g.fillOval(cx - 4, cz - 4, 8, 8);
    }

    private Color colorForBlock(BlockType block, int height) {
        Color base = switch (block) {
            case GRASS -> Color.web("#55aa33");
            case DIRT -> Color.web("#8b5a2b");
            case STONE -> Color.web("#888888");
            case SAND -> Color.web("#d8cf83");
            case WOOD -> Color.web("#a06a3a");
            case GLASS -> Color.web("#99d8ff");
            case WATER -> Color.web("#3b66c5");
            default -> Color.web("#333333");
        };

        double shade = Math.max(0.65, Math.min(1.15, 0.75 + height * 0.06));
        return base.deriveColor(0, 1, shade, 1);
    }

    private int atlasIndex(BlockType type) {
        return switch (type) {
            case GRASS -> 0;
            case DIRT -> 1;
            case STONE -> 2;
            case SAND -> 3;
            case WOOD -> 4;
            case GLASS -> 5;
            case WATER -> 6;
            default -> 1;
        };
    }

    private Image loadImage(String path) {
        try {
            return new Image(java.nio.file.Path.of(path).toUri().toString());
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
