package by.rublevskaya.pinkeditor.algorithms.lab1;

import by.rublevskaya.pinkeditor.model.Pixel;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class BresenhamAlgorithm implements LineAlgorithm {
    @Override
    public List<Pixel> calculate(int x1, int y1, int x2, int y2, DefaultTableModel tableModel) {
        List<Pixel> result = new ArrayList<>();
        int x = x1, y = y1;
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int s1 = Integer.signum(x2 - x1), s2 = Integer.signum(y2 - y1);
        boolean swap = false;

        if (dy > dx) { int t = dx; dx = dy; dy = t; swap = true; }
        int e = 2 * dy - dx;

        addPixel(result, tableModel, 0, x, y, e);

        for (int i = 1; i <= dx; i++) {
            if (e >= 0) {
                if (swap) x += s1; else y += s2;
                e -= 2 * dx;
            }
            if (swap) y += s2; else x += s1;
            e += 2 * dy;

            addPixel(result, tableModel, i, x, y, e);
        }
        return result;
    }

    private void addPixel(List<Pixel> list, DefaultTableModel model, int step, int x, int y, int e) {
        list.add(new Pixel(x, y, 1.0f));
        model.addRow(new Object[]{ step, String.valueOf(x), String.valueOf(y), String.valueOf(e), String.format("(%d, %d)", x, y), "1.0" });
    }
}
