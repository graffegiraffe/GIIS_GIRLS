package by.rublevskaya.pinkeditor.algorithms;

import by.rublevskaya.pinkeditor.model.Pixel;

import java.util.List;
import javax.swing.table.DefaultTableModel;

public interface LineAlgorithm {
    List<Pixel> calculate(int x1, int y1, int x2, int y2, DefaultTableModel tableModel);
}
