package by.rublevskaya.pinkeditor.algorithms.lab7;

import java.awt.geom.Point2D;

/**
 * Треугольник Делоне.
 * Хранит три вершины и вычисляет описанную окружность (circumcircle).
 */
public class Triangle {

    public final Point2D.Double a, b, c;

    // Описанная окружность
    public final Point2D.Double circumCenter;
    public final double          circumRadiusSq;

    public Triangle(Point2D.Double a, Point2D.Double b, Point2D.Double c) {
        this.a = a;
        this.b = b;
        this.c = c;

        // Вычисление центра описанной окружности
        double ax = a.x, ay = a.y;
        double bx = b.x, by = b.y;
        double cx = c.x, cy = c.y;

        double d = 2 * (ax * (by - cy) + bx * (cy - ay) + cx * (ay - by));
        if (Math.abs(d) < 1e-10) {
            // Вырожденный треугольник
            circumCenter   = new Point2D.Double((ax + bx + cx) / 3.0, (ay + by + cy) / 3.0);
            circumRadiusSq = Double.MAX_VALUE;
        } else {
            double a2 = ax * ax + ay * ay;
            double b2 = bx * bx + by * by;
            double c2 = cx * cx + cy * cy;
            double ux = (a2 * (by - cy) + b2 * (cy - ay) + c2 * (ay - by)) / d;
            double uy = (a2 * (cx - bx) + b2 * (ax - cx) + c2 * (bx - ax)) / d;
            circumCenter   = new Point2D.Double(ux, uy);
            double dx = ax - ux, dy = ay - uy;
            circumRadiusSq = dx * dx + dy * dy;
        }
    }

    /** Проверяет, лежит ли точка p строго внутри описанной окружности. */
    public boolean circumCircleContains(Point2D.Double p) {
        double dx = p.x - circumCenter.x;
        double dy = p.y - circumCenter.y;
        return dx * dx + dy * dy < circumRadiusSq - 1e-10;
    }

    /**
     * Проверяет, является ли ребро (p1, p2) общим с этим треугольником.
     * Порядок вершин неважен.
     */
    public boolean hasEdge(Point2D.Double p1, Point2D.Double p2) {
        return (samePoint(a, p1) && samePoint(b, p2)) ||
               (samePoint(a, p2) && samePoint(b, p1)) ||
               (samePoint(b, p1) && samePoint(c, p2)) ||
               (samePoint(b, p2) && samePoint(c, p1)) ||
               (samePoint(c, p1) && samePoint(a, p2)) ||
               (samePoint(c, p2) && samePoint(a, p1));
    }

    /** Содержит ли треугольник точку p как одну из вершин. */
    public boolean hasVertex(Point2D.Double p) {
        return samePoint(a, p) || samePoint(b, p) || samePoint(c, p);
    }

    public static boolean samePoint(Point2D.Double p1, Point2D.Double p2) {
        return Math.abs(p1.x - p2.x) < 1e-9 && Math.abs(p1.y - p2.y) < 1e-9;
    }

    @Override
    public String toString() {
        return String.format("T[(%.0f,%.0f)(%.0f,%.0f)(%.0f,%.0f)]",
                a.x, a.y, b.x, b.y, c.x, c.y);
    }
}
