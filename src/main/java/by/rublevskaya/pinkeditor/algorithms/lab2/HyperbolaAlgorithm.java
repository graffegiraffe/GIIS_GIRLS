package by.rublevskaya.pinkeditor.algorithms.lab2;

import by.rublevskaya.pinkeditor.model.Pixel;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class HyperbolaAlgorithm implements CurveAlgorithm {
    @Override
    public List<Pixel> calculate(int xc, int yc, int[] params, DefaultTableModel tableModel) {
        List<Pixel> pixels = new ArrayList<>();
        int a = params[0];
        int b = params[1];

        int x = a;
        int y = 0;

        int maxStep = 100;

        int step = 0;
        logStep(tableModel, step, x, y);
        addQuadPixels(pixels, xc, yc, x, y);

        for (int i = 0; i < maxStep; i++) {
            step++;
            y++;
            // x = a * sqrt(1 + y^2/b^2)
            double xVal = a * Math.sqrt(1 + (double)(y*y)/(b*b));
            x = (int) Math.round(xVal);

            logStep(tableModel, step, x, y);
            addQuadPixels(pixels, xc, yc, x, y);
        }

        return pixels;
    }

    private void addQuadPixels(List<Pixel> pixels, int xc, int yc, int x, int y) {
        pixels.add(new Pixel(xc + x, yc + y, 1.0f));
        pixels.add(new Pixel(xc - x, yc + y, 1.0f));
        pixels.add(new Pixel(xc + x, yc - y, 1.0f));
        pixels.add(new Pixel(xc - x, yc - y, 1.0f));
    }

    private void logStep(DefaultTableModel model, int step, int x, int y) {
        model.addRow(new Object[]{
                step, "-", "-", "-", "Calc", x, y, "-", String.format("(%d, %d)", x, y)
        });
    }
}