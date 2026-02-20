package engine;

public class RaycastHit {
    public final int x, y, z;
    public final int faceX, faceY, faceZ;
    public final double distance;

    public RaycastHit(int x, int y, int z,
                      int fx, int fy, int fz,
                      double distance) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.faceX = fx;
        this.faceY = fy;
        this.faceZ = fz;
        this.distance = distance;
    }
}
