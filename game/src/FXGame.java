import engine.Gravity;
import engine.Physics;
import javafx.animation.AnimationTimer;
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
import world.BlockType;
import world.ChunkWorld;

public class FXGame extends Application {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    // World & physics
    private final ChunkWorld world = new ChunkWorld();
    private Physics physics;

    // Player state
    private double px = 10;
    private double py = 0;
    private double pz = 10;

    private double velY = 0;
    private boolean onGround = false;

    // Movement flags
    private boolean w, a, s, d;
    private boolean jumpQueued;

    // UI
    private GraphicsContext g;
    private TextField commandBox;
    private boolean commandMode = false;

    @Override
    public void start(Stage stage) {

        physics = new Physics(world);
        py = physics.clampToGround(px, py, pz);

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        g = canvas.getGraphicsContext2D();

        commandBox = new TextField();
        commandBox.setVisible(false);
        commandBox.setPromptText("/set x y z BLOCK");
        commandBox.setMaxWidth(600);

        StackPane root = new StackPane(canvas, commandBox);
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // INPUT
        scene.setOnKeyPressed(e -> onKey(e.getCode(), true));
        scene.setOnKeyReleased(e -> onKey(e.getCode(), false));

        commandBox.setOnAction(e -> executeCommand());

        stage.setTitle("VoxelAI – Stable Physics Test");
        stage.setScene(scene);
        stage.show();

        // GAME LOOP
        new AnimationTimer() {
            long last = 0;

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
            if (pressed && key == KeyCode.ESCAPE) closeCommandMode();
            return;
        }

        switch (key) {
            case W -> w = pressed;
            case A -> a = pressed;
            case S -> s = pressed;
            case D -> d = pressed;
            case SPACE -> {
                if (pressed) jumpQueued = true;
            }
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

        double speed = 6 * dt;

        if (w) pz -= speed;
        if (s) pz += speed;
        if (a) px -= speed;
        if (d) px += speed;

        // Jump
        if (jumpQueued && onGround) {
            velY = Gravity.JUMP_FORCE;
            onGround = false;
        }
        jumpQueued = false;

        // Gravity
        velY = Gravity.apply(velY, dt);
        py += velY * dt;

        // Ground collision
        if (physics.isOnGround(px, py, pz)) {
            py = physics.clampToGround(px, py, pz);
            velY = 0;
            onGround = true;
        }
    }

    private void executeCommand() {
        try {
            String[] p = commandBox.getText().trim().split("\\s+");
            if (p.length >= 5 && p[0].equalsIgnoreCase("/set")) {
                int x = Integer.parseInt(p[1]);
                int y = Integer.parseInt(p[2]);
                int z = Integer.parseInt(p[3]);
                BlockType b = BlockType.valueOf(p[4].toUpperCase());
                world.setBlock(x, y, z, b);
            }
        } catch (Exception e) {
            System.out.println("Invalid command");
        }
        closeCommandMode();
    }

    private void closeCommandMode() {
        commandMode = false;
        commandBox.clear();
        commandBox.setVisible(false);
    }

    private void render() {

        // SKY
        g.setFill(Color.web("#87CEEB"));
        g.fillRect(0, 0, WIDTH, HEIGHT / 2.0);

        // GROUND
        g.setFill(Color.web("#55AA33"));
        g.fillRect(0, HEIGHT / 2.0, WIDTH, HEIGHT / 2.0);

        // PLAYER (debug dot)
        g.setFill(Color.CYAN);
        g.fillOval(
                WIDTH / 2.0 - 6,
                HEIGHT / 2.0 - 6 - (py - physics.clampToGround(px, py, pz)) * 40,
                12,
                12
        );

        // HUD
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Consolas", 18));
        g.fillText(
                String.format("X: %.2f  Y: %.2f  Z: %.2f", px, py, pz),
                16, 26
        );
        g.fillText("WASD move | SPACE jump | / command", 16, 50);
    }

    public static void main(String[] args) {
        launch();
    }
}