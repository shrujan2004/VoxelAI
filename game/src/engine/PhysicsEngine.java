package engine;

import world.ChunkWorld;

public class PhysicsEngine {

    public static final double GRAVITY = 22.0;
    public static final double JUMP_POWER = 8.5;
    public static final double TERMINAL_VELOCITY = 35.0;
    public static final double AIR_DRAG = 0.985;

    public static void update(Player p, ChunkWorld world, double dt, boolean jumpRequest) {

        // Apply gravity with a terminal velocity cap
        p.velocityY -= GRAVITY * dt;
        if (p.velocityY < -TERMINAL_VELOCITY) {
            p.velocityY = -TERMINAL_VELOCITY;
        }

        // Light air resistance helps keep movement stable over varying frame rates
        p.velocityY *= Math.pow(AIR_DRAG, dt * 60.0);
        p.y += p.velocityY * dt;

        // Collision resolution
        if (collides(p, world)) {
            if (p.velocityY < 0) {
                while (collides(p, world)) p.y += 0.005;
                p.onGround = true;
            } else {
                while (collides(p, world)) p.y -= 0.005;
            }
            p.velocityY = 0;
        } else {
            p.onGround = false;
        }

        // Jump (ONE TIME)
        if (jumpRequest && p.onGround) {
            p.velocityY = JUMP_POWER;
            p.onGround = false;
        }
    }

    private static boolean collides(Player p, ChunkWorld world) {

        int minX = (int) Math.floor(p.x - Player.RADIUS);
        int maxX = (int) Math.floor(p.x + Player.RADIUS);
        int minY = (int) Math.floor(p.y);
        int maxY = (int) Math.floor(p.y + Player.HEIGHT);
        int minZ = (int) Math.floor(p.z - Player.RADIUS);
        int maxZ = (int) Math.floor(p.z + Player.RADIUS);

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