package by.rublevskaya.pinkeditor.model.geom3d;

public class Matrix4x4 {
    public double[][] mat;

    public Matrix4x4() {
        mat = new double[4][4];
    }

    public static Matrix4x4 identity() {
        Matrix4x4 m = new Matrix4x4();
        for (int i = 0; i < 4; i++) m.mat[i][i] = 1.0;
        return m;
    }

    public Matrix4x4 multiply(Matrix4x4 other) {
        Matrix4x4 result = new Matrix4x4();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    result.mat[i][j] += this.mat[i][k] * other.mat[k][j];
                }
            }
        }
        return result;
    }

    public static Matrix4x4 translation(double dx, double dy, double dz) {
        Matrix4x4 m = identity();
        m.mat[3][0] = dx; m.mat[3][1] = dy; m.mat[3][2] = dz;
        return m;
    }

    public static Matrix4x4 scale(double sx, double sy, double sz) {
        Matrix4x4 m = identity();
        m.mat[0][0] = sx; m.mat[1][1] = sy; m.mat[2][2] = sz;
        return m;
    }

    public static Matrix4x4 rotationX(double angleDeg) {
        Matrix4x4 m = identity();
        double rad = Math.toRadians(angleDeg);
        m.mat[1][1] = Math.cos(rad); m.mat[1][2] = Math.sin(rad);
        m.mat[2][1] = -Math.sin(rad); m.mat[2][2] = Math.cos(rad);
        return m;
    }

    public static Matrix4x4 rotationY(double angleDeg) {
        Matrix4x4 m = identity();
        double rad = Math.toRadians(angleDeg);
        m.mat[0][0] = Math.cos(rad); m.mat[0][2] = -Math.sin(rad);
        m.mat[2][0] = Math.sin(rad); m.mat[2][2] = Math.cos(rad);
        return m;
    }

    public static Matrix4x4 rotationZ(double angleDeg) {
        Matrix4x4 m = identity();
        double rad = Math.toRadians(angleDeg);
        m.mat[0][0] = Math.cos(rad); m.mat[0][1] = Math.sin(rad);
        m.mat[1][0] = -Math.sin(rad); m.mat[1][1] = Math.cos(rad);
        return m;
    }

    public static Matrix4x4 reflection(boolean x, boolean y, boolean z) {
        Matrix4x4 m = identity();
        if (x) m.mat[0][0] = -1;
        if (y) m.mat[1][1] = -1;
        if (z) m.mat[2][2] = -1;
        return m;
    }

    public static Matrix4x4 perspective(double d) {
        Matrix4x4 m = identity();
        if (d != 0) m.mat[2][3] = 1.0 / d;
        return m;
    }
}