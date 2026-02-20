package engine;

public class Gravity {

    public static final double GRAVITY = 18.0;
    public static final double JUMP_FORCE = 6.8;

    public static double apply(double velocityY, double deltaTime) {
        return velocityY - GRAVITY * deltaTime;
    }
}
