import engine.PhysicsEngine;
import engine.Player;
import graphics.FirstPersonRenderer;
import world.BlockType;

final class FXGameUpdateSystem {
    private final FirstPersonRenderer firstPersonRenderer;

    FXGameUpdateSystem(FirstPersonRenderer firstPersonRenderer) {
        this.firstPersonRenderer = firstPersonRenderer;
    }

    void update(FXGameState state, double dt) {
        state.player.capturePreviousTransform();
        state.input.updateLookSmoothing(dt);

        state.player.yaw = state.input.yaw;
        state.player.pitch = state.input.pitch;

        PhysicsEngine.updateHorizontal(state.player, state.world, dt, state.input.moveX(), state.input.moveZ(), state.input.sprint);
        PhysicsEngine.update(state.player, state.world, dt, state.input.jumpRequested);
        state.input.jumpRequested = false;

        double speed = Math.hypot(state.player.velocityX, state.player.velocityZ);
        state.walkTime += speed * dt * 8.0;

        state.targetHit = firstPersonRenderer.renderTargetOnly(state.world, state.player, state.player.yaw, state.player.pitch);

        processMining(state, dt);
        processPlacement(state);
        processCrafting(state);
    }

    private void processMining(FXGameState state, double dt) {
        if (state.input.breakHeld && state.targetHit != null) {
            BlockType hitBlock = state.world.getBlock(state.targetHit.x, state.targetHit.y, state.targetHit.z);
            if (state.miningSystem.tickBreak(hitBlock, state.targetHit.x, state.targetHit.y, state.targetHit.z, dt)) {
                state.world.breakBlock(state.targetHit.x, state.targetHit.y, state.targetHit.z);
                state.inventory.add(hitBlock, 1);
            }
        } else {
            state.miningSystem.reset();
        }
    }

    private void processPlacement(FXGameState state) {
        if (state.input.placeRequested && state.targetHit != null) {
            state.input.placeRequested = false;
            placeSelectedBlock(state);
        }
    }

    private void processCrafting(FXGameState state) {
        if (state.input.craftRequested) {
            state.input.craftRequested = false;
            state.craftingSystem.craftStoneFromDirt(state.inventory);
            state.craftingSystem.craftGlassFromSand(state.inventory);
            state.craftingSystem.craftWoodFromGrass(state.inventory);
        }
    }

    private void placeSelectedBlock(FXGameState state) {
        BlockType selected = state.hotbar[state.selectedSlot];
        if (selected == BlockType.AIR || selected == BlockType.WATER) {
            return;
        }
        if (!state.inventory.remove(selected, 1)) {
            return;
        }

        int px = state.targetHit.x - state.targetHit.faceX;
        int py = state.targetHit.y - state.targetHit.faceY;
        int pz = state.targetHit.z - state.targetHit.faceZ;

        if (state.world.getBlock(px, py, pz) == BlockType.AIR && !intersectsPlayer(state.player, px, py, pz)) {
            state.world.setBlock(px, py, pz, selected);
        } else {
            state.inventory.add(selected, 1);
        }
    }

    private boolean intersectsPlayer(Player player, int bx, int by, int bz) {
        double blockMinX = bx;
        double blockMaxX = bx + 1;
        double blockMinY = by;
        double blockMaxY = by + 1;
        double blockMinZ = bz;
        double blockMaxZ = bz + 1;

        double playerMinX = player.x - Player.RADIUS;
        double playerMaxX = player.x + Player.RADIUS;
        double playerMinY = player.y;
        double playerMaxY = player.y + Player.HEIGHT;
        double playerMinZ = player.z - Player.RADIUS;
        double playerMaxZ = player.z + Player.RADIUS;

        return blockMinX < playerMaxX && blockMaxX > playerMinX
                && blockMinY < playerMaxY && blockMaxY > playerMinY
                && blockMinZ < playerMaxZ && blockMaxZ > playerMinZ;
    }
}
