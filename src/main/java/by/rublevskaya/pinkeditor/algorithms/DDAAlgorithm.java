package by.rublevskaya.pinkeditor.algorithms;

import by.rublevskaya.pinkeditor.model.Pixel;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class DDAAlgorithm implements LineAlgorithm {
    @Override
    public List<Pixel> calculate(int x1, int y1, int x2, int y2, DefaultTableModel tableModel) {
        List<Pixel> result = new ArrayList<>();
        int length = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        float dx = (float)(x2 - x1) / length;
        float dy = (float)(y2 - y1) / length;
        float x = x1 + 0.5f * Math.signum(dx);
        float y = y1 + 0.5f * Math.signum(dy);

        addPixel(result, tableModel, 0, x, y, "1.0");

        for(int i = 1; i <= length; i++) {
            x += dx;
            y += dy;
            addPixel(result, tableModel, i, x, y, "1.0");
        }
        return result;
    }

    private void addPixel(List<Pixel> list, DefaultTableModel model, int step, float x, float y, String intensity) {
        list.add(new Pixel((int)x, (int)y, Float.parseFloat(intensity)));
        model.addRow(new Object[]{ step, String.format("%.2f", x), String.format("%.2f", y), "-", String.format("(%d, %d)", (int)x, (int)y), intensity });
    }
}
