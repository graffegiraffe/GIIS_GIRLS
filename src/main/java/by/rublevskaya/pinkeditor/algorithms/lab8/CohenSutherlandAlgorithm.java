package by.rublevskaya.pinkeditor.algorithms.lab8;

public class CohenSutherlandAlgorithm {

    public static final int INSIDE = 0;
    public static final int LEFT   = 1;
    public static final int RIGHT  = 2;
    public static final int BOTTOM = 4;
    public static final int TOP    = 8;

    private final double xMin, yMin, xMax, yMax;

    public CohenSutherlandAlgorithm(double xMin, double yMin, double xMax, double yMax) {
        this.xMin = xMin; this.yMin = yMin;
        this.xMax = xMax; this.yMax = yMax;
    }

    public int computeOutcode(double x, double y) {
        int code = INSIDE;
        if      (x < xMin) code |= LEFT;
        else if (x > xMax) code |= RIGHT;
        if      (y < yMin) code |= BOTTOM;
        else if (y > yMax) code |= TOP;
        return code;
    }

    public static class ClipResult {
        public boolean visible;
        public boolean clipped;
        public double x1, y1, x2, y2;
        public int outcode1, outcode2;
        public String status;
    }

    public ClipResult clip(double x1, double y1, double x2, double y2) {
        ClipResult result = new ClipResult();
        result.outcode1 = computeOutcode(x1, y1);
        result.outcode2 = computeOutcode(x2, y2);

        double cx1 = x1, cy1 = y1, cx2 = x2, cy2 = y2;
        int out1 = result.outcode1;
        int out2 = result.outcode2;

        while (true) {
            if ((out1 | out2) == 0) {
                result.visible  = true;
                result.x1 = cx1; result.y1 = cy1;
                result.x2 = cx2; result.y2 = cy2;
                result.clipped  = (result.outcode1 != 0 || result.outcode2 != 0);
                result.status   = result.clipped ? "Отсечён (частично видим)" : "Целиком видим";
                return result;
            } else if ((out1 & out2) != 0) {
                result.visible = false;
                result.x1 = x1; result.y1 = y1;
                result.x2 = x2; result.y2 = y2;
                result.status  = "Целиком невидим";
                return result;
            } else {
                int outcodeOut = (out1 != 0) ? out1 : out2;
                double ix = 0, iy = 0;

                if ((outcodeOut & TOP) != 0) {
                    ix = cx1 + (cx2 - cx1) * (yMax - cy1) / (cy2 - cy1);
                    iy = yMax;
                } else if ((outcodeOut & BOTTOM) != 0) {
                    ix = cx1 + (cx2 - cx1) * (yMin - cy1) / (cy2 - cy1);
                    iy = yMin;
                } else if ((outcodeOut & RIGHT) != 0) {
                    iy = cy1 + (cy2 - cy1) * (xMax - cx1) / (cx2 - cx1);
                    ix = xMax;
                } else {
                    iy = cy1 + (cy2 - cy1) * (xMin - cx1) / (cx2 - cx1);
                    ix = xMin;
                }

                if (outcodeOut == out1) {
                    cx1 = ix; cy1 = iy;
                    out1 = computeOutcode(cx1, cy1);
                } else {
                    cx2 = ix; cy2 = iy;
                    out2 = computeOutcode(cx2, cy2);
                }
            }
        }
    }

    public static String outcodeString(int code) {
        return String.format("%s%s%s%s",
            (code & TOP)    != 0 ? "В" : "0",
            (code & BOTTOM) != 0 ? "Н" : "0",
            (code & RIGHT)  != 0 ? "П" : "0",
            (code & LEFT)   != 0 ? "Л" : "0"
        );
    }
}