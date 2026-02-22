import graphics.FirstPersonRenderer;
import graphics.TexturePack;
import javafx.scene.canvas.GraphicsContext;
import ui.HudRenderer;

final class FXGameRenderSystem {
    private final FirstPersonRenderer firstPersonRenderer;
    private final HudRenderer hudRenderer;
    private final TexturePack textures;

    FXGameRenderSystem(FirstPersonRenderer firstPersonRenderer, HudRenderer hudRenderer, TexturePack textures) {
        this.firstPersonRenderer = firstPersonRenderer;
        this.hudRenderer = hudRenderer;
        this.textures = textures;
    }

    void render(GraphicsContext g, FXGameState state) {
        double viewBob = state.player.onGround
                ? Math.sin(state.walkTime) * Math.min(0.06, Math.hypot(state.player.velocityX, state.player.velocityZ) * 0.01)
                : 0;

        firstPersonRenderer.render(g, state.world, state.player, state.player.yaw, state.player.pitch, textures, viewBob);

        hudRenderer.renderCrosshair(g);
        hudRenderer.renderHotbar(g, state.hotbar, state.selectedSlot, textures, state.inventory);
        hudRenderer.renderPlayerHand(g, state.maleArm, state.walkTime);
        hudRenderer.renderTerrainMiniView(g, state.world, state.player, 20, FXGameState.HEIGHT - 260, 320, 220);
        hudRenderer.renderStats(g, state.player, state.player.yaw, state.input.sprint, state.targetHit,
                state.hotbar[state.selectedSlot], state.miningSystem.progress());
    }
}
