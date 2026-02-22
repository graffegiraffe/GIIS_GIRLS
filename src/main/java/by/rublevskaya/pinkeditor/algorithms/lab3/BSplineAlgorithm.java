package by.rublevskaya.pinkeditor.algorithms.lab3;

import by.rublevskaya.pinkeditor.model.Pixel;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class BSplineAlgorithm implements ParametricCurveAlgorithm {
    @Override
    public List<Pixel> calculate(int[][] controlPoints, DefaultTableModel tableModel) {
        List<Pixel> pixels = new ArrayList<>();
        if (controlPoints.length < 4) return pixels;

        int numSegments = controlPoints.length - 3;

        for (int seg = 0; seg < numSegments; seg++) {
            int x0 = controlPoints[seg][0],     y0 = controlPoints[seg][1];
            int x1 = controlPoints[seg + 1][0], y1 = controlPoints[seg + 1][1];
            int x2 = controlPoints[seg + 2][0], y2 = controlPoints[seg + 2][1];
            int x3 = controlPoints[seg + 3][0], y3 = controlPoints[seg + 3][1];

            tableModel.addRow(new Object[]{"Сегмент " + (seg + 1), "-", "-", "-", "-"});

            int steps = 20;
            for (int i = 0; i <= steps; i++) {
                double t = i / (double) steps;
                double t2 = t * t;
                double t3 = t2 * t;

                double s1 = (-t3 + 3*t2 - 3*t + 1) / 6.0;
                double s2 = (3*t3 - 6*t2 + 4) / 6.0;
                double s3 = (-3*t3 + 3*t2 + 3*t + 1) / 6.0;
                double s4 = t3 / 6.0;

                double x = s1 * x0 + s2 * x1 + s3 * x2 + s4 * x3;
                double y = s1 * y0 + s2 * y1 + s3 * y2 + s4 * y3;

                pixels.add(new Pixel((int)Math.round(x), (int)Math.round(y), 1.0f));

                if (i % 10 == 0) {
                    tableModel.addRow(new Object[]{
                            String.format("  t=%.2f", t), "Точка",
                            String.format("(%.1f, %.1f)", x, y),
                            String.format("%.3f, %.3f", s1, s2),
                            String.format("%.3f, %.3f", s3, s4)
                    });
                }
            }
        }
        return pixels;
    }

    @Override
    public int getRequiredPoints() { return -1; }

    @Override
    public String[] getPointLabels() {
        return new String[]{"Вершины (минимум 4)"};
    }
}