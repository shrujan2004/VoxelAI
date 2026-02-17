import javafx.application.Application;
import javafx.animation.AnimationTimer;
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

    // Player position
    private double px = 10;
    private double py = 1;
    private double pz = 10;

    private double vy = 0;
    private boolean onGround = true;

    private static final double GRAVITY = 0.6;
    private static final double JUMP_POWER = 10;
    private static final double SPEED = 0.15;

    // Movement flags
    private boolean w, a, s, d;

    // Command system
    private boolean commandMode = false;
    private TextField commandBox;

    private final ChunkWorld world = new ChunkWorld();
    private GraphicsContext g;
    private Canvas canvas;

    @Override
    public void start(Stage stage) {

        canvas = new Canvas(VIEW * TILE, VIEW * TILE);
        g = canvas.getGraphicsContext2D();

        commandBox = new TextField();
        commandBox.setPromptText("Type command…");
        commandBox.setVisible(false);
        commandBox.setMaxWidth(300);

        StackPane root = new StackPane(canvas, commandBox);
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("VoxelAI");
        stage.show();

        // SINGLE, CORRECT INPUT HANDLER
        scene.setOnKeyPressed(e -> {

            // COMMAND MODE HANDLING
            if (commandMode) {
                if (e.getCode() == KeyCode.ESCAPE) {
                    exitCommandMode();
                }
                return;
            }

            switch (e.getCode()) {
                case W -> w = true;
                case S -> s = true;
                case A -> a = true;
                case D -> d = true;

                case SPACE -> {
                    if (onGround) {
                        vy = JUMP_POWER;
                        onGround = false;
                    }
                }

                case SLASH -> enterCommandMode();
            }
        });

        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case W -> w = false;
                case S -> s = false;
                case A -> a = false;
                case D -> d = false;
            }
        });

        commandBox.setOnAction(e -> {
            // (Command execution later)
            exitCommandMode();
        });

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        }.start();
    }

    private void enterCommandMode() {
        commandMode = true;
        commandBox.setVisible(true);
        commandBox.clear();
        commandBox.requestFocus();
    }

    private void exitCommandMode() {
        commandMode = false;
        commandBox.setVisible(false);
        commandBox.clear();
        canvas.requestFocus(); // CRITICAL
    }

    private void update() {

        double nx = px;
        double nz = pz;

        if (w) nz -= SPEED;
        if (s) nz += SPEED;
        if (a) nx -= SPEED;
        if (d) nx += SPEED;

        // Collision at body height (Y = 1)
        if (!world.isSolid((int) nx, 1, (int) nz)) {
            px = nx;
            pz = nz;
        }

        // Gravity
        vy -= GRAVITY;
        py += vy * 0.1;

        if (py <= 1) {
            py = 1;
            vy = 0;
            onGround = true;
        }
    }

    private void render() {

        g.setFill(Color.BLACK);
        g.fillRect(0, 0, VIEW * TILE, VIEW * TILE);

        int half = VIEW / 2;
        int cx = (int) px;
        int cz = (int) pz;

        // Ground
        for (int dz = -half; dz <= half; dz++) {
            for (int dx = -half; dx <= half; dx++) {

                BlockType b = world.get(cx + dx, 0, cz + dz);
                if (b != BlockType.AIR) {
                    g.setFill(Color.GREEN);
                    g.fillRect(
                        (dx + half) * TILE,
                        (dz + half) * TILE,
                        TILE,
                        TILE
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

        // HUD
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Consolas", 18));
        g.fillText(
            "X: " + (int) px +
            "  Y: " + String.format("%.2f", py) +
            "  Z: " + (int) pz,
            10,
            22
        );
    }

    public static void main(String[] args) {
        launch();
    }
}
