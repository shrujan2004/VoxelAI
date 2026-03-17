package commands;

import world.BlockType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AICommandHandler {

    private final FillExecutor executor;

    public AICommandHandler(CommandExecutor executor) {
        this.executor = executor::fill;
    }

    public AICommandHandler(ChunkCommandExecutor executor) {
        this.executor = executor::fill;
    }

    public void handle(String json) {
        String command = extractString(json, "command", "");
        String message = extractString(json, "message", "");

        if (!message.isBlank()) {
            System.out.println("🤖 AI: " + message);
        }

        if ("fill".equalsIgnoreCase(command)) {
            int x1 = extractInt(json, "x1", 0);
            int y1 = extractInt(json, "y1", 1);
            int z1 = extractInt(json, "z1", 0);
            int x2 = extractInt(json, "x2", x1);
            int y2 = extractInt(json, "y2", y1);
            int z2 = extractInt(json, "z2", z1);
            String blockName = extractString(json, "block", "DIRT");

            try {
                BlockType block = BlockType.valueOf(blockName.toUpperCase());
                executor.fill(x1, y1, z1, x2, y2, z2, block);
            } catch (IllegalArgumentException ex) {
                System.out.println("⚠ Unknown block from AI: " + blockName);
            }
        }
    }

    private static int extractInt(String src, String key, int fallback) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\s*(-?\\d+)");
        Matcher m = p.matcher(src);
        return m.find() ? Integer.parseInt(m.group(1)) : fallback;
    }

    private static String extractString(String src, String key, String fallback) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\s*\"([^\"]*)\"");
        Matcher m = p.matcher(src);
        return m.find() ? m.group(1) : fallback;
    }

    @FunctionalInterface
    private interface FillExecutor {
        void fill(int x1, int y1, int z1, int x2, int y2, int z2, BlockType type);
    }
}
