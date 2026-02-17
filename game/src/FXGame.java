import javafx.application.Application;
import javafx.animation.AnimationTimer;
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
import world.*;

import java.util.HashMap;
import java.util.Map;

public class FXGame extends Application {

    private static final int TILE = 32;
    private static final int VIEW = 15;

    // Player position
    private int px = 0;
    private int pz = 2;

    // Visual jump only (NOT world physics)
    private double py = 1.0;
    private double velocityY = 0;
    private boolean jumping = false;

    private static final double GRAVITY = 0.7;
    private static final double JUMP_POWER = 10;

    private final ChunkWorld world = new ChunkWorld();

    private boolean commandMode = false;
    private TextField commandBox;
    private GraphicsContext g;

    private final Map<BlockType, Image> textures = new HashMap<>();

    @Override
    public void start(Stage stage) {

        // Load textures
        for (BlockType b : BlockType.values()) {
            if (b.texture != null) {
                textures.put(b, new Image("file:../tiles/" + b.texture));
            }
        }

        Canvas canvas = new Canvas(VIEW * TILE, VIEW * TILE);
        g = canvas.getGraphicsContext2D();

        commandBox = new TextField();
        commandBox.setPromptText("set x z BLOCK");
        commandBox.setVisible(false);

        StackPane root = new StackPane(canvas, commandBox);
        Scene scene = new Scene(root);

        scene.setOnKeyPressed(e -> handleKey(e.getCode()));
        scene.setOnKeyTyped(e -> {
            if (e.getCharacter().equals("/") && !commandMode) {
                commandMode = true;
                commandBox.setVisible(true);
                commandBox.requestFocus();
                commandBox.clear();
            }
        });

        commandBox.setOnAction(e -> executeCommand());

        stage.setTitle("VoxelAI");
        stage.setScene(scene);
        stage.show();

        // Stable game loop
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateJump();
                render();
            }
        }.start();
    }

    private void handleKey(KeyCode key) {

        if (key == KeyCode.ESCAPE && commandMode) {
            commandMode = false;
            commandBox.clear();
            commandBox.setVisible(false);
            return;
        }

        int nx = px;
        int nz = pz;

        if (!commandMode) {
            switch (key) {
                case W -> nz--;
                case S -> nz++;
                case A -> nx--;
                case D -> nx++;
                case SPACE -> {
                    if (!jumping) {
                        velocityY = JUMP_POWER;
                        jumping = true;
                    }
                }
            }

            if (world.isWalkable(nx, nz)) {
                px = nx;
                pz = nz;
            }
        }
    }

    // Jump = visual arc only (NO falling through world)
    private void updateJump() {
        if (jumping) {
            py += velocityY * 0.1;
            velocityY -= GRAVITY;

            if (py <= 1.0) {
                py = 1.0;
                velocityY = 0;
                jumping = false;
            }
        }
    }

    private void executeCommand() {
        try {
            String[] p = commandBox.getText().split(" ");
            int x = Integer.parseInt(p[1]);
            int z = Integer.parseInt(p[2]);
            BlockType b = BlockType.valueOf(p[3].toUpperCase());
            world.setBlock(x, z, b);
        } catch (Exception ignored) {}

        commandBox.clear();
        commandBox.setVisible(false);
        commandMode = false;
    }

    private void render() {
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, VIEW * TILE, VIEW * TILE);

        int half = VIEW / 2;

        for (int dz = -half; dz <= half; dz++) {
            for (int dx = -half; dx <= half; dx++) {
                BlockType b = world.getBlock(px + dx, pz + dz);
                if (b != BlockType.AIR) {
                    g.drawImage(
                        textures.get(b),
                        (dx + half) * TILE,
                        (dz + half) * TILE
                    );
                }
            }
        }

        // Player
        g.setFill(Color.CYAN);
        g.fillOval(
            half * TILE,
            half * TILE - (py - 1) * TILE,
            TILE,
            TILE
        );

        // HUD (Minecraft-style)
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Consolas", 18));
        g.fillText(
            "X: " + px + "  Y: " + String.format("%.2f", py) + "  Z: " + pz,
            10,
            22
        );
    }

    public static void main(String[] args) {
        launch();
    }
}
