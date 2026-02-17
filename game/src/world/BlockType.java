package world;

public enum BlockType {
    AIR('.', "\u001B[0m"),
    WOOD('W', "\u001B[33m"),   // Yellow
    STONE('S', "\u001B[37m"), // White
    DIRT('D', "\u001B[31m"),  // Brown/Red
    GLASS('G', "\u001B[34m"); // Blue

    private final char symbol;
    private final String color;

    BlockType(char symbol, String color) {
        this.symbol = symbol;
        this.color = color;
    }

    public String render() {
        return color + symbol + "\u001B[0m";
    }
}
