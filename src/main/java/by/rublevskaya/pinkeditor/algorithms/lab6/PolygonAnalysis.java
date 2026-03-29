package by.rublevskaya.pinkeditor.algorithms.lab6;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
public class PolygonAnalysis {
    public static class Normal {
        public final double midX, midY;
        public final double nx, ny;

        public Normal(double midX, double midY, double nx, double ny) {
            this.midX = midX; this.midY = midY;
            this.nx = nx;     this.ny = ny;
        }
    }
    public static boolean isConvex(List<Point> vertices) {
        int n = vertices.size();
        if (n < 3) return false;

        int sign = 0;
        for (int i = 0; i < n; i++) {
            Point O = vertices.get(i);
            Point A = vertices.get((i + 1) % n);
            Point B = vertices.get((i + 2) % n);
            long c = cross(O, A, B);
            if (c != 0) {
                int s = (c > 0) ? 1 : -1;
                if (sign == 0) sign = s;
                else if (sign != s) return false;
            }
        }
        return true;
    }
    public static List<Normal> getInnerNormals(List<Point> vertices) {
        int n = vertices.size();
        List<Normal> normals = new ArrayList<>();
        if (n < 3) return normals;
        double area2 = signedArea2(vertices);

        for (int i = 0; i < n; i++) {
            Point a = vertices.get(i);
            Point b = vertices.get((i + 1) % n);

            double midX = (a.x + b.x) / 2.0;
            double midY = (a.y + b.y) / 2.0;

            double dx = b.x - a.x;
            double dy = b.y - a.y;
            double len = Math.hypot(dx, dy);
            if (len == 0) continue;
            double nx = -dy / len;
            double ny =  dx / len;
            if (area2 < 0) { nx = -nx; ny = -ny; }

            normals.add(new Normal(midX, midY, nx, ny));
        }
        return normals;
    }
    public static boolean isPointInside(List<Point> vertices, Point p) {
        int n = vertices.size();
        if (n < 3) return false;

        int crossings = 0;
        double px = p.x, py = p.y;

        for (int i = 0; i < n; i++) {
            Point a = vertices.get(i);
            Point b = vertices.get((i + 1) % n);

            double ax = a.x, ay = a.y;
            double bx = b.x, by = b.y;

            if (((ay <= py && by > py) || (by <= py && ay > py)) &&
                    (px < (bx - ax) * (py - ay) / (by - ay) + ax)) {
                crossings++;
            }
        }
        return (crossings % 2) == 1;
    }
    public static List<Point> segmentIntersections(List<Point> vertices, Point p1, Point p2) {
        int n = vertices.size();
        List<Point> result = new ArrayList<>();
        if (n < 2) return result;

        for (int i = 0; i < n; i++) {
            Point a = vertices.get(i);
            Point b = vertices.get((i + 1) % n);
            Point inter = segmentIntersect(p1, p2, a, b);
            if (inter != null) result.add(inter);
        }
        return result;
    }
    public static boolean segmentIntersects(List<Point> vertices, Point p1, Point p2) {
        return !segmentIntersections(vertices, p1, p2).isEmpty();
    }
    private static long cross(Point O, Point A, Point B) {
        return (long)(A.x - O.x) * (B.y - O.y) - (long)(A.y - O.y) * (B.x - O.x);
    }

    private static double signedArea2(List<Point> v) {
        double area = 0;
        int n = v.size();
        for (int i = 0; i < n; i++) {
            Point a = v.get(i);
            Point b = v.get((i + 1) % n);
            area += (double)(a.x) * b.y - (double)(b.x) * a.y;
        }
        return area;
    }
    private static Point segmentIntersect(Point p1, Point p2, Point p3, Point p4) {
        double d1x = p2.x - p1.x, d1y = p2.y - p1.y;
        double d2x = p4.x - p3.x, d2y = p4.y - p3.y;

        double denom = d1x * d2y - d1y * d2x;
        if (Math.abs(denom) < 1e-10) return null;

        double t = ((p3.x - p1.x) * d2y - (p3.y - p1.y) * d2x) / denom;
        double u = ((p3.x - p1.x) * d1y - (p3.y - p1.y) * d1x) / denom;

        if (t >= 0 && t <= 1 && u >= 0 && u <= 1) {
            int ix = (int) Math.round(p1.x + t * d1x);
            int iy = (int) Math.round(p1.y + t * d1y);
            return new Point(ix, iy);
        }
        return null;
    }
}