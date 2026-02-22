import engine.Player;
import engine.PlayerInputState;
import engine.RaycastHit;
import gameplay.CraftingSystem;
import gameplay.Inventory;
import gameplay.MiningSystem;
import javafx.scene.image.Image;
import world.BlockType;
import world.ChunkWorld;

final class FXGameState {
    static final int WIDTH = 1280;
    static final int HEIGHT = 720;

    final ChunkWorld world = new ChunkWorld();
    final Player player = new Player(10, 7, 10);
    final PlayerInputState input = new PlayerInputState();

    final Inventory inventory = new Inventory();
    final CraftingSystem craftingSystem = new CraftingSystem();
    final MiningSystem miningSystem = new MiningSystem();

    final BlockType[] hotbar = createDefaultHotbar();

    int selectedSlot = 0;
    double walkTime = 0;
    RaycastHit targetHit;

    Image maleArm;

    private BlockType[] createDefaultHotbar() {
        return new BlockType[]{
                BlockType.GRASS, BlockType.DIRT, BlockType.STONE,
                BlockType.SAND, BlockType.WOOD, BlockType.GLASS,
                BlockType.WATER, BlockType.GRASS, BlockType.STONE
        };
    }
}
