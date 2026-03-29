package by.rublevskaya.pinkeditor.algorithms.lab6;

import javax.swing.table.DefaultTableModel;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class GrahamScan {
    public static List<List<Point>> compute(List<Point> points, DefaultTableModel tableModel) {
        List<List<Point>> steps = new ArrayList<>();
        if (points.size() < 3) return steps;
        Point anchor = points.stream()
                .min(Comparator.comparingInt((Point p) -> p.y).thenComparingInt(p -> p.x))
                .orElse(points.get(0));

        List<Point> sorted = new ArrayList<>(points);
        sorted.remove(anchor);
        sorted.sort((a, b) -> {
            double angA = Math.atan2(a.y - anchor.y, a.x - anchor.x);
            double angB = Math.atan2(b.y - anchor.y, b.x - anchor.x);
            if (Double.compare(angA, angB) != 0) return Double.compare(angA, angB);
            int da = distSq(anchor, a);
            int db = distSq(anchor, b);
            return Integer.compare(da, db);
        });

        int step = 1;

        Stack<Point> stack = new Stack<>();
        stack.push(anchor);
        stack.push(sorted.get(0));

        addStep(steps, stack, tableModel, step++, "Инициализация стека");

        for (int i = 1; i < sorted.size(); i++) {
            Point next = sorted.get(i);

            while (stack.size() > 1 && cross(nextToTop(stack), stack.peek(), next) <= 0) {
                Point removed = stack.pop();
                addRow(tableModel, step++,
                        "Удаление (" + removed.x + "," + removed.y + ")",
                        stackToString(stack),
                        "Правый поворот");
            }

            stack.push(next);
            addStep(steps, stack, tableModel, step++,
                    "Добавление (" + next.x + "," + next.y + ")");
        }

        addStep(steps, stack, tableModel, step, "Готово");

        return steps;
    }
    private static long cross(Point O, Point A, Point B) {
        return (long)(A.x - O.x) * (B.y - O.y) - (long)(A.y - O.y) * (B.x - O.x);
    }

    private static int distSq(Point a, Point b) {
        int dx = a.x - b.x, dy = a.y - b.y;
        return dx * dx + dy * dy;
    }

    private static Point nextToTop(Stack<Point> s) {
        Point top = s.pop();
        Point next = s.peek();
        s.push(top);
        return next;
    }

    private static void addStep(List<List<Point>> steps, Stack<Point> stack,
                                 DefaultTableModel tm, int step, String note) {
        steps.add(new ArrayList<>(stack));
        if (tm != null) {
            tm.addRow(new Object[]{step, stackToString(stack), note});
        }
    }

    private static void addRow(DefaultTableModel tm, int step, String action,
                                String stack, String note) {
        if (tm != null) {
            tm.addRow(new Object[]{step, action, stack, note});
        }
    }

    private static String stackToString(Stack<Point> s) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < s.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("(").append(s.get(i).x).append(",").append(s.get(i).y).append(")");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String[] getTableColumns() {
        return new String[]{"Шаг", "Действие / Точка", "Стек", "Примечание"};
    }
}