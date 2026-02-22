package gameplay;

import world.BlockType;

public class CraftingSystem {

    public boolean craftStoneFromDirt(Inventory inventory) {
        if (inventory.remove(BlockType.DIRT, 4)) {
            inventory.add(BlockType.STONE, 1);
            return true;
        }
        return false;
    }

    public boolean craftGlassFromSand(Inventory inventory) {
        if (inventory.remove(BlockType.SAND, 3)) {
            inventory.add(BlockType.GLASS, 1);
            return true;
        }
        return false;
    }

    public boolean craftWoodFromGrass(Inventory inventory) {
        if (inventory.remove(BlockType.GRASS, 2)) {
            inventory.add(BlockType.WOOD, 1);
            return true;
        }
        return false;
    }
}
