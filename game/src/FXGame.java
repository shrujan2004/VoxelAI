import graphics.FirstPersonRenderer;
import graphics.TexturePack;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;
import ui.HudRenderer;
import world.ChunkWorldIO;

public class FXGame extends Application {
    private final FXGameState state = new FXGameState();

    private FXGameUpdateSystem updateSystem;
    private FXGameRenderSystem renderSystem;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(FXGameState.WIDTH, FXGameState.HEIGHT);
        GraphicsContext g = canvas.getGraphicsContext2D();
        Scene scene = new Scene(new javafx.scene.layout.StackPane(canvas));

        FirstPersonRenderer firstPersonRenderer = new FirstPersonRenderer(FXGameState.WIDTH, FXGameState.HEIGHT);
        HudRenderer hudRenderer = new HudRenderer(FXGameState.WIDTH, FXGameState.HEIGHT);
        TexturePack textures = FXGameAssets.loadTexturePack();

        state.maleArm = FXGameAssets.loadMaleArm();
        ChunkWorldIO.load(state.world);

        updateSystem = new FXGameUpdateSystem(firstPersonRenderer);
        renderSystem = new FXGameRenderSystem(firstPersonRenderer, hudRenderer, textures);

        new FXGameInputHandler().bind(scene, state);

        stage.setTitle("VoxelAI - Phase 2/3/4 Upgrades");
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
                dt = Math.min(dt, 0.05);

                updateSystem.update(state, dt);
                renderSystem.render(g, state);
                last = now;
            }
        }.start();
    }

    public static void main(String[] args) {
        launch();
    }
}
