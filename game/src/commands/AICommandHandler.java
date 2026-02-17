package commands;

import org.json.JSONObject;
import world.BlockType;

public class AICommandHandler {

    private final FillExecutor executor;

    public AICommandHandler(CommandExecutor executor) {
        this.executor = executor::fill;
    }

    public AICommandHandler(ChunkCommandExecutor executor) {
        this.executor = executor::fill;
    }

    public void handle(String json) {
        JSONObject obj = new JSONObject(json);

        String command = obj.getString("command");
        JSONObject params = obj.getJSONObject("params");
        String message = obj.optString("message", "");

        if (!message.isBlank()) {
            System.out.println("🤖 AI: " + message);
        }

        if (command.equals("fill")) {
            int x1 = params.getInt("x1");
            int y1 = params.optInt("y1", 1);
            int z1 = params.getInt("z1");
            int x2 = params.getInt("x2");
            int y2 = params.optInt("y2", 1);
            int z2 = params.getInt("z2");
            BlockType block = BlockType.valueOf(params.getString("block").toUpperCase());

            executor.fill(x1, y1, z1, x2, y2, z2, block);
        }
    }

    @FunctionalInterface
    private interface FillExecutor {
        void fill(int x1, int y1, int z1, int x2, int y2, int z2, BlockType type);
    }
}
