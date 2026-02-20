package engine;

import world.ChunkWorld;

public class Player {

    public double x, y, z;
    public double velY;

    public double yaw;
    public double pitch;

    public static final double RADIUS = 0.3;
    public static final double HEIGHT = 1.8;

    private static final double GRAVITY = 18.0;
    private static final double JUMP_FORCE = 7.0;

    public Player(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = Math.PI / 2;
    }

    public void update(double dt, ChunkWorld world, boolean jump) {

        velY -= GRAVITY * dt;
        y += velY * dt;

        if (collides(world)) {
            if (velY < 0) {
                while (collides(world)) y += 0.01;
            } else {
                while (collides(world)) y -= 0.01;
            }
            velY = 0;
        }

        if (jump && isGrounded(world)) {
            velY = JUMP_FORCE;
        }
    }

    public void move(double dx, double dz, ChunkWorld world) {
        if (!collides(world, x + dx, y, z)) x += dx;
        if (!collides(world, x, y, z + dz)) z += dz;
    }

    public boolean isGrounded(ChunkWorld world) {
        return collides(world, x, y - 0.05, z);
    }

    private boolean collides(ChunkWorld world) {
        return collides(world, x, y, z);
    }

    private boolean collides(ChunkWorld world, double px, double py, double pz) {
        int minX = (int) Math.floor(px - RADIUS);
        int maxX = (int) Math.floor(px + RADIUS);
        int minY = (int) Math.floor(py);
        int maxY = (int) Math.floor(py + HEIGHT);
        int minZ = (int) Math.floor(pz - RADIUS);
        int maxZ = (int) Math.floor(pz + RADIUS);

        for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                for (int z = minZ; z <= maxZ; z++)
                    if (world.isSolid(x, y, z))
                        return true;

        return false;
    }
}
