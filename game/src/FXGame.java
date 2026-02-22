import engine.PhysicsEngine;
import engine.Player;
import engine.RaycastHit;
import engine.Raycaster;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.image.Image;
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

    private double yaw = 0;
    private RaycastHit targetHit;

    private Image maleHead;
    private Image maleBody;
    private Image maleArm;
    private Image maleLeg;

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
            if (e.getCode() == KeyCode.LEFT) yaw -= Math.toRadians(8);
            if (e.getCode() == KeyCode.RIGHT) yaw += Math.toRadians(8);
            if (e.getCode() == KeyCode.SPACE) jumpRequest = true;
        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.W) w = false;
            if (e.getCode() == KeyCode.S) s = false;
            if (e.getCode() == KeyCode.A) a = false;
            if (e.getCode() == KeyCode.D) d = false;
        });

        loadMaleSkinPack();

        stage.setTitle("VoxelAI – Raycaster + Male Skin + Physics");
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

        double forwardX = Math.sin(yaw);
        double forwardZ = -Math.cos(yaw);
        double rightX = Math.cos(yaw);
        double rightZ = Math.sin(yaw);

        double dx = 0, dz = 0;
        if (w) {
            dx += forwardX * speed;
            dz += forwardZ * speed;
        }
        if (s) {
            dx -= forwardX * speed;
            dz -= forwardZ * speed;
        }
        if (a) {
            dx -= rightX * speed;
            dz -= rightZ * speed;
        }
        if (d) {
            dx += rightX * speed;
            dz += rightZ * speed;
        }

        player.move(dx, dz, world);

        PhysicsEngine.update(player, world, dt, jumpRequest);
        jumpRequest = false;

        targetHit = Raycaster.raycast(
                world,
                player.x, player.eyeY(), player.z,
                Math.sin(yaw), 0.0, -Math.cos(yaw),
                6.0
        );
    }

    private void render(GraphicsContext g) {

        // Sky
        g.setFill(Color.SKYBLUE);
        g.fillRect(0, 0, WIDTH, HEIGHT / 2);

        // Ground
        g.setFill(Color.GREEN);
        g.fillRect(0, HEIGHT / 2, WIDTH, HEIGHT / 2);

        // Player indicator / skin preview
        if (maleHead != null && maleBody != null) {
            double cx = WIDTH / 2.0;
            double cy = HEIGHT / 2.0;
            g.drawImage(maleHead, cx - 16, cy - 52, 32, 32);
            g.drawImage(maleBody, cx - 16, cy - 20, 32, 32);
            if (maleArm != null) {
                g.drawImage(maleArm, cx - 34, cy - 20, 18, 32);
                g.drawImage(maleArm, cx + 16, cy - 20, 18, 32);
            }
            if (maleLeg != null) {
                g.drawImage(maleLeg, cx - 16, cy + 10, 14, 28);
                g.drawImage(maleLeg, cx + 2, cy + 10, 14, 28);
            }
        } else {
            g.setFill(Color.CYAN);
            g.fillOval(WIDTH / 2 - 6, HEIGHT / 2 - 6, 12, 12);
        }

        // HUD
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Consolas", 18));
        g.fillText(
                String.format("X %.2f  Y %.2f  Z %.2f  Yaw %.0f°", player.x, player.y, player.z, Math.toDegrees(yaw)),
                20, 30
        );
        if (targetHit != null) {
            g.fillText(
                    String.format("Ray hit: (%d, %d, %d) dist %.2f", targetHit.x, targetHit.y, targetHit.z, targetHit.distance),
                    20, 55
            );
        } else {
            g.fillText("Ray hit: none", 20, 55);
        }
        g.fillText("WASD move | LEFT/RIGHT turn | SPACE jump", 20, 80);
    }


    private void loadMaleSkinPack() {
        maleHead = loadImage("game/Player male/male_head.png");
        maleBody = loadImage("game/Player male/male_body.png");
        maleArm = loadImage("game/Player male/male_arm.png");
        maleLeg = loadImage("game/Player male/male_leg.png");
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