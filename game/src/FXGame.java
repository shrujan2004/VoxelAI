import javafx.application.Application;
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

    private int px = 0, pz = 0;
    private final ChunkWorld world = new ChunkWorld();

    private boolean commandMode = false;
    private TextField commandBox;
    private GraphicsContext g;

    private final Map<BlockType, Image> textures = new HashMap<>();

    @Override
    public void start(Stage stage) {

        // 🔹 LOAD TEXTURES (CORRECT PATH)
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

        // MOVEMENT
        scene.setOnKeyPressed(e -> handleKey(e.getCode()));

        // "/" OPENS COMMAND MODE
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

        render();
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
            }

            if (world.isWalkable(nx, nz)) {
                px = nx;
                pz = nz;
            }
            render();
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
        render();
    }

    private void render() {
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, VIEW * TILE, VIEW * TILE);

        int half = VIEW / 2;

        // DRAW WORLD
        for (int dz = -half; dz <= half; dz++) {
            for (int dx = -half; dx <= half; dx++) {
                BlockType b = world.getBlock(px + dx, pz + dz);
                if (b != BlockType.AIR) {
                    g.drawImage(
                            textures.get(b),
                            (dx + half) * TILE,
                            (dz + half) * TILE,
                            TILE, TILE
                    );
                }
            }
        }

        // PLAYER
        g.setFill(Color.CYAN);
        g.fillOval(half * TILE, half * TILE, TILE, TILE);

        // COORDINATES
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Consolas", 18));
        g.fillText("X: " + px + "   Z: " + pz, 10, 22);
    }

    public static void main(String[] args) {
        launch();
    }
}
