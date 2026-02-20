package engine;

import world.ChunkWorld;

public class PhysicsEngine {

    private static final double GRAVITY = 25.0;
    private static final double JUMP_POWER = 9.5;

    public static void update(Player p, ChunkWorld world, double dt) {

        p.velocityY -= GRAVITY * dt;
        p.y += p.velocityY * dt;

        int ground = world.getSurfaceHeight((int)p.x, (int)p.z) + 1;

        if (p.y <= ground) {
            p.y = ground;
            p.velocityY = 0;
            p.onGround = true;
        } else {
            p.onGround = false;
        }
    }

    public static void jump(Player p) {
        if (p.onGround) {
            p.velocityY = JUMP_POWER;
            p.onGround = false;
        }
    }
}