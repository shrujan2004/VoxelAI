package world;

public enum BlockType {
    AIR('.'),
    WOOD('W'),
    STONE('S'),
    DIRT('D'),
    GLASS('G');

    private final char symbol;

    BlockType(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }
}
