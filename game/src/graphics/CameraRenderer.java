package graphics;

import world.BlockType;
import world.World;

public class CameraRenderer {

    public static void render(World world, int px, int py, int pz, int radius) {
        System.out.println("\n🗺️ Camera View (Player @ " + px + "," + py + "," + pz + ")\n");

        for (int z = pz - radius; z <= pz + radius; z++) {
            for (int x = px - radius; x <= px + radius; x++) {

                if (x == px && z == pz) {
                    System.out.print("\u001B[36mP\u001B[0m ");
                    continue;
                }

                BlockType block = world.getBlock(x, py, z);
                String glyph = switch (block) {
                    case AIR -> ".";
                    case GRASS -> "G";
                    case DIRT -> "D";
                    case STONE -> "S";
                    case WOOD -> "W";
                    case SAND -> "A";
                    case GLASS -> "L";
                    case WATER -> "~";
                };
                System.out.print(glyph + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}
