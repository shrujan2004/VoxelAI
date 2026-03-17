package graphics;

import javafx.scene.paint.Color;
import world.BlockType;

public class BlockVisuals {

    public static Color colorForBlock(BlockType block, int height) {
        Color base = switch (block) {
            case GRASS -> Color.web("#55aa33");
            case DIRT -> Color.web("#8b5a2b");
            case STONE -> Color.web("#888888");
            case SAND -> Color.web("#d8cf83");
            case WOOD -> Color.web("#a06a3a");
            case GLASS -> Color.web("#99d8ff");
            case WATER -> Color.web("#3b66c5");
            default -> Color.web("#333333");
        };

        double shade = Math.max(0.65, Math.min(1.15, 0.75 + height * 0.06));
        return base.deriveColor(0, 1, shade, 1);
    }

    public static double shadeForFace(int fx, int fy, int fz) {
        if (fy > 0) return 1.0;
        if (fy < 0) return 0.55;
        if (Math.abs(fx) == 1) return 0.75;
        if (Math.abs(fz) == 1) return 0.85;
        return 0.8;
    }
}
