package by.rublevskaya.pinkeditor.algorithms;

public enum AlgorithmType {
    DDA("DDA"),
    BRESENHAM("Bresenham"),
    WU("algorithm By");

    private final String name;

    AlgorithmType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
