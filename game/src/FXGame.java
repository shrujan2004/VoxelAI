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
    private static final double FIXED_TIMESTEP = 1.0 / 120.0;

    private final FXGameState state = new FXGameState();

    private FXGameUpdateSystem updateSystem;
    private FXGameRenderSystem renderSystem;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(FXGameState.WIDTH, FXGameState.HEIGHT);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setImageSmoothing(false);
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
            double accumulator = 0.0;

            @Override
            public void handle(long now) {
                if (last == 0) {
                    last = now;
                    return;
                }

                double frameDt = (now - last) / 1_000_000_000.0;
                frameDt = Math.min(frameDt, 0.05);
                last = now;

                accumulator += frameDt;
                while (accumulator >= FIXED_TIMESTEP) {
                    updateSystem.update(state, FIXED_TIMESTEP);
                    accumulator -= FIXED_TIMESTEP;
                }

                double alpha = Math.max(0.0, Math.min(1.0, accumulator / FIXED_TIMESTEP));
                renderSystem.render(g, state, alpha);
            }
        }.start();
    }

    public static void main(String[] args) {
        launch();
    }
}
