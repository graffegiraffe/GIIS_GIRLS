package by.rublevskaya.pinkeditor.algorithms.lab5;

import javax.swing.table.DefaultTableModel;
import java.awt.Point;
import java.util.*;

public class OrderedEdgeListAlgorithm implements PolygonFillAlgorithm {

    @Override
    public List<List<Point>> fill(List<Point> vertices, Point seed,
                                   int width, int height, DefaultTableModel tableModel) {
        List<List<Point>> steps = new ArrayList<>();
        if (vertices.size() < 3) return steps;

        int n = vertices.size();
        List<int[]> intersections = new ArrayList<>(); // {x, y}

        for (int i = 0; i < n; i++) {
            Point p1 = vertices.get(i);
            Point p2 = vertices.get((i + 1) % n);

            int x1 = p1.x, y1 = p1.y, x2 = p2.x, y2 = p2.y;

            if (y1 == y2) continue;

            if (y1 > y2) {
                int t = x1; x1 = x2; x2 = t;
                t = y1;     y1 = y2; y2 = t;
            }

            for (int y = y1; y < y2; y++) {
                int x = (int) Math.round(x1 + (double)(y - y1) * (x2 - x1) / (y2 - y1));
                intersections.add(new int[]{x, y});
            }
        }

        intersections.sort((a, b) -> a[1] != b[1] ? Integer.compare(a[1], b[1]) : Integer.compare(a[0], b[0]));

        int stepNum = 0;
        int idx = 0;
        while (idx < intersections.size()) {
            int y = intersections.get(idx)[1];

            List<Integer> xs = new ArrayList<>();
            while (idx < intersections.size() && intersections.get(idx)[1] == y) {
                xs.add(intersections.get(idx)[0]);
                idx++;
            }

            for (int j = 0; j + 1 < xs.size(); j += 2) {
                int xStart = xs.get(j);
                int xEnd   = xs.get(j + 1);

                List<Point> span = new ArrayList<>();
                for (int x = xStart; x <= xEnd; x++) {
                    span.add(new Point(x, y));
                }
                if (!span.isEmpty()) {
                    steps.add(span);
                    stepNum++;
                    tableModel.addRow(new Object[]{stepNum, y, xStart, xEnd, xEnd - xStart + 1});
                }
            }
        }

        return steps;
    }

    @Override
    public String[] getTableColumns() {
        return new String[]{"Шаг", "Y", "X нач.", "X кон.", "Пикселей"};
    }
}