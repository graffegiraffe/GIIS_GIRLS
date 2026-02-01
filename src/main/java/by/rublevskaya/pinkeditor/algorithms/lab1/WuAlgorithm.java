package by.rublevskaya.pinkeditor.algorithms.lab1;

import by.rublevskaya.pinkeditor.model.Pixel;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class WuAlgorithm implements LineAlgorithm {
    @Override
    public List<Pixel> calculate(int x1, int y1, int x2, int y2, DefaultTableModel tableModel) {
        List<Pixel> result = new ArrayList<>();
        int iStep = 0;
        boolean steep = Math.abs(y2 - y1) > Math.abs(x2 - x1);

        if (steep) { int t=x1; x1=y1; y1=t; t=x2; x2=y2; y2=t; }
        if (x1 > x2) { int t=x1; x1=x2; x2=t; t=y1; y1=y2; y2=t; }

        float dx = x2 - x1, dy = y2 - y1;
        float gradient = (dx == 0) ? 1.0f : dy / dx;

        int xend = x1;
        float yend = y1 + gradient * (xend - x1);
        float xgap = 1.0f;
        int xpxl1 = xend, ypxl1 = (int)yend;

        addWuPixel(result, steep, xpxl1, ypxl1, (1 - (yend - (int)yend)) * xgap);
        addWuPixel(result, steep, xpxl1, ypxl1 + 1, (yend - (int)yend) * xgap);

        float intery = yend + gradient;
        int xpxl2 = x2;

        for (int x = xpxl1 + 1; x < x2; x++) {
            float bright1 = 1.0f - (intery - (int)intery);
            addWuPixel(result, steep, x, (int)intery, bright1);
            tableModel.addRow(new Object[]{ ++iStep, String.format("%.2f", steep ? intery : x), String.format("%.2f", steep ? x : intery), String.format("%.2f", intery), "Main", String.format("%.2f", bright1) });

            float bright2 = intery - (int)intery;
            addWuPixel(result, steep, x, (int)intery + 1, bright2);
            tableModel.addRow(new Object[]{ ++iStep, String.format("%.2f", steep ? (intery + 1) : x), String.format("%.2f", steep ? x : (intery + 1)), String.format("%.2f", intery), "Sub", String.format("%.2f", bright2) });
            intery += gradient;
        }
        addWuPixel(result, steep, x2, y2, 1.0f);
        return result;
    }

    private void addWuPixel(List<Pixel> list, boolean steep, int x, int y, float brightness) {
        if (steep) list.add(new Pixel(y, x, brightness));
        else list.add(new Pixel(x, y, brightness));
    }
}
