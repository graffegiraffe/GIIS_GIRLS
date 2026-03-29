package by.rublevskaya.pinkeditor.algorithms.lab6;

import javax.swing.table.DefaultTableModel;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
public class JarvisMarch {
    public static List<List<Point>> compute(List<Point> points, DefaultTableModel tableModel) {
        List<List<Point>> steps = new ArrayList<>();
        int n = points.size();
        if (n < 3) return steps;
        int startIdx = 0;
        for (int i = 1; i < n; i++) {
            if (points.get(i).x < points.get(startIdx).x ||
               (points.get(i).x == points.get(startIdx).x && points.get(i).y > points.get(startIdx).y)) {
                startIdx = i;
            }
        }

        List<Point> hull = new ArrayList<>();
        int current = startIdx;
        int step = 1;

        do {
            hull.add(points.get(current));
            steps.add(new ArrayList<>(hull));

            if (tableModel != null) {
                Point cp = points.get(current);
                tableModel.addRow(new Object[]{
                        step++,
                        "(" + cp.x + "," + cp.y + ")",
                        hull.size(),
                        "Добавлено в оболочку"
                });
            }

            int next = (current + 1) % n;
            for (int i = 0; i < n; i++) {
                long c = cross(points.get(current), points.get(next), points.get(i));
                if (c < 0) {
                    next = i;
                } else if (c == 0) {
                    if (distSq(points.get(current), points.get(i)) >
                        distSq(points.get(current), points.get(next))) {
                        next = i;
                    }
                }
            }

            current = next;

        } while (current != startIdx && hull.size() <= n);
        steps.add(new ArrayList<>(hull));
        if (tableModel != null) {
            tableModel.addRow(new Object[]{
                    step, "Замыкание", hull.size(), "Оболочка построена"
            });
        }

        return steps;
    }
    private static long cross(Point O, Point A, Point B) {
        return (long)(A.x - O.x) * (B.y - O.y) - (long)(A.y - O.y) * (B.x - O.x);
    }

    private static long distSq(Point a, Point b) {
        long dx = a.x - b.x, dy = a.y - b.y;
        return dx * dx + dy * dy;
    }

    public static String[] getTableColumns() {
        return new String[]{"Шаг", "Точка", "Размер оболочки", "Примечание"};
    }
}