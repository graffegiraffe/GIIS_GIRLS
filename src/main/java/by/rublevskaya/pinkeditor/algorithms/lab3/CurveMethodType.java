package by.rublevskaya.pinkeditor.algorithms.lab3;

public enum CurveMethodType {
    HERMITE("Эрмит"),
    BEZIER("Безье"),
    BSPLINE("B-сплайн");

    private final String name;

    CurveMethodType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}