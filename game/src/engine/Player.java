package engine;

import world.ChunkWorld;

public class Player {

    public double x, y, z;

    // REQUIRED by PhysicsEngine (Option A)
    public double velocityY = 0;
    public double velocityX = 0;
    public double velocityZ = 0;
    public boolean onGround = false;

    // Collision box
    public static final double RADIUS = 0.3;
    public static final double HEIGHT = 1.8;

    public Player(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double eyeY() {
        return y + HEIGHT * 0.9;
    }

    /* ================================
       Horizontal movement with collision
       ================================ */
    public void move(double dx, double dz, ChunkWorld world) {
        if (!collides(world, x + dx, y, z)) {
            x += dx;
        }
        if (!collides(world, x, y, z + dz)) {
            z += dz;
        }
    }

    /* ================================
       Collision check
       ================================ */
    private boolean collides(ChunkWorld world, double px, double py, double pz) {

        int minX = (int) Math.floor(px - RADIUS);
        int maxX = (int) Math.floor(px + RADIUS);
        int minY = (int) Math.floor(py);
        int maxY = (int) Math.floor(py + HEIGHT);
        int minZ = (int) Math.floor(pz - RADIUS);
        int maxZ = (int) Math.floor(pz + RADIUS);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (world.isSolid(x, y, z)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
