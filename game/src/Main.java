import commands.AICommandHandler;
import commands.CommandExecutor;
import graphics.CameraRenderer;
import java.util.Scanner;
import world.BlockType;
import world.World;

public class Main {

    public static void main(String[] args) {

        World world = new World();
        CommandExecutor executor = new CommandExecutor(world);
        AICommandHandler aiHandler = new AICommandHandler(executor);
        AIClient aiClient = new AIClient();

        // PLAYER POSITION
        int playerX = 8;
        int playerY = 1;
        int playerZ = 8;

        Scanner scanner = new Scanner(System.in);

        System.out.println("🧱 VoxelAI Started");
        System.out.println("Controls:");
        System.out.println("  W A S D        → Move");
        System.out.println("  // <text>      → Use AI");
        System.out.println("  /fill ...      → Offline build");
        System.out.println("  exit           → Quit");
        System.out.println();

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            // ----------------------------
            // PLAYER MOVEMENT
            // ----------------------------
            switch (input.toLowerCase()) {
                case "w" -> playerZ--;
                case "s" -> playerZ++;
                case "a" -> playerX--;
                case "d" -> playerX++;
            }

            // ----------------------------
            // OFFLINE FILL COMMAND
            // ----------------------------
            if (input.startsWith("/fill")) {
                try {
                    String[] p = input.split(" ");
                    int x1 = Integer.parseInt(p[1]);
                    int y1 = Integer.parseInt(p[2]);
                    int z1 = Integer.parseInt(p[3]);
                    int x2 = Integer.parseInt(p[4]);
                    int y2 = Integer.parseInt(p[5]);
                    int z2 = Integer.parseInt(p[6]);
                    BlockType block = BlockType.valueOf(p[7].toUpperCase());

                    executor.fill(x1, y1, z1, x2, y2, z2, block);
                } catch (Exception e) {
                    System.out.println("❌ Usage: /fill x1 y1 z1 x2 y2 z2 BLOCK");
                }
            }

            // ----------------------------
            // AI MODE (EXPLICIT // ONLY)
            // ----------------------------
            else if (input.startsWith("//")) {
                String aiInput = input.substring(2).trim();

                if (!aiInput.isEmpty()) {
                    String aiResponse = aiClient.askAI(aiInput);
                    if (aiResponse != null) {
                        aiHandler.handle(aiResponse);
                    } else {
                        System.out.println("🤖 AI unavailable.");
                    }
                }
            }

            // ----------------------------
            // RENDER CAMERA EVERY LOOP
            // ----------------------------
            CameraRenderer.render(world, playerX, playerY, playerZ, 3);
        }

        scanner.close();
        System.out.println("👋 Game exited.");
    }
}
