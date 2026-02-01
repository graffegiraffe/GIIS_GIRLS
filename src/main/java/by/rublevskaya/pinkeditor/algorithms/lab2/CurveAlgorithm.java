package by.rublevskaya.pinkeditor.algorithms.lab2;

import by.rublevskaya.pinkeditor.model.Pixel;
import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * params - массив параметров (R для окружности; A, B для эллипса/гиперболы; P для параболы).
 */
public interface CurveAlgorithm {
    List<Pixel> calculate(int xc, int yc, int[] params, DefaultTableModel tableModel);
}
