package engine;

public class Player {

    public double x, y, z;
    public double velocityY;
    public boolean onGround;

    public Player(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}