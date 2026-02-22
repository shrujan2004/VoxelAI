package gameplay;

import world.BlockType;

public class MiningSystem {

    private int targetX = Integer.MIN_VALUE;
    private int targetY = Integer.MIN_VALUE;
    private int targetZ = Integer.MIN_VALUE;
    private double progress = 0;

    public boolean tickBreak(BlockType block, int x, int y, int z, double dt) {
        if (block == null || block == BlockType.AIR || block == BlockType.WATER) {
            reset();
            return false;
        }

        if (x != targetX || y != targetY || z != targetZ) {
            targetX = x;
            targetY = y;
            targetZ = z;
            progress = 0;
        }

        double speed = 1.0 / Math.max(0.1, block.hardness);
        progress += dt * speed;
        if (progress >= 1.0) {
            reset();
            return true;
        }
        return false;
    }

    public void reset() {
        progress = 0;
        targetX = Integer.MIN_VALUE;
        targetY = Integer.MIN_VALUE;
        targetZ = Integer.MIN_VALUE;
    }

    public double progress() {
        return progress;
    }
}
