package by.rublevskaya.pinkeditor.algorithms.lab2;

public enum CurveType {
    CIRCLE("Окружность (R)"),
    ELLIPSE("Эллипс (A, B)"),
    HYPERBOLA("Гипербола (A, B)"),
    PARABOLA("Парабола (P)");

    private final String name;
    CurveType(String name) { this.name = name; }
    @Override public String toString() { return name; }
}
