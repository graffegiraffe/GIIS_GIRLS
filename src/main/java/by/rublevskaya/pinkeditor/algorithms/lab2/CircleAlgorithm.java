package by.rublevskaya.pinkeditor.algorithms.lab2;

import by.rublevskaya.pinkeditor.model.Pixel;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class CircleAlgorithm implements CurveAlgorithm {
    @Override
    public List<Pixel> calculate(int xc, int yc, int[] params, DefaultTableModel tableModel) {
        List<Pixel> pixels = new ArrayList<>();
        int R = params[0];

        int x = 0;
        int y = R;
        // изачальная ошибка: 2 - 2R
        int delta = 2 * (1 - R);

        int step = 0;

        String plotStr = String.format("(%d, %d)", x, y);

        tableModel.addRow(new Object[]{
                step,
                "",
                "", "", "",
                x, y,
                delta,
                plotStr
        });

        addSymmetricPixels(pixels, xc, yc, x, y);

        while (y > 0) {
            step++;
            int next_x, next_y, next_delta;
            String pixelType, d_str = "", d_star_str = "";

            if (delta < 0) {
                // А
                int d = 2 * (delta + y) - 1;
                d_str = String.valueOf(d);

                if (d <= 0) {
                    //горизонт. шаг H
                    pixelType = "H";
                    next_x = x + 1;
                    next_y = y;
                    next_delta = delta + 2 * next_x + 1;
                } else {
                    //диагонал. шаг D
                    pixelType = "D";
                    next_x = x + 1;
                    next_y = y - 1;
                    next_delta = delta + 2 * next_x - 2 * next_y + 2;
                }
            } else if (delta > 0) {
                //Б
                int d_star = 2 * (delta - x) - 1;
                d_star_str = String.valueOf(d_star);

                if (d_star <= 0) {
                    //диагонал. шаг D
                    pixelType = "D";
                    next_x = x + 1;
                    next_y = y - 1;
                    next_delta = delta + 2 * next_x - 2 * next_y + 2;
                } else {
                    //вертикал. шаг V
                    pixelType = "V";
                    next_x = x;
                    next_y = y - 1;
                    next_delta = delta - 2 * next_y + 1;
                }
            } else {
                //В. delta == 0
                pixelType = "D";
                next_x = x + 1;
                next_y = y - 1;
                next_delta = delta + 2 * next_x - 2 * next_y + 2;
            }

            plotStr = String.format("(%d, %d)", next_x, next_y);

            tableModel.addRow(new Object[]{
                    step,
                    delta,        // Del_i ошибка
                    d_str,        // d
                    d_star_str,   // d*
                    pixelType,    // Пиксель
                    next_x,       // X
                    next_y,       // Y
                    next_delta,   // Del_i+1 ошибка следующего шага
                    plotStr       // Plot (x, y)
            });

            x = next_x;
            y = next_y;
            delta = next_delta;
            addSymmetricPixels(pixels, xc, yc, x, y);
        }
        return pixels;
    }

    private void addSymmetricPixels(List<Pixel> pixels, int xc, int yc, int x, int y) {
        pixels.add(new Pixel(xc + x, yc + y, 1.0f));
        pixels.add(new Pixel(xc - x, yc + y, 1.0f));
        pixels.add(new Pixel(xc + x, yc - y, 1.0f));
        pixels.add(new Pixel(xc - x, yc - y, 1.0f));
        pixels.add(new Pixel(xc + y, yc + x, 1.0f));
        pixels.add(new Pixel(xc - y, yc + x, 1.0f));
        pixels.add(new Pixel(xc + y, yc - x, 1.0f));
        pixels.add(new Pixel(xc - y, yc - x, 1.0f));
    }
}