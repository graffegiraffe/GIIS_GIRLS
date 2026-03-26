package by.rublevskaya.pinkeditor.algorithms.lab5;

import javax.swing.table.DefaultTableModel;
import java.awt.Point;
import java.util.List;

public interface PolygonFillAlgorithm {
    List<List<Point>> fill(List<Point> vertices, Point seed,
                           int width, int height, DefaultTableModel tableModel);
    String[] getTableColumns();
}