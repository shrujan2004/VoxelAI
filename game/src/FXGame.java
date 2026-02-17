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

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 640;
    private static final int RAY_COUNT = 180;
    private static final double MAX_VIEW_DISTANCE = 22;
    private static final double FOV = Math.toRadians(75);

    private double px = 0;
    private double pz = 2;
    private double py = 2;

    private double velocityY = 0;
    private boolean jumpPressed = false;

    private double yaw = Math.PI / 2;

    private final ChunkWorld world = new ChunkWorld();
    private final AIClient aiClient = new AIClient();
    private final AICommandHandler aiHandler = new AICommandHandler(new ChunkCommandExecutor(world));

    // Command system
    private boolean commandMode = false;
    private TextField commandBox;

    private final Map<BlockType, Color> blockColors = new HashMap<>();
    private final Map<String, Image> skinHeads = new HashMap<>();
    private String activeSkin = "male";

    private boolean moveForward;
    private boolean moveBackward;
    private boolean moveLeft;
    private boolean moveRight;

    @Override
    public void start(Stage stage) {
        loadPalette();
        loadSkins();

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        g = canvas.getGraphicsContext2D();

        commandBox = new TextField();
        commandBox.setPromptText("/set x z BLOCK  |  /skin male|female|gnome  |  /ai make a house");
        commandBox.setVisible(false);
        commandBox.setMaxWidth(560);

        StackPane root = new StackPane(canvas, commandBox);
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        scene.setOnMouseMoved(e -> {
            if (!commandMode) {
                double centerX = scene.getWidth() / 2;
                double delta = (e.getSceneX() - centerX) * 0.0025;
                yaw += delta;
            }
        });

        scene.setOnKeyPressed(e -> onKey(e.getCode(), true));
        scene.setOnKeyReleased(e -> onKey(e.getCode(), false));
        commandBox.setOnAction(e -> executeCommand());

        stage.setTitle("VoxelAI - First Person Sandbox");
        stage.setScene(scene);
        stage.show();

        scene.setCursor(Cursor.CROSSHAIR);

        new AnimationTimer() {
            long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                double dt = (now - lastTime) / 1_000_000_000.0;
                update(dt);
                render();
                lastTime = now;
            }
        }.start();
    }

    private void onKey(KeyCode key, boolean pressed) {
        if (commandMode) {
            if (key == KeyCode.ESCAPE && pressed) {
                closeCommandMode();
            }
            return;
        }

        switch (key) {
            case W -> moveForward = pressed;
            case S -> moveBackward = pressed;
            case A -> moveLeft = pressed;
            case D -> moveRight = pressed;
            case SPACE -> jumpPressed = pressed;
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
        double speed = 5.4;
        double moveX = 0;
        double moveZ = 0;

        if (moveForward) {
            moveX += Math.cos(yaw) * speed * dt;
            moveZ += Math.sin(yaw) * speed * dt;
        }
        if (moveBackward) {
            moveX -= Math.cos(yaw) * speed * dt;
            moveZ -= Math.sin(yaw) * speed * dt;
        }
        if (moveLeft) {
            moveX += Math.cos(yaw - Math.PI / 2) * speed * dt;
            moveZ += Math.sin(yaw - Math.PI / 2) * speed * dt;
        }
        if (moveRight) {
            moveX += Math.cos(yaw + Math.PI / 2) * speed * dt;
            moveZ += Math.sin(yaw + Math.PI / 2) * speed * dt;
        }

        double nx = px + moveX;
        double nz = pz + moveZ;
        if (world.isWalkable((int) Math.round(nx), (int) Math.round(nz))) {
            px = nx;
            pz = nz;
        }

        int ground = world.getSurfaceHeight((int) Math.round(px), (int) Math.round(pz)) + 1;
        if (jumpPressed && Math.abs(py - ground) < 0.01) {
            velocityY = 6.5;
        }

        velocityY -= 15.0 * dt;
        py += velocityY * dt;

        if (py < ground) {
            py = ground;
            velocityY = 0;
        }

    private void executeCommand() {
        String command = commandBox.getText().trim();

        try {
            if (command.startsWith("/set")) {
                String[] p = command.split("\\s+");
                int x = Integer.parseInt(p[1]);
                int z = Integer.parseInt(p[2]);
                BlockType block = BlockType.valueOf(p[3].toUpperCase());
                world.setBlock(x, z, block);
            } else if (command.startsWith("/skin")) {
                String[] p = command.split("\\s+");
                if (skinHeads.containsKey(p[1].toLowerCase())) {
                    activeSkin = p[1].toLowerCase();
                }
            } else if (command.startsWith("/ai")) {
                String prompt = command.substring(3).trim();
                String response = aiClient.askAI(prompt);
                if (response != null) {
                    aiHandler.handle(response);
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
        g.setFill(Color.SKYBLUE);
        g.fillRect(0, 0, WIDTH, HEIGHT / 2.0);
        g.setFill(Color.web("#6d523b"));
        g.fillRect(0, HEIGHT / 2.0, WIDTH, HEIGHT / 2.0);

        for (int i = 0; i < RAY_COUNT; i++) {
            double rayAngle = yaw - (FOV / 2.0) + ((double) i / RAY_COUNT) * FOV;
            castRay(i, rayAngle);
        }

        drawHUD();
    }

    private void castRay(int rayIndex, double angle) {
        double step = 0.18;
        double distance = 0;
        int hitHeight = 0;
        BlockType hitBlock = BlockType.GRASS;

        while (distance < MAX_VIEW_DISTANCE) {
            distance += step;
            int sx = (int) Math.floor(px + Math.cos(angle) * distance);
            int sz = (int) Math.floor(pz + Math.sin(angle) * distance);
            hitHeight = world.getSurfaceHeight(sx, sz);
            hitBlock = world.getBlock(sx, sz);

            if (hitHeight + 1 >= py - 0.3) {
                break;
            }
        }

        double corrected = distance * Math.cos(angle - yaw);
        double wallHeight = (HEIGHT * 1.2) / Math.max(0.1, corrected);

        double x = ((double) rayIndex / RAY_COUNT) * WIDTH;
        double stripeWidth = (double) WIDTH / RAY_COUNT + 1;
        double yTop = HEIGHT / 2.0 - wallHeight / 2.0;

        Color base = blockColors.getOrDefault(hitBlock, Color.DARKGRAY);
        double shade = Math.max(0.25, 1.0 - corrected / MAX_VIEW_DISTANCE);
        g.setFill(base.deriveColor(0, 1, shade, 1));
        g.fillRect(x, yTop, stripeWidth, wallHeight);
    }

    private void drawHUD() {
        g.setStroke(Color.WHITE);
        g.strokeLine(WIDTH / 2.0 - 10, HEIGHT / 2.0, WIDTH / 2.0 + 10, HEIGHT / 2.0);
        g.strokeLine(WIDTH / 2.0, HEIGHT / 2.0 - 10, WIDTH / 2.0, HEIGHT / 2.0 + 10);

        g.setFill(Color.color(0, 0, 0, 0.5));
        g.fillRect(14, 14, 350, 110);
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Consolas", 18));
        g.fillText(String.format("XYZ: %.1f / %.1f / %.1f", px, py, pz), 24, 44);
        g.fillText("Skin: " + activeSkin + "  |  / for commands", 24, 70);
        g.fillText("Mouse: look  WASD: move  SPACE: jump", 24, 96);

        Image icon = skinHeads.get(activeSkin);
        if (icon != null) {
            g.drawImage(icon, WIDTH - 92, 16, 72, 72);
        }
    }

    private void loadPalette() {
        blockColors.put(BlockType.GRASS, Color.web("#5fbd3f"));
        blockColors.put(BlockType.DIRT, Color.web("#7f5539"));
        blockColors.put(BlockType.STONE, Color.web("#8d99ae"));
        blockColors.put(BlockType.WOOD, Color.web("#9c6644"));
        blockColors.put(BlockType.GLASS, Color.web("#a8dadc"));
        blockColors.put(BlockType.SAND, Color.web("#e9c46a"));
        blockColors.put(BlockType.WATER, Color.web("#2a9d8f"));
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
