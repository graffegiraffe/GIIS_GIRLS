package by.rublevskaya.pinkeditor.algorithms.lab3;

import by.rublevskaya.pinkeditor.model.Pixel;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class HermiteAlgorithm implements ParametricCurveAlgorithm {
    @Override
    public List<Pixel> calculate(int[][] controlPoints, DefaultTableModel tableModel) {
        List<Pixel> pixels = new ArrayList<>();
        if (controlPoints.length < 4) return pixels;

        double p1x = controlPoints[0][0];
        double p1y = controlPoints[0][1];
        double p4x = controlPoints[1][0];
        double p4y = controlPoints[1][1];

        double r1x = controlPoints[2][0];
        double r1y = controlPoints[2][1];
        double r4x = controlPoints[3][0];
        double r4y = controlPoints[3][1];

        int steps = 100;
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            double t2 = t * t;
            double t3 = t2 * t;

            double h1 = 2*t3 - 3*t2 + 1;    // P1
            double h2 = -2*t3 + 3*t2;       // P4
            double h3 = t3 - 2*t2 + t;      // R1
            double h4 = t3 - t2;            // R4

            double x = h1 * p1x + h2 * p4x + h3 * r1x + h4 * r4x;
            double y = h1 * p1y + h2 * p4y + h3 * r1y + h4 * r4y;

            pixels.add(new Pixel((int)Math.round(x), (int)Math.round(y), 1.0f));

            if (i % 10 == 0) {
                tableModel.addRow(new Object[]{
                        String.format("t=%.1f", t), "P(t)",
                        String.format("(%.1f, %.1f)", x, y),
                        String.format("h1,2: %.2f; %.2f", h1, h2),
                        String.format("h3,4: %.2f; %.2f", h3, h4)
                });
            }
        }
        return pixels;
    }

    @Override
    public int getRequiredPoints() { return 4; }

    @Override
    public String[] getPointLabels() {
        return new String[]{"P1 (Старт)", "P4 (Финиш)", "R1 (Вектор)", "R4 (Вектор)"};
    }
}