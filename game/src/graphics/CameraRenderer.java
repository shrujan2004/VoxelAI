package graphics;

import world.World;
import world.BlockType;

public class CameraRenderer {

    public static void render(World world, int px, int py, int pz, int radius) {
        System.out.println("\n🗺️ Camera View (Player @ " + px + "," + py + "," + pz + ")\n");

        for (int z = pz - radius; z <= pz + radius; z++) {
            for (int x = px - radius; x <= px + radius; x++) {

                if (x == px && z == pz) {
                    System.out.print("P ");
                    continue;
                }

                BlockType block = world.getBlock(x, py, z);
                System.out.print(block.getSymbol() + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}
