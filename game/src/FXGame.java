import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import world.*;

public class FXGame extends Application {

    private static final int TILE = 32;
    private static final int VIEW = 15;

    private int px = 0, pz = 0;
    private final ChunkWorld world = new ChunkWorld();

    private boolean commandMode = false;
    private TextField commandBox;
    private GraphicsContext g;

    @Override
    public void start(Stage stage) {

        Canvas canvas = new Canvas(VIEW * TILE, VIEW * TILE);
        g = canvas.getGraphicsContext2D();

        commandBox = new TextField();
        commandBox.setPromptText("Type command (example: set 1 0 WOOD)");
        commandBox.setVisible(false);

        StackPane root = new StackPane(canvas, commandBox);
        Scene scene = new Scene(root);

        // Movement + ESC
        scene.setOnKeyPressed(e -> handleKey(e.getCode()));

        // "/" opens command mode (keyboard-layout safe)
        scene.setOnKeyTyped(e -> {
            if (e.getCharacter().equals("/") && !commandMode) {
                commandMode = true;
                commandBox.setVisible(true);
                commandBox.requestFocus();
                commandBox.clear();
            }
        });

        commandBox.setOnAction(e -> executeCommand());

        stage.setTitle("VoxelAI – Infinite World");
        stage.setScene(scene);
        stage.show();

        render();
    }

    private void handleKey(KeyCode key) {

        // Close command box
        if (key == KeyCode.ESCAPE && commandMode) {
            commandMode = false;
            commandBox.clear();
            commandBox.setVisible(false);
            return;
        }

        // Player movement
        if (!commandMode) {
            switch (key) {
                case W -> pz--;
                case S -> pz++;
                case A -> px--;
                case D -> px++;
            }
            render();
        }
    }

    private void executeCommand() {
        String text = commandBox.getText().trim();
        commandBox.clear();
        commandBox.setVisible(false);
        commandMode = false;

        try {
            String[] p = text.split(" ");

            // set x z BLOCK
            if (p[0].equalsIgnoreCase("set")) {
                int x = Integer.parseInt(p[1]);
                int z = Integer.parseInt(p[2]);
                BlockType b = BlockType.valueOf(p[3].toUpperCase());
                world.setBlock(x, z, b);
            }

        } catch (Exception ignored) {}

        render();
    }

    private void render() {
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, VIEW * TILE, VIEW * TILE);

        int half = VIEW / 2;

        // Draw world
        for (int dz = -half; dz <= half; dz++) {
            for (int dx = -half; dx <= half; dx++) {
                BlockType b = world.getBlock(px + dx, pz + dz);

                g.setFill(colorOf(b));
                g.fillRect(
                        (dx + half) * TILE,
                        (dz + half) * TILE,
                        TILE, TILE
                );
            }
        }

        // Player
        g.setFill(Color.CYAN);
        g.fillOval(half * TILE, half * TILE, TILE, TILE);

        // ===== COORDINATES (TOP-LEFT, MINECRAFT STYLE) =====
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Consolas", 18));
        g.fillText("X: " + px + "   Z: " + pz, 10, 22);
    }

    private Color colorOf(BlockType b) {
        return switch (b) {
            case DIRT -> Color.SADDLEBROWN;
            case WOOD -> Color.GOLDENROD;
            case STONE -> Color.LIGHTGRAY;
            case GLASS -> Color.LIGHTBLUE;
            default -> Color.BLACK;
        };
    }

    public static void main(String[] args) {
        launch();
    }
}
