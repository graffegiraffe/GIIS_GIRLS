package by.rublevskaya.pinkeditor.algorithms.lab3;

import by.rublevskaya.pinkeditor.model.Pixel;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class BezierAlgorithm implements ParametricCurveAlgorithm {
    @Override
    public List<Pixel> calculate(int[][] controlPoints, DefaultTableModel tableModel) {
        List<Pixel> pixels = new ArrayList<>();
        if (controlPoints.length < 4) return pixels;

        int x1 = controlPoints[0][0], y1 = controlPoints[0][1]; // P1
        int x2 = controlPoints[1][0], y2 = controlPoints[1][1]; // P2
        int x3 = controlPoints[2][0], y3 = controlPoints[2][1]; // P3
        int x4 = controlPoints[3][0], y4 = controlPoints[3][1]; // P4

        int steps = 50;
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            double mt = 1.0 - t;

            // P(t) = (1-t)³P1 + 3t(1-t)²P2 + 3t²(1-t)P3 + t³P4
            double b1 = mt * mt * mt;
            double b2 = 3 * t * mt * mt;
            double b3 = 3 * t * t * mt;
            double b4 = t * t * t;

            double x = b1 * x1 + b2 * x2 + b3 * x3 + b4 * x4;
            double y = b1 * y1 + b2 * y2 + b3 * y3 + b4 * y4;

            pixels.add(new Pixel((int)Math.round(x), (int)Math.round(y), 1.0f));

            if (i % 5 == 0) {
                tableModel.addRow(new Object[]{
                        String.format("t=%.2f", t), "Кривая",
                        String.format("(%.2f, %.2f)", x, y),
                        String.format("b1=%.3f, b2=%.3f", b1, b2),
                        String.format("b3=%.3f, b4=%.3f", b3, b4)
                });
            }
        }
        return pixels;
    }

    @Override
    public int getRequiredPoints() { return 4; }

    @Override
    public String[] getPointLabels() {
        return new String[]{"P1 (Концевая)", "P2 (Опорная)", "P3 (Опорная)", "P4 (Концевая)"};
    }
}