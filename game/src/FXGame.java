import commands.AICommandHandler;
import commands.ChunkCommandExecutor;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import world.BlockType;
import world.ChunkWorld;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FXGame extends Application {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int RAY_COUNT = 420;
    private static final double FOV = Math.toRadians(82);
    private static final double MAX_DIST = 55;

    private static final double PLAYER_RADIUS = 0.30;
    private static final double PLAYER_HEIGHT = 1.8;
    private static final double EYE_OFFSET = 1.62;

    private static final double INTERACT_DISTANCE = 7.0;

    private final ChunkWorld world = new ChunkWorld();
    private final AIClient aiClient = new AIClient();
    private final AICommandHandler aiHandler = new AICommandHandler(new ChunkCommandExecutor(world));

    private final Map<BlockType, Color> palette = new HashMap<>();
    private final Map<String, Image> skinHeads = new HashMap<>();

    private final BlockType[] hotbar = {
            BlockType.GRASS,
            BlockType.DIRT,
            BlockType.STONE,
            BlockType.WOOD,
            BlockType.SAND,
            BlockType.GLASS,
            BlockType.WATER,
            BlockType.AIR,
            BlockType.AIR
    };

    private int selectedSlot = 0;

    private GraphicsContext g;
    private TextField commandBox;

    private boolean commandMode;

    private double px = 0;
    private double py = 10;
    private double pz = 0;

    private double velY;
    private boolean jumpQueued;

    private boolean moveForward;
    private boolean moveBackward;
    private boolean moveLeft;
    private boolean moveRight;
    private boolean sprint;

    private double yaw = Math.PI / 2;
    private double pitch = 0;

    private String activeSkin = "male";

    @Override
    public void start(Stage stage) {
        loadPalette();
        loadSkins();
        py = world.getSurfaceHeight((int) px, (int) pz) + 1.1;

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        g = canvas.getGraphicsContext2D();

        commandBox = new TextField();
        commandBox.setVisible(false);
        commandBox.setMaxWidth(760);
        commandBox.setPromptText("/set x y z BLOCK | /skin male|female|gnome | /ai build me a house");
        commandBox.setOnAction(e -> executeCommand());

        StackPane root = new StackPane(canvas, commandBox);
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        scene.setOnKeyPressed(e -> onKey(e.getCode(), true));
        scene.setOnKeyReleased(e -> onKey(e.getCode(), false));
        scene.setOnMousePressed(e -> onMousePressed(e.getButton()));
        scene.setOnScroll(e -> {
            if (e.getDeltaY() < 0) {
                selectedSlot = (selectedSlot + 1) % hotbar.length;
            } else {
                selectedSlot = (selectedSlot - 1 + hotbar.length) % hotbar.length;
            }
            double cx = scene.getWidth() * 0.5;
            double cy = scene.getHeight() * 0.5;
            yaw += (e.getSceneX() - cx) * 0.0022;
            pitch += (e.getSceneY() - cy) * 0.0018;
            pitch = Math.max(-0.6, Math.min(0.6, pitch));
        });

        scene.setOnMouseMoved(e -> {
            if (commandMode) {
                return;
            }
            double cx = scene.getWidth() * 0.5;
            double cy = scene.getHeight() * 0.5;
            yaw += (e.getSceneX() - cx) * 0.0022;
            pitch += (e.getSceneY() - cy) * 0.0018;
            pitch = Math.max(-0.62, Math.min(0.62, pitch));
        });

        stage.setTitle("VoxelAI - Minecraft Style FPV");
        stage.setScene(scene);
        stage.show();

        scene.setCursor(Cursor.CROSSHAIR);

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
                render();
                last = now;
            }
        }.start();
    }

    private void onKey(KeyCode key, boolean pressed) {
        if (commandMode) {
            if (pressed && key == KeyCode.ESCAPE) {
                closeCommandMode();
            }
            return;
        }

        switch (key) {
            case W -> moveForward = pressed;
            case S -> moveBackward = pressed;
            case A -> moveLeft = pressed;
            case D -> moveRight = pressed;
            case SHIFT -> sprint = pressed;
            case SPACE -> jumpQueued = pressed;
            case DIGIT1 -> selectedSlot = 0;
            case DIGIT2 -> selectedSlot = 1;
            case DIGIT3 -> selectedSlot = 2;
            case DIGIT4 -> selectedSlot = 3;
            case DIGIT5 -> selectedSlot = 4;
            case DIGIT6 -> selectedSlot = 5;
            case DIGIT7 -> selectedSlot = 6;
            case DIGIT8 -> selectedSlot = 7;
            case DIGIT9 -> selectedSlot = 8;
            case SLASH -> {
                if (pressed) {
                    commandMode = true;
                    commandBox.setVisible(true);
                    commandBox.requestFocus();
                    commandBox.clear();
                }
            }
        }
    }

    private void onMousePressed(MouseButton button) {
        if (commandMode) {
            return;
        }

        RaycastHit hit = raycastFromPlayer(INTERACT_DISTANCE);
        if (hit == null) {
            return;
        }

        if (button == MouseButton.PRIMARY) {
            world.setBlock(hit.x, hit.y, hit.z, BlockType.AIR);
        } else if (button == MouseButton.SECONDARY) {
            BlockType placing = hotbar[selectedSlot];
            if (placing == BlockType.AIR) {
                return;
            }

            int placeX = hit.x - hit.faceX;
            int placeY = hit.y - hit.faceY;
            int placeZ = hit.z - hit.faceZ;

            if (blockIntersectsPlayer(placeX, placeY, placeZ)) {
                return;
            }
        }
    }

    private void update(double dt) {
        double moveSpeed = sprint ? 7.3 : 4.6;
        double dx = 0;
        double dz = 0;

        if (moveForward) {
            dx += Math.cos(yaw) * moveSpeed * dt;
            dz += Math.sin(yaw) * moveSpeed * dt;
        }
        if (moveBackward) {
            dx -= Math.cos(yaw) * moveSpeed * dt;
            dz -= Math.sin(yaw) * moveSpeed * dt;
        }
        if (moveLeft) {
            dx += Math.cos(yaw - Math.PI / 2) * moveSpeed * dt;
            dz += Math.sin(yaw - Math.PI / 2) * moveSpeed * dt;
        }
        if (moveRight) {
            dx += Math.cos(yaw + Math.PI / 2) * moveSpeed * dt;
            dz += Math.sin(yaw + Math.PI / 2) * moveSpeed * dt;
        }

        moveWithCollision(dx, 0);
        moveWithCollision(0, dz);

        boolean grounded = isGrounded();
        if (jumpQueued && grounded) {
            velY = 6.8;
        }

            world.setBlock(placeX, placeY, placeZ, placing);
        }
    }

    private boolean blockIntersectsPlayer(int bx, int by, int bz) {
        int minX = (int) Math.floor(px - PLAYER_RADIUS);
        int maxX = (int) Math.floor(px + PLAYER_RADIUS);
        int minY = (int) Math.floor(py);
        int maxY = (int) Math.floor(py + PLAYER_HEIGHT);
        int minZ = (int) Math.floor(pz - PLAYER_RADIUS);
        int maxZ = (int) Math.floor(pz + PLAYER_RADIUS);
        return bx >= minX && bx <= maxX && by >= minY && by <= maxY && bz >= minZ && bz <= maxZ;
    }

    private void update(double dt) {
        double moveSpeed = sprint ? 7.3 : 4.6;
        double dx = 0;
        double dz = 0;

        if (moveForward) {
            dx += Math.cos(yaw) * moveSpeed * dt;
            dz += Math.sin(yaw) * moveSpeed * dt;
        }
        if (moveBackward) {
            dx -= Math.cos(yaw) * moveSpeed * dt;
            dz -= Math.sin(yaw) * moveSpeed * dt;
        }
        if (moveLeft) {
            dx += Math.cos(yaw - Math.PI / 2) * moveSpeed * dt;
            dz += Math.sin(yaw - Math.PI / 2) * moveSpeed * dt;
        }
        if (moveRight) {
            dx += Math.cos(yaw + Math.PI / 2) * moveSpeed * dt;
            dz += Math.sin(yaw + Math.PI / 2) * moveSpeed * dt;
        }

        moveWithCollision(dx, 0);
        moveWithCollision(0, dz);

        boolean grounded = isGrounded();
        if (jumpQueued && grounded) {
            velY = 6.8;
        }

        velY -= 17 * dt;
        py += velY * dt;

        if (collides(px, py, pz)) {
            if (velY < 0) {
                while (collides(px, py, pz)) {
                    py += 0.01;
                }
            } else {
                while (collides(px, py, pz)) {
                    py -= 0.01;
                }
            }
            velY = 0;
        }

        if (py < -20) {
            px = 0;
            pz = 0;
            py = world.getSurfaceHeight(0, 0) + 1.1;
            velY = 0;
        }
    }

    private void moveWithCollision(double dx, double dz) {
        double nx = px + dx;
        double nz = pz + dz;
        if (!collides(nx, py, nz)) {
            px = nx;
            pz = nz;
        }
    }

    private boolean isGrounded() {
        return collides(px, py - 0.06, pz);
    }

    private boolean collides(double x, double y, double z) {
        int minX = (int) Math.floor(x - PLAYER_RADIUS);
        int maxX = (int) Math.floor(x + PLAYER_RADIUS);
        int minY = (int) Math.floor(y);
        int maxY = (int) Math.floor(y + PLAYER_HEIGHT);
        int minZ = (int) Math.floor(z - PLAYER_RADIUS);
        int maxZ = (int) Math.floor(z + PLAYER_RADIUS);

        for (int bx = minX; bx <= maxX; bx++) {
            for (int by = minY; by <= maxY; by++) {
                for (int bz = minZ; bz <= maxZ; bz++) {
                    if (world.isSolid(bx, by, bz)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private RaycastHit raycastFromPlayer(double maxDistance) {
        double originX = px;
        double originY = py + EYE_OFFSET;
        double originZ = pz;

        double dirX = Math.cos(yaw) * Math.cos(pitch);
        double dirY = -Math.sin(pitch);
        double dirZ = Math.sin(yaw) * Math.cos(pitch);

        double distance = 0;
        int previousX = (int) Math.floor(originX);
        int previousY = (int) Math.floor(originY);
        int previousZ = (int) Math.floor(originZ);

        while (distance < maxDistance) {
            distance += 0.05;
            double sx = originX + dirX * distance;
            double sy = originY + dirY * distance;
            double sz = originZ + dirZ * distance;

            int bx = (int) Math.floor(sx);
            int by = (int) Math.floor(sy);
            int bz = (int) Math.floor(sz);

            if (world.isSolid(bx, by, bz)) {
                int fx = bx - previousX;
                int fy = by - previousY;
                int fz = bz - previousZ;
                return new RaycastHit(bx, by, bz, fx, fy, fz, distance);
            }

            previousX = bx;
            previousY = by;
            previousZ = bz;
        }
        return null;
    }

    private void executeCommand() {
        String command = commandBox.getText().trim();
        try {
            if (command.startsWith("/set")) {
                String[] parts = command.split("\\s+");
                if (parts.length >= 5) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int z = Integer.parseInt(parts[3]);
                    BlockType b = BlockType.valueOf(parts[4].toUpperCase());
                    world.setBlock(x, y, z, b);
                }
            } else if (command.startsWith("/skin")) {
                String[] parts = command.split("\\s+");
                String name = parts[1].toLowerCase();
                if (skinHeads.containsKey(name)) {
                    activeSkin = name;
                }
            } else if (command.startsWith("/ai")) {
                String prompt = command.substring(3).trim();
                if (!prompt.isBlank()) {
                    String json = aiClient.askAI(prompt);
                    if (json != null) {
                        aiHandler.handle(json);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("⚠️ Invalid command: " + command);
        }

        closeCommandMode();
    }

    private void closeCommandMode() {
        commandMode = false;
        commandBox.clear();
        commandBox.setVisible(false);
    }

    private void render() {
        double horizon = HEIGHT / 2.0 + pitch * 320;

        g.setFill(Color.web("#7dc7ff"));
        g.fillRect(0, 0, WIDTH, horizon);

        g.setFill(Color.web("#7d5f40"));
        g.fillRect(0, horizon, WIDTH, HEIGHT - horizon);

        for (int i = 0; i < RAY_COUNT; i++) {
            double cameraX = (2.0 * i / RAY_COUNT - 1.0) * Math.tan(FOV / 2.0);
            double rayDirX = Math.cos(yaw) + (-Math.sin(yaw)) * cameraX;
            double rayDirZ = Math.sin(yaw) + Math.cos(yaw) * cameraX;

            double dist = 0;
            BlockType hit = BlockType.AIR;
            int hitY = 0;
            while (dist < MAX_DIST) {
                dist += 0.045;
                int sx = (int) Math.floor(px + rayDirX * dist);
                int sz = (int) Math.floor(pz + rayDirZ * dist);

                int eyeY = (int) Math.floor(py + EYE_OFFSET);
                hit = world.getBlock(sx, eyeY, sz);
                hitY = eyeY;

                if (hit.solid) {
                    break;
                }

                for (int down = eyeY - 1; down >= eyeY - 2; down--) {
                    BlockType lower = world.getBlock(sx, down, sz);
                    if (lower.solid) {
                        hit = lower;
                        hitY = down;
                        break;
                    }
                }

                if (hit.solid) {
                    break;
                }
            }

            if (!hit.solid) {
                continue;
            }

            double corrected = dist * Math.cos(Math.atan(cameraX));
            double verticalOffset = ((py + EYE_OFFSET) - (hitY + 0.5)) / Math.max(0.001, corrected);
            double wallHeight = (HEIGHT / Math.max(0.12, corrected)) * 0.95;

            double screenX = (double) i / RAY_COUNT * WIDTH;
            double lineW = (double) WIDTH / RAY_COUNT + 1;
            double top = (horizon - wallHeight / 2.0) + verticalOffset * 260;

            Color base = palette.getOrDefault(hit, Color.GRAY);
            double shade = Math.max(0.18, 1.0 - corrected / MAX_DIST);
            g.setFill(base.deriveColor(0, 1, shade, 1));
            g.fillRect(screenX, top, lineW, wallHeight);
        }

        drawHUD();
    }

    private void drawHUD() {
        g.setStroke(Color.WHITE);
        g.strokeLine(WIDTH / 2.0 - 8, HEIGHT / 2.0, WIDTH / 2.0 + 8, HEIGHT / 2.0);
        g.strokeLine(WIDTH / 2.0, HEIGHT / 2.0 - 8, WIDTH / 2.0, HEIGHT / 2.0 + 8);

        g.setFill(Color.color(0, 0, 0, 0.45));
        g.fillRect(12, 12, 560, 138);
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Consolas", 18));
        g.fillText(String.format("XYZ %.2f %.2f %.2f", px, py, pz), 24, 40);
        g.fillText("WASD move | SPACE jump | SHIFT sprint | LMB break | RMB place", 24, 66);
        g.fillText("1-9 / wheel switch inventory | Skin: " + activeSkin + " | /ai <prompt>", 24, 92);

        BlockType selected = hotbar[selectedSlot];
        g.fillText("Selected: " + selected.name(), 24, 118);

        drawHotbar();

        Image icon = skinHeads.get(activeSkin);
        if (icon != null) {
            g.drawImage(icon, WIDTH - 102, 16, 84, 84);
        }
    }

    private void drawHotbar() {
        int slots = hotbar.length;
        double slotSize = 56;
        double pad = 6;
        double totalWidth = slots * slotSize + (slots - 1) * pad;
        double startX = (WIDTH - totalWidth) / 2.0;
        double y = HEIGHT - 78;

        for (int i = 0; i < slots; i++) {
            BlockType block = hotbar[i];
            Color fill = palette.getOrDefault(block, Color.color(0.2, 0.2, 0.2));

            if (block == BlockType.AIR) {
                fill = Color.color(0.1, 0.1, 0.1, 0.7);
            }

            double x = startX + i * (slotSize + pad);
            g.setFill(Color.color(0, 0, 0, 0.55));
            g.fillRoundRect(x, y, slotSize, slotSize, 8, 8);
            g.setFill(fill);
            g.fillRoundRect(x + 6, y + 6, slotSize - 12, slotSize - 12, 6, 6);

            g.setStroke(i == selectedSlot ? Color.GOLD : Color.WHITE);
            g.setLineWidth(i == selectedSlot ? 3 : 1.2);
            g.strokeRoundRect(x, y, slotSize, slotSize, 8, 8);

            g.setFill(Color.WHITE);
            g.setFont(Font.font("Consolas", 13));
            g.fillText(String.valueOf(i + 1), x + 4, y + 14);
        }
    }

    private void loadPalette() {
        palette.put(BlockType.GRASS, Color.web("#67b14b"));
        palette.put(BlockType.DIRT, Color.web("#8e5b3a"));
        palette.put(BlockType.STONE, Color.web("#9aa0a6"));
        palette.put(BlockType.WOOD, Color.web("#ad7f49"));
        palette.put(BlockType.SAND, Color.web("#e9d27a"));
        palette.put(BlockType.GLASS, Color.web("#b9e8ff"));
        palette.put(BlockType.WATER, Color.web("#4aa3df"));
    }

    private void loadSkins() {
        registerSkin("male", "../Player male/male_head.png");
        registerSkin("female", "../Player female/female_head.png");
        registerSkin("gnome", "../Gnome/gnome_head.png");
    }

    private void registerSkin(String key, String relativePath) {
        File file = new File(relativePath);
        if (file.exists()) {
            skinHeads.put(key, new Image(file.toURI().toString()));
        }
    }

    public static void main(String[] args) {
        launch();
    }

    private static class RaycastHit {
        final int x;
        final int y;
        final int z;
        final int faceX;
        final int faceY;
        final int faceZ;
        final double distance;

        RaycastHit(int x, int y, int z, int faceX, int faceY, int faceZ, double distance) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.faceX = faceX;
            this.faceY = faceY;
            this.faceZ = faceZ;
            this.distance = distance;
        }
    }
}
