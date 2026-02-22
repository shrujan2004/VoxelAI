import engine.PhysicsEngine;
import engine.Player;
import engine.RaycastHit;
import graphics.FirstPersonRenderer;
import graphics.HudRenderer;
import graphics.TexturePack;
import input.PlayerInputState;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import world.BlockType;
import world.ChunkWorld;

public class FXGame extends Application {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    private final ChunkWorld world = new ChunkWorld();
    private final Player player = new Player(10, 7, 10);

    private final PlayerInputState input = new PlayerInputState();

    private final BlockType[] hotbar = {
            BlockType.GRASS, BlockType.DIRT, BlockType.STONE,
            BlockType.SAND, BlockType.WOOD, BlockType.GLASS,
            BlockType.WATER, BlockType.GRASS, BlockType.STONE
    };

    private int selectedSlot = 0;
    private double walkTime = 0;
    private RaycastHit targetHit;

    private Image maleArm;
    private TexturePack textures;

    private FirstPersonRenderer firstPersonRenderer;
    private HudRenderer hudRenderer;

    private double yaw = 0;
    private double walkTime = 0;
    private RaycastHit targetHit;

    private Image maleArm;
    private Image atlas;

    private static final int TILE_SIZE = 16;
    private static final int ATLAS_COLS = 4;

    private final BlockType[] hotbar = {
            BlockType.GRASS, BlockType.DIRT, BlockType.STONE,
            BlockType.SAND, BlockType.WOOD, BlockType.GLASS,
            BlockType.WATER, BlockType.GRASS, BlockType.STONE
    };
    private int selectedSlot = 0;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext g = canvas.getGraphicsContext2D();
        Scene scene = new Scene(new javafx.scene.layout.StackPane(canvas));

        firstPersonRenderer = new FirstPersonRenderer(WIDTH, HEIGHT);
        hudRenderer = new HudRenderer(WIDTH, HEIGHT);

        maleArm = loadImage("game/Player male/male_arm.png");
        textures = new TexturePack("game/tiles/atlas.png");

        bindInput(scene);

        stage.setTitle("VoxelAI – FPV + Hotbar + Split Architecture");
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

    private void bindInput(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.W) input.forward = true;
            if (e.getCode() == KeyCode.S) input.back = true;
            if (e.getCode() == KeyCode.A) input.left = true;
            if (e.getCode() == KeyCode.D) input.right = true;
            if (e.getCode() == KeyCode.SHIFT) input.sprint = true;
            if (e.getCode() == KeyCode.LEFT) input.turnLeft();
            if (e.getCode() == KeyCode.RIGHT) input.turnRight();
            if (e.getCode() == KeyCode.SPACE) input.jumpRequested = true;

            if (e.getCode().isDigitKey()) {
                String name = e.getCode().getName();
                if (name.length() == 1) {
                    int idx = Integer.parseInt(name) - 1;
                    if (idx >= 0 && idx < hotbar.length) {
                        selectedSlot = idx;
                    }
                }
            }
        });

        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.W) input.forward = false;
            if (e.getCode() == KeyCode.S) input.back = false;
            if (e.getCode() == KeyCode.A) input.left = false;
            if (e.getCode() == KeyCode.D) input.right = false;
            if (e.getCode() == KeyCode.SHIFT) input.sprint = false;
        });
    }

    private void update(double dt) {
        PhysicsEngine.updateHorizontal(player, world, dt, input.moveX(), input.moveZ(), input.sprint);
        PhysicsEngine.update(player, world, dt, input.jumpRequested);
        input.jumpRequested = false;

        double speed = Math.hypot(player.velocityX, player.velocityZ);
        walkTime += speed * dt * 5.5;
    }

    private void render(GraphicsContext g) {
        targetHit = firstPersonRenderer.render(g, world, player, input.yaw, textures);

        hudRenderer.renderCrosshair(g);
        hudRenderer.renderHotbar(g, hotbar, selectedSlot, textures);
        hudRenderer.renderPlayerHand(g, maleArm, walkTime);
        hudRenderer.renderTerrainMiniView(g, world, player, 20, HEIGHT - 260, 320, 220);
        hudRenderer.renderStats(g, player, input.yaw, input.sprint, targetHit);
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
