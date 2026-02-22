package input;

public class PlayerInputState {

    public boolean forward;
    public boolean back;
    public boolean left;
    public boolean right;
    public boolean sprint;
    public boolean jumpRequested;

    public boolean breakHeld;
    public boolean placeRequested;
    public boolean craftRequested;

    public double yaw = 0;
    public double pitch = -0.20;

    public void turnLeft() {
        yaw -= Math.toRadians(6);
    }

    public void turnRight() {
        yaw += Math.toRadians(6);
    }

    public void lookUp() {
        pitch = Math.max(-0.9, pitch - 0.08);
    }

    public void lookDown() {
        pitch = Math.min(0.9, pitch + 0.08);
    }

    public double moveX() {
        double forwardX = Math.sin(yaw);
        double rightX = Math.cos(yaw);

        double moveX = 0;
        if (forward) moveX += forwardX;
        if (back) moveX -= forwardX;
        if (left) moveX -= rightX;
        if (right) moveX += rightX;
        return moveX;
    }

    public double moveZ() {
        double forwardZ = -Math.cos(yaw);
        double rightZ = Math.sin(yaw);

        double moveZ = 0;
        if (forward) moveZ += forwardZ;
        if (back) moveZ -= forwardZ;
        if (left) moveZ -= rightZ;
        if (right) moveZ += rightZ;
        return moveZ;
    }
}
