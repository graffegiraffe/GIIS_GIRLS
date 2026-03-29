package by.rublevskaya.pinkeditor.algorithms.lab7;

import javax.swing.table.DefaultTableModel;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Триангуляция Делоне — алгоритм Боуера–Уотсона (Bowyer-Watson).
 *
 * Шаги:
 *  1. Добавить супер-треугольник, охватывающий все точки.
 *  2. Для каждой точки:
 *     a. Найти все треугольники, чья описанная окружность содержит точку («плохие»).
 *     b. Найти граничный многоугольник «плохих» треугольников (рёбра, не общие с другими плохими).
 *     c. Удалить «плохие» треугольники.
 *     d. Создать новые треугольники из граничных рёбер и текущей точки.
 *  3. Удалить треугольники, имеющие общие вершины с супер-треугольником.
 */
public class DelaunayTriangulation {

    /**
     * Результат одного шага анимации.
     */
    public static class Step {
        public final List<Triangle>       triangulation; // текущие треугольники
        public final Point2D.Double       addedPoint;    // только что добавленная точка
        public final List<Triangle>       badTriangles;  // «плохие» треугольники этого шага
        public final String               description;

        public Step(List<Triangle> triangulation, Point2D.Double addedPoint,
                    List<Triangle> bad, String desc) {
            this.triangulation = new ArrayList<>(triangulation);
            this.addedPoint    = addedPoint;
            this.badTriangles  = new ArrayList<>(bad);
            this.description   = desc;
        }
    }

    /**
     * Выполняет триангуляцию Делоне.
     *
     * @param inputPoints набор точек
     * @param tableModel  таблица для лога (может быть null)
     * @return список шагов алгоритма
     */
    public static List<Step> compute(List<Point2D.Double> inputPoints,
                                     DefaultTableModel tableModel) {
        List<Step> steps = new ArrayList<>();
        if (inputPoints.size() < 3) return steps;

        // 1. Создать супер-треугольник
        double minX = inputPoints.stream().mapToDouble(p -> p.x).min().orElse(0);
        double minY = inputPoints.stream().mapToDouble(p -> p.y).min().orElse(0);
        double maxX = inputPoints.stream().mapToDouble(p -> p.x).max().orElse(1);
        double maxY = inputPoints.stream().mapToDouble(p -> p.y).max().orElse(1);

        double dx = maxX - minX, dy = maxY - minY;
        double deltaMax = Math.max(dx, dy) * 3;
        double midX = (minX + maxX) / 2.0, midY = (minY + maxY) / 2.0;

        Point2D.Double st1 = new Point2D.Double(midX - 2 * deltaMax, midY - deltaMax);
        Point2D.Double st2 = new Point2D.Double(midX,                 midY + 2 * deltaMax);
        Point2D.Double st3 = new Point2D.Double(midX + 2 * deltaMax, midY - deltaMax);

        List<Triangle> triangles = new ArrayList<>();
        Triangle superTriangle = new Triangle(st1, st2, st3);
        triangles.add(superTriangle);

        int stepNum = 1;

        // 2. Добавлять точки по одной
        for (Point2D.Double point : inputPoints) {
            List<Triangle> bad     = new ArrayList<>();
            List<Triangle> notBad  = new ArrayList<>();

            // a. Найти «плохие» треугольники
            for (Triangle t : triangles) {
                if (t.circumCircleContains(point)) bad.add(t);
                else                               notBad.add(t);
            }

            // b. Найти граничный многоугольник (рёбра, принадлежащие ровно одному плохому)
            List<Point2D.Double[]> boundary = new ArrayList<>();
            for (Triangle t : bad) {
                Point2D.Double[][] edges = {
                        {t.a, t.b}, {t.b, t.c}, {t.c, t.a}
                };
                for (Point2D.Double[] edge : edges) {
                    boolean shared = false;
                    for (Triangle other : bad) {
                        if (other == t) continue;
                        if (other.hasEdge(edge[0], edge[1])) { shared = true; break; }
                    }
                    if (!shared) boundary.add(edge);
                }
            }

            // c. Удалить плохие треугольники
            triangles = notBad;

            // d. Создать новые треугольники
            List<Triangle> newTris = new ArrayList<>();
            for (Point2D.Double[] edge : boundary) {
                Triangle nt = new Triangle(edge[0], edge[1], point);
                newTris.add(nt);
                triangles.add(nt);
            }

            // Запись шага
            List<Triangle> snapshot = new ArrayList<>(triangles);
            steps.add(new Step(snapshot, point, bad,
                    "Точка (" + (int)point.x + "," + (int)point.y +
                    "): плохих=" + bad.size() + ", новых=" + newTris.size()));

            if (tableModel != null) {
                tableModel.addRow(new Object[]{
                        stepNum++,
                        String.format("(%.0f, %.0f)", point.x, point.y),
                        bad.size(),
                        newTris.size(),
                        triangles.size()
                });
            }
        }

        // 3. Удалить треугольники, связанные с супер-треугольником
        List<Triangle> finalTriangles = new ArrayList<>();
        for (Triangle t : triangles) {
            if (!t.hasVertex(st1) && !t.hasVertex(st2) && !t.hasVertex(st3)) {
                finalTriangles.add(t);
            }
        }

        // Добавить финальный шаг
        steps.add(new Step(finalTriangles, null, new ArrayList<>(), "Финал: суперт-треугольник удалён"));
        if (tableModel != null) {
            tableModel.addRow(new Object[]{
                    stepNum, "—", 0, 0, finalTriangles.size()
            });
        }

        return steps;
    }

    public static String[] getTableColumns() {
        return new String[]{"Шаг", "Точка", "Плохих △", "Новых △", "Всего △"};
    }
}
