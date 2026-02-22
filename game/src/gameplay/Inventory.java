package gameplay;

import world.BlockType;

import java.util.EnumMap;
import java.util.Map;

public class Inventory {

    private final Map<BlockType, Integer> counts = new EnumMap<>(BlockType.class);

    public Inventory() {
        add(BlockType.DIRT, 12);
        add(BlockType.WOOD, 6);
        add(BlockType.STONE, 4);
    }

    public void add(BlockType type, int amount) {
        if (type == null || type == BlockType.AIR || amount <= 0) return;
        counts.put(type, get(type) + amount);
    }

    public boolean remove(BlockType type, int amount) {
        if (amount <= 0) return true;
        int cur = get(type);
        if (cur < amount) return false;
        counts.put(type, cur - amount);
        return true;
    }

    public int get(BlockType type) {
        return counts.getOrDefault(type, 0);
    }
}
