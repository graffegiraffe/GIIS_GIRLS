package by.rublevskaya.pinkeditor.algorithms.lab5;

import javax.swing.table.DefaultTableModel;
import java.awt.Point;
import java.util.*;

public class SimpleFloodFillAlgorithm implements PolygonFillAlgorithm {

    @Override
    public List<List<Point>> fill(List<Point> vertices, Point seed,
                                   int width, int height, DefaultTableModel tableModel) {
        List<List<Point>> steps = new ArrayList<>();
        if (vertices.size() < 3 || seed == null) return steps;

        boolean[][] boundary = rasteriseBoundary(vertices, width, height);
        boolean[][] filled   = new boolean[width][height];

        if (seed.x < 0 || seed.x >= width || seed.y < 0 || seed.y >= height) return steps;
        if (boundary[seed.x][seed.y]) return steps;

        Deque<Point> stack = new ArrayDeque<>();
        stack.push(seed);

        int stepNum = 0;

        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        while (!stack.isEmpty()) {
            Point p = stack.pop();
            int px = p.x, py = p.y;

            if (px < 0 || px >= width || py < 0 || py >= height) continue;
            if (boundary[px][py] || filled[px][py]) continue;

            filled[px][py] = true;
            steps.add(Collections.singletonList(new Point(px, py)));
            stepNum++;
            tableModel.addRow(new Object[]{stepNum, px, py, stack.size()});

            for (int[] d : dirs) {
                int nx = px + d[0], ny = py + d[1];
                if (nx >= 0 && nx < width && ny >= 0 && ny < height
                        && !boundary[nx][ny] && !filled[nx][ny]) {
                    stack.push(new Point(nx, ny));
                }
            }
        }
        return steps;
    }

    @Override
    public String[] getTableColumns() {
        return new String[]{"Шаг", "X", "Y", "Стек"};
    }

    public static boolean[][] rasteriseBoundary(List<Point> vertices, int width, int height) {
        boolean[][] grid = new boolean[width][height];
        int n = vertices.size();
        for (int i = 0; i < n; i++) {
            Point a = vertices.get(i);
            Point b = vertices.get((i + 1) % n);
            bresenham(a.x, a.y, b.x, b.y, grid, width, height);
        }
        return grid;
    }

    private static void bresenham(int x1, int y1, int x2, int y2,
                                   boolean[][] grid, int w, int h) {
        int dx = Math.abs(x2 - x1), sx = x1 < x2 ? 1 : -1;
        int dy = Math.abs(y2 - y1), sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        while (true) {
            if (x1 >= 0 && x1 < w && y1 >= 0 && y1 < h) grid[x1][y1] = true;
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x1 += sx; }
            if (e2 <  dx) { err += dx; y1 += sy; }
        }
    }
}