package by.rublevskaya.pinkeditor.algorithms.lab7;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Диаграмма Вороного, построенная как двойственная к триангуляции Делоне.
 *
 * Каждое ребро Делоне (общее двум треугольникам) соответствует ребру Вороного —
 * отрезку между центрами описанных окружностей этих двух треугольников.
 * Рёбра, принадлежащие только одному треугольнику (граничные), продолжаются
 * до края области.
 */
public class VoronoiDiagram {

    /** Ребро диаграммы Вороного: отрезок [p1, p2]. */
    public static class VoronoiEdge {
        public final Point2D.Double p1, p2;
        public VoronoiEdge(Point2D.Double p1, Point2D.Double p2) {
            this.p1 = p1; this.p2 = p2;
        }
    }

    /**
     * Строит диаграмму Вороного по готовой триангуляции Делоне.
     *
     * @param triangles конечный список треугольников Делоне
     * @param canvasW   ширина холста (для продления граничных рёбер)
     * @param canvasH   высота холста
     * @return список рёбер Вороного
     */
    public static List<VoronoiEdge> build(List<Triangle> triangles, double canvasW, double canvasH) {
        List<VoronoiEdge> edges = new ArrayList<>();
        int n = triangles.size();

        for (int i = 0; i < n; i++) {
            Triangle t1 = triangles.get(i);
            boolean foundAB = false, foundBC = false, foundCA = false;

            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                Triangle t2 = triangles.get(j);

                if (!foundAB && t2.hasEdge(t1.a, t1.b)) {
                    addEdge(edges, t1.circumCenter, t2.circumCenter, canvasW, canvasH);
                    foundAB = true;
                }
                if (!foundBC && t2.hasEdge(t1.b, t1.c)) {
                    addEdge(edges, t1.circumCenter, t2.circumCenter, canvasW, canvasH);
                    foundBC = true;
                }
                if (!foundCA && t2.hasEdge(t1.c, t1.a)) {
                    addEdge(edges, t1.circumCenter, t2.circumCenter, canvasW, canvasH);
                    foundCA = true;
                }
            }

            // Граничные рёбра (не имеют соседа) — продлить луч от центра описанной окружности
            if (!foundAB) addBoundaryRay(edges, t1, t1.a, t1.b, canvasW, canvasH);
            if (!foundBC) addBoundaryRay(edges, t1, t1.b, t1.c, canvasW, canvasH);
            if (!foundCA) addBoundaryRay(edges, t1, t1.c, t1.a, canvasW, canvasH);
        }

        return edges;
    }

    // ---------- helpers ----------

    private static void addEdge(List<VoronoiEdge> edges,
                                  Point2D.Double p1, Point2D.Double p2,
                                  double w, double h) {
        // Избегать дублей и вырожденных рёбер
        if (Triangle.samePoint(p1, p2)) return;
        for (VoronoiEdge e : edges) {
            if ((Triangle.samePoint(e.p1, p1) && Triangle.samePoint(e.p2, p2)) ||
                (Triangle.samePoint(e.p1, p2) && Triangle.samePoint(e.p2, p1))) return;
        }
        edges.add(new VoronoiEdge(p1, p2));
    }

    /**
     * Граничное ребро Делоне: у треугольника нет соседа через ребро (p1→p2).
     * Строим луч от circumCenter треугольника в направлении, перпендикулярном ребру,
     * наружу от треугольника.
     */
    private static void addBoundaryRay(List<VoronoiEdge> edges, Triangle t,
                                        Point2D.Double p1, Point2D.Double p2,
                                        double w, double h) {
        // Середина ребра
        double mx = (p1.x + p2.x) / 2.0;
        double my = (p1.y + p2.y) / 2.0;

        // Направление ребра
        double ex = p2.x - p1.x, ey = p2.y - p1.y;
        // Нормаль (перпендикуляр)
        double nx = -ey, ny = ex;

        // Вектор от середины ребра к circumCenter
        double cx = t.circumCenter.x - mx;
        double cy = t.circumCenter.y - my;

        // Направляем нормаль внутрь треугольника (от circumCenter наружу)
        if (nx * cx + ny * cy > 0) { nx = -nx; ny = -ny; }

        double len = Math.hypot(nx, ny);
        if (len < 1e-10) return;
        nx /= len; ny /= len;

        // Продлить луч до края canvas
        double far = Math.max(w, h) * 3;
        Point2D.Double end = new Point2D.Double(
                t.circumCenter.x + nx * far,
                t.circumCenter.y + ny * far);

        edges.add(new VoronoiEdge(t.circumCenter, end));
    }
}
