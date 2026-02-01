package by.rublevskaya.pinkeditor.algorithms.lab2;

import by.rublevskaya.pinkeditor.model.Pixel;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class ParabolaAlgorithm implements CurveAlgorithm {
    @Override
    public List<Pixel> calculate(int xc, int yc, int[] params, DefaultTableModel tableModel) {
        List<Pixel> pixels = new ArrayList<>();
        int p = params[0]; //y^2 = 2px
        int x = 0;
        int y = 0;

        int limit = 100;

        //регион 1, наклон < 1
        int delta = 1 - p;

        int step = 0;
        logStep(tableModel, step, delta, "Start", x, y, "-");
        addPixels(pixels, xc, yc, x, y);

        //первый регион
        while (y < p && x < limit) {
            step++;
            int prev_delta = delta;
            int x_old = x;
            int y_old = y;

            if (delta >= 0) {
                x++;
                delta = delta - 2 * p;
            }
            y++;
            delta = delta + 2 * y + 1;

            String type = (x > x_old) ? "D" : "V"; //если x изменился - диагональ, иначе вертикаль (y^2=2px)

            logStep(tableModel, step, prev_delta, type, x, y, String.valueOf(delta));
            addPixels(pixels, xc, yc, x, y);
        }

        //второй регион
        while (x < limit) {
            step++;
            int prev_delta = delta;
            int x_old = x;

            if (delta < 0) {
                y++;
                delta = delta + 2 * y + 1;
            }
            x++;
            delta = delta - 2 * p;

            String type = (y > (y-1)) ? "D" : "H";

            logStep(tableModel, step, prev_delta, type, x, y, String.valueOf(delta));
            addPixels(pixels, xc, yc, x, y);
        }

        return pixels;
    }

    private void addPixels(List<Pixel> pixels, int xc, int yc, int x, int y) {
        pixels.add(new Pixel(xc + x, yc + y, 1.0f));
        pixels.add(new Pixel(xc + x, yc - y, 1.0f));
    }

    private void logStep(DefaultTableModel model, int step, int delta, String type, int x, int y, String nextDelta) {
        model.addRow(new Object[]{
                step, delta, "-", "-", type, x, y, nextDelta, String.format("(%d, %d)", x, y)
        });
    }
}