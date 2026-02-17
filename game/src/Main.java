import commands.*;
import graphics.CameraRenderer;
import java.util.Scanner;
import world.*;

public class Main {

    public static void main(String[] args) {

        World world = new World();
        CommandExecutor executor = new CommandExecutor(world);
        AICommandHandler aiHandler = new AICommandHandler(executor);
        AIClient aiClient = new AIClient();

        int px = 8, py = 1, pz = 8;

        Scanner scanner = new Scanner(System.in);

        System.out.println("🧱 VoxelAI Started");
        System.out.println("WASD move | // AI | /fill | /save | /load | exit");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equals("exit")) break;

            // MOVEMENT WITH COLLISION
            int nx = px, nz = pz;
            switch (input) {
                case "w" -> nz--;
                case "s" -> nz++;
                case "a" -> nx--;
                case "d" -> nx++;
            }

            if (world.isWalkable(nx, py, nz)) {
                px = nx;
                pz = nz;
            }

            // SAVE / LOAD
            if (input.equals("/save")) {
                WorldIO.save(world, px, py, pz);
            }

            if (input.equals("/load")) {
                int[] pos = WorldIO.load(world);
                if (pos != null) {
                    px = pos[0];
                    py = pos[1];
                    pz = pos[2];
                }
            }

            // OFFLINE BUILD
            if (input.startsWith("/fill")) {
                try {
                    String[] p = input.split(" ");
                    executor.fill(
                            Integer.parseInt(p[1]),
                            Integer.parseInt(p[2]),
                            Integer.parseInt(p[3]),
                            Integer.parseInt(p[4]),
                            Integer.parseInt(p[5]),
                            Integer.parseInt(p[6]),
                            BlockType.valueOf(p[7].toUpperCase())
                    );
                } catch (Exception e) {
                    System.out.println("❌ /fill x1 y1 z1 x2 y2 z2 BLOCK");
                }
            }

            // AI MODE
            if (input.startsWith("//")) {
                String ai = input.substring(2);
                String res = aiClient.askAI(ai);
                if (res != null) aiHandler.handle(res);
            }

            CameraRenderer.render(world, px, py, pz, 3);
        }

        scanner.close();
    }
}
