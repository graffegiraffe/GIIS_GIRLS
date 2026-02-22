package by.rublevskaya.pinkeditor.algorithms.lab3;

import by.rublevskaya.pinkeditor.model.Pixel;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public interface ParametricCurveAlgorithm {
    List<Pixel> calculate(int[][] controlPoints, DefaultTableModel tableModel);
    int getRequiredPoints();
    String[] getPointLabels();
}