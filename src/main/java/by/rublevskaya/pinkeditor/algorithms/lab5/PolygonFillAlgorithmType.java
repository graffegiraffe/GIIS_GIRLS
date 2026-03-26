package by.rublevskaya.pinkeditor.algorithms.lab5;

public enum PolygonFillAlgorithmType {
    ORDERED_EDGE_LIST("Упорядоченный список рёбер (OEL)"),
    ACTIVE_EDGE_LIST("Список активных рёбер (AEL)"),
    SIMPLE_FLOOD_FILL("Простая заливка с затравкой"),
    SCANLINE_FLOOD_FILL("Построчная заливка с затравкой");

    private final String displayName;

    PolygonFillAlgorithmType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}