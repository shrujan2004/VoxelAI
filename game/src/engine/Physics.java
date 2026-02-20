package engine;

import world.ChunkWorld;

public class Physics {

    private final ChunkWorld world;

    public Physics(ChunkWorld world) {
        this.world = world;
    }

    public boolean isOnGround(double x, double y, double z) {
        int groundY = world.getSurfaceHeight((int) x, (int) z);
        return y <= groundY + 1.01;
    }

    public double clampToGround(double x, double y, double z) {
        int groundY = world.getSurfaceHeight((int) x, (int) z);
        return groundY + 1.0;
    }
}
