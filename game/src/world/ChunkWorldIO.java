package world;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class ChunkWorldIO {

    private static File resolveSaveFile() {
        String[] candidates = {
                "game/src/save_chunk_edits.txt",
                "save_chunk_edits.txt",
                "../src/save_chunk_edits.txt"
        };
        for (String c : candidates) {
            File f = new File(c);
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            if (f.exists() || (parent != null && parent.exists())) {
                return f;
            }
        }
        return new File("save_chunk_edits.txt");
    }

    public static void save(ChunkWorld world) {
        File save = resolveSaveFile();
        try (PrintWriter out = new PrintWriter(new FileWriter(save))) {
            for (Map.Entry<Long, BlockType> e : world.snapshotEdits().entrySet()) {
                long key = e.getKey();
                out.println(world.keyX(key) + "," + world.keyY(key) + "," + world.keyZ(key) + "," + e.getValue().name());
            }
            System.out.println("💾 ChunkWorld saved: " + save.getPath());
        } catch (Exception ex) {
            System.out.println("❌ Save failed: " + ex.getMessage());
        }
    }

    public static void load(ChunkWorld world) {
        File save = resolveSaveFile();
        Map<Long, BlockType> loaded = new HashMap<>();

        if (!save.exists()) {
            try {
                File parent = save.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                save.createNewFile();
                world.restoreEdits(loaded);
                System.out.println("🆕 No save found. Generated new world state: " + save.getPath());
                return;
            } catch (Exception ex) {
                System.out.println("⚠ Could not create save file, continuing with generated world: " + ex.getMessage());
                world.restoreEdits(loaded);
                return;
            }
        }

        try (BufferedReader in = new BufferedReader(new FileReader(save))) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length != 4) continue;
                int x = Integer.parseInt(p[0]);
                int y = Integer.parseInt(p[1]);
                int z = Integer.parseInt(p[2]);
                BlockType t = BlockType.valueOf(p[3]);
                loaded.put(world.key(x, y, z), t);
            }
            world.restoreEdits(loaded);
            System.out.println("📂 ChunkWorld loaded: " + save.getPath());
        } catch (Exception ex) {
            System.out.println("⚠ Load skipped: " + ex.getMessage());
        }
    }
}
