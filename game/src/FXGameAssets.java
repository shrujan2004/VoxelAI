import graphics.TexturePack;
import javafx.scene.image.Image;

import java.io.File;

final class FXGameAssets {
    private FXGameAssets() {
    }

    static Image loadMaleArm() {
        return loadImage("game/Player male/male_arm.png", "../Player male/male_arm.png", "Player male/male_arm.png");
    }

    static TexturePack loadTexturePack() {
        return new TexturePack("game/tiles/atlas.png");
    }

    private static Image loadImage(String... candidates) {
        for (String candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            File file = new File(candidate);
            if (file.exists()) {
                return new Image(file.toURI().toString());
            }
        }
        return null;
    }
}
