import commands.AICommandHandler;
import commands.CommandExecutor;
import java.util.Scanner;
import world.World;

public class Main {

    public static void main(String[] args) {

        World world = new World();
        CommandExecutor executor = new CommandExecutor(world);
        AICommandHandler aiHandler = new AICommandHandler(executor);
        AIClient aiClient = new AIClient();

        Scanner scanner = new Scanner(System.in);

        System.out.println("🧱 VoxelAI Started");
        System.out.println("Type natural language commands");
        System.out.println("Example: build a wooden platform 5 by 5");
        System.out.println("Type 'exit' to quit");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            String aiResponse = aiClient.askAI(input);

            if (aiResponse == null) {
                System.out.println("🤖 AI is currently unavailable (quota / offline).");
                System.out.println("🤖 Try again later or use offline commands.");
                continue;
            }

            aiHandler.handle(aiResponse);
        }

        scanner.close();
        System.out.println("👋 Game exited.");
    }
}
