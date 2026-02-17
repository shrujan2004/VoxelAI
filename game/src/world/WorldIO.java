package world;

import java.io.*;

public class WorldIO {

    public static void save(World world, int px, int py, int pz) {
        try (PrintWriter out = new PrintWriter("save.txt")) {

            out.println(px + " " + py + " " + pz);

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 8; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockType b = world.getBlock(x, y, z);
                        if (b != BlockType.AIR) {
                            out.println(x + " " + y + " " + z + " " + b.name());
                        }
                    }
                }
            }

            System.out.println("💾 World saved.");

        } catch (Exception e) {
            System.out.println("❌ Save failed.");
        }
    }

    public static int[] load(World world) {
        try (BufferedReader br = new BufferedReader(new FileReader("save.txt"))) {

            String[] p = br.readLine().split(" ");
            int px = Integer.parseInt(p[0]);
            int py = Integer.parseInt(p[1]);
            int pz = Integer.parseInt(p[2]);

            String line;
            while ((line = br.readLine()) != null) {
                String[] s = line.split(" ");
                world.setBlock(
                        Integer.parseInt(s[0]),
                        Integer.parseInt(s[1]),
                        Integer.parseInt(s[2]),
                        BlockType.valueOf(s[3])
                );
            }

            System.out.println("📂 World loaded.");
            return new int[]{px, py, pz};

        } catch (Exception e) {
            System.out.println("❌ Load failed.");
            return null;
        }
    }
}
