package by.rublevskaya.pinkeditor.algorithms.lab5;

import javax.swing.table.DefaultTableModel;
import java.awt.Point;
import java.util.*;

public class ScanLineFloodFillAlgorithm implements PolygonFillAlgorithm {

    @Override
    public List<List<Point>> fill(List<Point> vertices, Point seed,
                                   int width, int height, DefaultTableModel tableModel) {
        List<List<Point>> steps = new ArrayList<>();
        if (vertices.size() < 3 || seed == null) return steps;

        boolean[][] boundary = SimpleFloodFillAlgorithm.rasteriseBoundary(vertices, width, height);
        boolean[][] filled   = new boolean[width][height];

        if (seed.x < 0 || seed.x >= width || seed.y < 0 || seed.y >= height) return steps;
        if (boundary[seed.x][seed.y]) return steps;

        Deque<Point> stack = new ArrayDeque<>();
        stack.push(seed);

        int stepNum = 0;

        while (!stack.isEmpty()) {
            Point s = stack.pop();
            int sy = s.y, sx = s.x;

            if (sy < 0 || sy >= height || sx < 0 || sx >= width) continue;
            if (filled[sx][sy] || boundary[sx][sy]) continue;
            int xLeft = sx;
            while (xLeft > 0 && !boundary[xLeft - 1][sy] && !filled[xLeft - 1][sy]) xLeft--;
            int xRight = sx;
            while (xRight < width - 1 && !boundary[xRight + 1][sy] && !filled[xRight + 1][sy]) xRight++;

            List<Point> span = new ArrayList<>();
            for (int x = xLeft; x <= xRight; x++) {
                filled[x][sy] = true;
                span.add(new Point(x, sy));
            }
            steps.add(span);
            stepNum++;
            tableModel.addRow(new Object[]{stepNum, sy, xLeft, xRight, xRight - xLeft + 1});
            pushSeeds(stack, filled, boundary, xLeft, xRight, sy - 1, width, height);
            pushSeeds(stack, filled, boundary, xLeft, xRight, sy + 1, width, height);
        }
        return steps;
    }

    @Override
    public String[] getTableColumns() {
        return new String[]{"Шаг", "Y", "X нач.", "X кон.", "Пикселей"};
    }

    private void pushSeeds(Deque<Point> stack, boolean[][] filled, boolean[][] boundary,
                            int xLeft, int xRight, int y, int width, int height) {
        if (y < 0 || y >= height) return;
        boolean inGap = false;
        for (int x = xLeft; x <= xRight; x++) {
            if (!filled[x][y] && !boundary[x][y]) {
                inGap = true;
            } else {
                if (inGap) {
                    stack.push(new Point(x - 1, y));
                    inGap = false;
                }
            }
        }
        if (inGap) stack.push(new Point(xRight, y));
    }
}