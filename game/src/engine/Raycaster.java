package engine;

import world.ChunkWorld;

public class Raycaster {

    private static final double STEP = 0.05;

    private Raycaster() {
    }

    // Pure query utility: does not mutate world or player state.
    public static RaycastHit raycast(
            ChunkWorld world,
            double ox, double oy, double oz,
            double dx, double dy, double dz,
            double maxDist
    ) {
        double dist = 0;

        int px = (int) Math.floor(ox);
        int py = (int) Math.floor(oy);
        int pz = (int) Math.floor(oz);

        while (dist < maxDist) {
            dist += STEP;

            double sx = ox + dx * dist;
            double sy = oy + dy * dist;
            double sz = oz + dz * dist;

            int bx = (int) Math.floor(sx);
            int by = (int) Math.floor(sy);
            int bz = (int) Math.floor(sz);

            if (world.isSolid(bx, by, bz)) {
                return new RaycastHit(
                        bx, by, bz,
                        bx - px,
                        by - py,
                        bz - pz,
                        dist
                );
            }

            px = bx;
            py = by;
            pz = bz;
        }

        return null;
    }
}
