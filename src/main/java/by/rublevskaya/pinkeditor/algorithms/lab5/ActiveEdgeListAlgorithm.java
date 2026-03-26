package by.rublevskaya.pinkeditor.algorithms.lab5;

import javax.swing.table.DefaultTableModel;
import java.awt.Point;
import java.util.*;

public class ActiveEdgeListAlgorithm implements PolygonFillAlgorithm {

    private static class EdgeEntry {
        double x;
        final double dx;
        int yRemaining;

        EdgeEntry(double x, double dx, int yRemaining) {
            this.x = x;
            this.dx = dx;
            this.yRemaining = yRemaining;
        }
    }

    @Override
    public List<List<Point>> fill(List<Point> vertices, Point seed,
                                   int width, int height, DefaultTableModel tableModel) {
        List<List<Point>> steps = new ArrayList<>();
        if (vertices.size() < 3) return steps;

        int n = vertices.size();
        int yMin = Integer.MAX_VALUE, yMax = Integer.MIN_VALUE;
        for (Point p : vertices) {
            yMin = Math.min(yMin, p.y);
            yMax = Math.max(yMax, p.y);
        }

        @SuppressWarnings("unchecked")
        List<EdgeEntry>[] et = new List[yMax + 1];
        for (int i = 0; i <= yMax; i++) et[i] = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            Point p1 = vertices.get(i);
            Point p2 = vertices.get((i + 1) % n);

            int x1 = p1.x, y1 = p1.y, x2 = p2.x, y2 = p2.y;
            if (y1 == y2) continue;
            if (y1 > y2) {
                int t = x1; x1 = x2; x2 = t;
                t = y1;     y1 = y2; y2 = t;
            }

            double dx = (double)(x2 - x1) / (y2 - y1);
            int yRemaining = y2 - y1;
            et[y1].add(new EdgeEntry(x1, dx, yRemaining));
        }

        List<EdgeEntry> ael = new ArrayList<>();

        int stepNum = 0;
        for (int y = yMin; y < yMax; y++) {
            ael.addAll(et[y]);
            ael.sort(Comparator.comparingDouble(e -> e.x));
            for (int j = 0; j + 1 < ael.size(); j += 2) {
                int xStart = (int) Math.round(ael.get(j).x);
                int xEnd   = (int) Math.round(ael.get(j + 1).x);
                if (xStart > xEnd) { int t = xStart; xStart = xEnd; xEnd = t; }

                List<Point> span = new ArrayList<>();
                for (int x = xStart; x <= xEnd; x++) span.add(new Point(x, y));
                if (!span.isEmpty()) {
                    steps.add(span);
                    stepNum++;
                    tableModel.addRow(new Object[]{stepNum, y,
                            String.format("%.1f", ael.get(j).x),
                            String.format("%.1f", ael.get(j + 1).x),
                            xEnd - xStart + 1});
                }
            }

            Iterator<EdgeEntry> it = ael.iterator();
            while (it.hasNext()) {
                EdgeEntry e = it.next();
                e.yRemaining--;
                if (e.yRemaining == 0) {
                    it.remove();
                } else {
                    e.x += e.dx;
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