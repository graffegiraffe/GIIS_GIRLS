package by.rublevskaya.pinkeditor.model.geom3d;

public class Point3D {
    public double x, y, z, w;

    public Point3D(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z; this.w = 1.0;
    }

    public Point3D(double x, double y, double z, double w) {
        this.x = x; this.y = y; this.z = z; this.w = w;
    }

    public Point3D transform(Matrix4x4 m) {
        double nx = x * m.mat[0][0] + y * m.mat[1][0] + z * m.mat[2][0] + w * m.mat[3][0];
        double ny = x * m.mat[0][1] + y * m.mat[1][1] + z * m.mat[2][1] + w * m.mat[3][1];
        double nz = x * m.mat[0][2] + y * m.mat[1][2] + z * m.mat[2][2] + w * m.mat[3][2];
        double nw = x * m.mat[0][3] + y * m.mat[1][3] + z * m.mat[2][3] + w * m.mat[3][3];
        return new Point3D(nx, ny, nz, nw);
    }

    public Point3D normalize() {
        double safeW = w;
        if (safeW <= 0.01) {
            safeW = 0.01;
        }

        if (safeW != 0 && safeW != 1) {
            return new Point3D(x / safeW, y / safeW, z / safeW, 1.0);
        }
        return new Point3D(x, y, z, safeW);
    }
}