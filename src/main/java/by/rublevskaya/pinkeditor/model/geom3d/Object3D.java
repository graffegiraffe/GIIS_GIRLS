package by.rublevskaya.pinkeditor.model.geom3d;

import java.util.ArrayList;
import java.util.List;

public class Object3D {
    public List<Point3D> vertices = new ArrayList<>();
    public List<int[]> edges = new ArrayList<>(); // [start, end]
    public Matrix4x4 currentTransform = Matrix4x4.identity();

    public void addVertex(double x, double y, double z) {
        vertices.add(new Point3D(x, y, z));
    }

    public void addEdge(int v1, int v2) {
        edges.add(new int[]{v1, v2});
    }

    public void applyTransform(Matrix4x4 transform) {
        currentTransform = currentTransform.multiply(transform);
    }

    public void resetTransform() {
        currentTransform = Matrix4x4.identity();
    }
}