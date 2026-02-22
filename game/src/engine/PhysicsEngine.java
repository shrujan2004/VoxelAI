package engine;

import world.ChunkWorld;

public class PhysicsEngine {

    // Classic voxel-style per-tick numbers (20 ticks/sec) adapted to SI-like units.
    private static final double TICKS_PER_SECOND = 20.0;
    public static final double GRAVITY_PER_TICK = 0.08;
    public static final double GRAVITY = GRAVITY_PER_TICK * TICKS_PER_SECOND * TICKS_PER_SECOND;
    public static final double TERMINAL_VELOCITY_PER_TICK = 3.92;
    public static final double TERMINAL_VELOCITY = TERMINAL_VELOCITY_PER_TICK * TICKS_PER_SECOND;
    public static final double JUMP_POWER = 8.5;

    public static final double WALK_SPEED = 4.3;
    public static final double SPRINT_SPEED = 5.6;
    public static final double GROUND_ACCEL = 30.0;
    public static final double AIR_ACCEL = 8.0;

    // Requested friction model: ~10%/tick on ground, ~2%/tick in air.
    public static final double GROUND_FRICTION_PER_TICK = 0.90;
    public static final double AIR_FRICTION_PER_TICK = 0.98;

    public static final double JUMP_BUFFER_TIME = 0.15;
    public static final double COYOTE_TIME = 0.10;

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

        double tickScale = dt * TICKS_PER_SECOND;
        double friction = Math.pow(p.onGround ? GROUND_FRICTION_PER_TICK : AIR_FRICTION_PER_TICK, tickScale);
        p.velocityX *= friction;
        p.velocityZ *= friction;

        p.move(p.velocityX * dt, p.velocityZ * dt, world);
    }

    public static void update(Player p, ChunkWorld world, double dt, boolean jumpRequest) {
        boolean wasGrounded = p.onGround;

        if (jumpRequest) {
            p.jumpBufferTimer = JUMP_BUFFER_TIME;
        } else {
            p.jumpBufferTimer = Math.max(0, p.jumpBufferTimer - dt);
        }
        p.coyoteTimer = Math.max(0, p.coyoteTimer - dt);

        p.velocityY -= GRAVITY * dt;
        if (p.velocityY < -TERMINAL_VELOCITY) {
            p.velocityY = -TERMINAL_VELOCITY;
        }

        p.y += p.velocityY * dt;

        if (!p.onGround && p.velocityY < 0) {
            p.fallDistance += -p.velocityY * dt;
        }

        if (collides(p, world)) {
            if (p.velocityY < 0) {
                for (int i = 0; i < 200 && collides(p, world); i++) p.y += 0.005;
                p.onGround = true;
                p.coyoteTimer = COYOTE_TIME;
            } else {
                for (int i = 0; i < 200 && collides(p, world); i++) p.y -= 0.005;
            }
            p.velocityY = 0;
        } else {
            p.onGround = false;
        }

        if (!p.onGround && p.velocityY <= 0 && isStandingOnGround(p, world, 0.04)) {
            p.onGround = true;
            p.velocityY = 0;
        }

        if (!wasGrounded && p.onGround) {
            applyFallDamage(p);
            p.fallDistance = 0;
        }

        if (p.jumpBufferTimer > 0 && (p.onGround || p.coyoteTimer > 0)) {
            p.velocityY = JUMP_POWER;
            p.onGround = false;
            p.jumpBufferTimer = 0;
            p.coyoteTimer = 0;
        }
    }

    private static void applyFallDamage(Player p) {
        double safe = 3.0;
        if (p.fallDistance <= safe) return;
        double damage = (p.fallDistance - safe) * 2.0;
        p.damage(damage);
    }


    private static boolean isStandingOnGround(Player p, ChunkWorld world, double epsilon) {
        double feetY = p.y - epsilon;
        int minX = (int) Math.floor(p.x - Player.RADIUS);
        int maxX = (int) Math.floor(p.x + Player.RADIUS);
        int y = (int) Math.floor(feetY);
        int minZ = (int) Math.floor(p.z - Player.RADIUS);
        int maxZ = (int) Math.floor(p.z + Player.RADIUS);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (world.isSolid(x, y, z)) {
                    return true;
                }
            }
        }
        return false;
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
