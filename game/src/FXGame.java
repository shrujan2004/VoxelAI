import engine.PhysicsEngine;
import engine.Player;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import world.ChunkWorld;

public class FXGame extends Application {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    private final ChunkWorld world = new ChunkWorld();
    private final Player player = new Player(10, 6, 10);

    private boolean w, a, s, d;
    private boolean jumpRequest;

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
            if (e.getCode() == KeyCode.SPACE) jumpRequest = true;
        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.W) w = false;
            if (e.getCode() == KeyCode.S) s = false;
            if (e.getCode() == KeyCode.A) a = false;
            if (e.getCode() == KeyCode.D) d = false;
        });

        stage.setTitle("VoxelAI – Stable Physics Core");
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

        double speed = 5.0 * dt;

        double dx = 0, dz = 0;
        if (w) dz -= speed;
        if (s) dz += speed;
        if (a) dx -= speed;
        if (d) dx += speed;

        player.move(dx, dz, world);

        PhysicsEngine.update(player, world, dt, jumpRequest);
        jumpRequest = false;
    }

    private void render(GraphicsContext g) {

        // Sky
        g.setFill(Color.SKYBLUE);
        g.fillRect(0, 0, WIDTH, HEIGHT / 2);

        // Ground
        g.setFill(Color.GREEN);
        g.fillRect(0, HEIGHT / 2, WIDTH, HEIGHT / 2);

        // Player indicator
        g.setFill(Color.CYAN);
        g.fillOval(WIDTH / 2 - 6, HEIGHT / 2 - 6, 12, 12);

        // HUD
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Consolas", 18));
        g.fillText(
                String.format("X %.2f  Y %.2f  Z %.2f", player.x, player.y, player.z),
                20, 30
        );
        g.fillText("WASD move | SPACE jump | Stable physics", 20, 55);
    }

    public static void main(String[] args) {
        launch();
    }
}