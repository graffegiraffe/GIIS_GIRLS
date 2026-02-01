package by.rublevskaya.pinkeditor.algorithms.lab2;

import by.rublevskaya.pinkeditor.model.Pixel;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class EllipseAlgorithm implements CurveAlgorithm {
    @Override
    public List<Pixel> calculate(int xc, int yc, int[] params, DefaultTableModel tableModel) {
        List<Pixel> pixels = new ArrayList<>();
        int a = params[0];
        int b = params[1];

        long a2 = (long)a * a;
        long b2 = (long)b * b;

        int x = 0;
        int y = b;
        long delta = a2 + b2 - 2 * a2 * b;

        int step = 0;
        String plotStr = String.format("(%d, %d)", x, y);
        tableModel.addRow(new Object[]{step, delta, "", "", "", x, y, "", plotStr});
        addQuadPixels(pixels, xc, yc, x, y);

        while (y > 0) {
            step++;
            int next_x, next_y;
            long next_delta;
            String pixelType, d_str = "", d_star_str = "";

            if (delta < 0) {
                long d = 2 * (delta + a2 * y) - 1;
                d_str = String.valueOf(d);
                if (d <= 0) { //H
                    pixelType = "H";
                    next_x = x + 1;
                    next_y = y;
                    next_delta = delta + b2 * (2L * next_x + 1);
                } else { //D
                    pixelType = "D";
                    next_x = x + 1;
                    next_y = y - 1;
                    next_delta = delta + b2 * (2L * next_x + 1) + a2 * (1 - 2L * y); //тут y старый, по формуле
                }
            } else if (delta > 0) {
                long d_star = 2 * (delta - b2 * x) - 1;
                d_star_str = String.valueOf(d_star);
                if (d_star <= 0) { //D
                    pixelType = "D";
                    next_x = x + 1;
                    next_y = y - 1;
                    next_delta = delta + b2 * (2L * next_x + 1) + a2 * (1 - 2L * y);
                } else { //V
                    pixelType = "V";
                    next_x = x;
                    next_y = y - 1;
                    next_delta = delta + a2 * (1 - 2L * y);
                }
            } else {
                pixelType = "D";
                next_x = x + 1;
                next_y = y - 1;
                next_delta = delta + b2 * (2L * next_x + 1) + a2 * (1 - 2L * y);
            }

            plotStr = String.format("(%d, %d)", next_x, next_y);
            tableModel.addRow(new Object[]{step, delta, d_str, d_star_str, pixelType, next_x, next_y, next_delta, plotStr});

            x = next_x;
            y = next_y;
            delta = next_delta;
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
}