import graphics.FirstPersonRenderer;
import graphics.OpenGLVoxelRenderer;
import graphics.TexturePack;
import javafx.scene.canvas.GraphicsContext;
import ui.HudRenderer;

import java.nio.file.Path;

final class FXGameRenderSystem {
    private final FirstPersonRenderer firstPersonRenderer;
    private final HudRenderer hudRenderer;
    private final TexturePack textures;
    private final OpenGLVoxelRenderer openGLVoxelRenderer;

    FXGameRenderSystem(FirstPersonRenderer firstPersonRenderer, HudRenderer hudRenderer, TexturePack textures) {
        this.firstPersonRenderer = firstPersonRenderer;
        this.hudRenderer = hudRenderer;
        this.textures = textures;
        this.openGLVoxelRenderer = new OpenGLVoxelRenderer();
        try {
            this.openGLVoxelRenderer.loadTileTextures(Path.of("game/tiles"));
        } catch (Exception ignored) {
            // JavaFX software path can still render with TexturePack images if OpenGL bridge is unavailable.
        }
    }

    void render(GraphicsContext g, FXGameState state, double alpha) {
        // OpenGL bridge state wiring (safe no-op outside LWJGL runtime).
        openGLVoxelRenderer.beginTexturedRenderLoop();
        openGLVoxelRenderer.bindTileTexture("grass_top.png");
        openGLVoxelRenderer.configureFixedFunctionPointers();

        double viewBob = state.player.onGround
                ? Math.sin(state.walkTime) * Math.min(0.06, Math.hypot(state.player.velocityX, state.player.velocityZ) * 0.01)
                : 0;

        firstPersonRenderer.render(g, state.world, state.player, state.player.yaw, state.player.pitch, textures, viewBob, state.input.sprint, alpha);

        hudRenderer.renderCrosshair(g);
        hudRenderer.renderHotbar(g, state.hotbar, state.selectedSlot, textures, state.inventory);
        hudRenderer.renderPlayerHand(g, state.maleArm, state.walkTime);
        hudRenderer.renderTerrainMiniView(g, state.world, state.player, 20, FXGameState.HEIGHT - 260, 320, 220);
        hudRenderer.renderStats(g, state.player, state.player.yaw, state.input.sprint, state.targetHit,
                state.hotbar[state.selectedSlot], state.miningSystem.progress());
    }
}
