package engine;

import world.ChunkWorld;

public class PhysicsEngine {

    public static final double GRAVITY = 22.0;
    public static final double JUMP_POWER = 8.5;
    public static final double TERMINAL_VELOCITY = 35.0;
    public static final double AIR_DRAG = 0.985;

    public static final double WALK_SPEED = 4.3;
    public static final double SPRINT_SPEED = 5.6;
    public static final double GROUND_ACCEL = 30.0;
    public static final double AIR_ACCEL = 8.0;
    public static final double GROUND_FRICTION = 12.0;

    public static void updateHorizontal(Player p, ChunkWorld world, double dt, double moveX, double moveZ, boolean sprint) {
        double desiredSpeed = sprint ? SPRINT_SPEED : WALK_SPEED;
        double len = Math.sqrt(moveX * moveX + moveZ * moveZ);

        double targetVX = 0;
        double targetVZ = 0;
        if (len > 0.0001) {
            targetVX = (moveX / len) * desiredSpeed;
            targetVZ = (moveZ / len) * desiredSpeed;
        }

        double accel = p.onGround ? GROUND_ACCEL : AIR_ACCEL;
        p.velocityX = approach(p.velocityX, targetVX, accel * dt);
        p.velocityZ = approach(p.velocityZ, targetVZ, accel * dt);

        if (len < 0.0001 && p.onGround) {
            double frictionStep = GROUND_FRICTION * dt;
            p.velocityX = approach(p.velocityX, 0, frictionStep);
            p.velocityZ = approach(p.velocityZ, 0, frictionStep);
        }

        p.move(p.velocityX * dt, p.velocityZ * dt, world);
    }

    public static void update(Player p, ChunkWorld world, double dt, boolean jumpRequest) {
        boolean wasGrounded = p.onGround;

        p.velocityY -= GRAVITY * dt;
        if (p.velocityY < -TERMINAL_VELOCITY) {
            p.velocityY = -TERMINAL_VELOCITY;
        }

        p.velocityY *= Math.pow(AIR_DRAG, dt * 60.0);
        p.y += p.velocityY * dt;

        if (!p.onGround && p.velocityY < 0) {
            p.fallDistance += -p.velocityY * dt;
        }

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

        if (!wasGrounded && p.onGround) {
            applyFallDamage(p);
            p.fallDistance = 0;
        }

        if (jumpRequest && p.onGround) {
            p.velocityY = JUMP_POWER;
            p.onGround = false;
        }
    }

    private static void applyFallDamage(Player p) {
        double safe = 3.0;
        if (p.fallDistance <= safe) return;
        double damage = (p.fallDistance - safe) * 2.0;
        p.damage(damage);
    }

    private static double approach(double value, double target, double delta) {
        if (value < target) return Math.min(value + delta, target);
        if (value > target) return Math.max(value - delta, target);
        return target;
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
