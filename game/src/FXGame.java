import commands.AICommandHandler;
import commands.ChunkCommandExecutor;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
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

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 720;
    private static final int RAY_COUNT = 360;
    private static final double FOV = Math.toRadians(80);
    private static final double MAX_DIST = 40;

    private static final double PLAYER_RADIUS = 0.30;
    private static final double PLAYER_HEIGHT = 1.8;
    private static final double EYE_OFFSET = 1.62;

    private final ChunkWorld world = new ChunkWorld();
    private final AIClient aiClient = new AIClient();
    private final AICommandHandler aiHandler = new AICommandHandler(new ChunkCommandExecutor(world));

    private final Map<BlockType, Color> palette = new HashMap<>();
    private final Map<String, Image> skinHeads = new HashMap<>();

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
        commandBox.setMaxWidth(720);
        commandBox.setPromptText("/set x y z BLOCK | /skin male|female|gnome | /ai build me a house");
        commandBox.setOnAction(e -> executeCommand());

        StackPane root = new StackPane(canvas, commandBox);
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        scene.setOnKeyPressed(e -> onKey(e.getCode(), true));
        scene.setOnKeyReleased(e -> onKey(e.getCode(), false));

        scene.setOnMouseMoved(e -> {
            if (commandMode) {
                return;
            }
            double cx = scene.getWidth() * 0.5;
            double cy = scene.getHeight() * 0.5;
            yaw += (e.getSceneX() - cx) * 0.0022;
            pitch += (e.getSceneY() - cy) * 0.0018;
            pitch = Math.max(-0.6, Math.min(0.6, pitch));
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

        if (py < -10) {
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
        return collides(px, py - 0.05, pz);
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
        double horizon = HEIGHT / 2.0 + pitch * 260;

        g.setFill(Color.web("#87ceeb"));
        g.fillRect(0, 0, WIDTH, horizon);
        g.setFill(Color.web("#8b6b4a"));
        g.fillRect(0, horizon, WIDTH, HEIGHT - horizon);

        for (int i = 0; i < RAY_COUNT; i++) {
            double cameraX = (2.0 * i / RAY_COUNT - 1.0) * Math.tan(FOV / 2.0);
            double rayDirX = Math.cos(yaw) + (-Math.sin(yaw)) * cameraX;
            double rayDirZ = Math.sin(yaw) + Math.cos(yaw) * cameraX;

            double dist = 0;
            BlockType hit = BlockType.AIR;
            while (dist < MAX_DIST) {
                dist += 0.05;
                int sx = (int) Math.floor(px + rayDirX * dist);
                int sy = (int) Math.floor(py + EYE_OFFSET);
                int sz = (int) Math.floor(pz + rayDirZ * dist);
                hit = world.getBlock(sx, sy, sz);
                if (hit.solid) {
                    break;
                }
            }

            if (!hit.solid) {
                continue;
            }

            double corrected = dist * Math.cos(Math.atan(cameraX));
            double wallHeight = (HEIGHT / Math.max(0.1, corrected)) * 0.9;
            double screenX = (double) i / RAY_COUNT * WIDTH;
            double lineW = (double) WIDTH / RAY_COUNT + 1;
            double top = horizon - wallHeight / 2.0;

            Color base = palette.getOrDefault(hit, Color.GRAY);
            double shade = Math.max(0.2, 1.0 - corrected / MAX_DIST);
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
        g.fillRect(12, 12, 460, 120);
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Consolas", 18));
        g.fillText(String.format("XYZ %.2f %.2f %.2f", px, py, pz), 24, 42);
        g.fillText("WASD move | SPACE jump | SHIFT sprint | / command", 24, 70);
        g.fillText("Skin: " + activeSkin + " | AI: /ai build me a 7 by 7 house", 24, 98);

        Image icon = skinHeads.get(activeSkin);
        if (icon != null) {
            g.drawImage(icon, WIDTH - 94, 16, 78, 78);
        }
    }

    private void loadPalette() {
        palette.put(BlockType.GRASS, Color.web("#6ab04c"));
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
}
