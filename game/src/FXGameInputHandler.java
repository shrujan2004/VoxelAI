import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import world.ChunkWorldIO;

final class FXGameInputHandler {
    void bind(Scene scene, FXGameState state) {
        scene.setOnKeyPressed(e -> onKeyPressed(e.getCode(), state));
        scene.setOnKeyReleased(e -> onKeyReleased(e.getCode(), state));
    }

    private void onKeyPressed(KeyCode code, FXGameState state) {
        if (code == KeyCode.W) state.input.forward = true;
        if (code == KeyCode.S) state.input.back = true;
        if (code == KeyCode.A) state.input.left = true;
        if (code == KeyCode.D) state.input.right = true;
        if (code == KeyCode.SHIFT) state.input.sprint = true;
        if (code == KeyCode.LEFT) state.input.turnLeft();
        if (code == KeyCode.RIGHT) state.input.turnRight();
        if (code == KeyCode.UP) state.input.lookUp();
        if (code == KeyCode.DOWN) state.input.lookDown();
        if (code == KeyCode.SPACE) state.input.jumpRequested = true;

        if (code == KeyCode.F) state.input.breakHeld = true;
        if (code == KeyCode.R) state.input.placeRequested = true;
        if (code == KeyCode.C) state.input.craftRequested = true;
        if (code == KeyCode.F5) ChunkWorldIO.save(state.world);
        if (code == KeyCode.F9) ChunkWorldIO.load(state.world);

        if (code.isDigitKey()) {
            String name = code.getName();
            if (name.length() == 1) {
                int idx = Integer.parseInt(name) - 1;
                if (idx >= 0 && idx < state.hotbar.length) {
                    state.selectedSlot = idx;
                }
            }
        }
    }

    private void onKeyReleased(KeyCode code, FXGameState state) {
        if (code == KeyCode.W) state.input.forward = false;
        if (code == KeyCode.S) state.input.back = false;
        if (code == KeyCode.A) state.input.left = false;
        if (code == KeyCode.D) state.input.right = false;
        if (code == KeyCode.SHIFT) state.input.sprint = false;
        if (code == KeyCode.F) state.input.breakHeld = false;
    }
}
