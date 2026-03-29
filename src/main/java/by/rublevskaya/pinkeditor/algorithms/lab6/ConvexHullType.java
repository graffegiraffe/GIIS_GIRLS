package by.rublevskaya.pinkeditor.algorithms.lab6;

public enum ConvexHullType {
    GRAHAM("Алгоритм Грэхема"),
    JARVIS("Обход Джарвиса");

    private final String displayName;

    ConvexHullType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
