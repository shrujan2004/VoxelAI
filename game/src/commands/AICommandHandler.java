package commands;

import world.BlockType;
import org.json.JSONObject;

public class AICommandHandler {

    private CommandExecutor executor;

    public AICommandHandler(CommandExecutor executor) {
        this.executor = executor;
    }

    public void handle(String json) {

        JSONObject obj = new JSONObject(json);

        String command = obj.getString("command");
        JSONObject params = obj.getJSONObject("params");
        String message = obj.optString("message", "");

        System.out.println("🤖 AI: " + message);

        if (command.equals("fill")) {
            int x1 = params.getInt("x1");
            int y1 = params.getInt("y1");
            int z1 = params.getInt("z1");
            int x2 = params.getInt("x2");
            int y2 = params.getInt("y2");
            int z2 = params.getInt("z2");
            BlockType block =
                    BlockType.valueOf(params.getString("block"));

            executor.fill(x1, y1, z1, x2, y2, z2, block);
        }
    }
}
