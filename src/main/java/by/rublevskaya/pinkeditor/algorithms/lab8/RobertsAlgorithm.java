package by.rublevskaya.pinkeditor.algorithms.lab8;

import by.rublevskaya.pinkeditor.model.geom3d.Point3D;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class RobertsAlgorithm {

    public static class FaceResult {
        public boolean visible;
        public double[] normal;
        public double dotProduct;
    }

    public List<FaceResult> computeVisibility(
            List<Point3D> vertices,
            List<int[]> faces,
            Point3D eyePos,
            DefaultTableModel tableModel) {

        List<FaceResult> results = new ArrayList<>();

        for (int i = 0; i < faces.size(); i++) {
            int[] face = faces.get(i);

            FaceResult fr = new FaceResult();
            fr.normal = new double[]{0, 0, 0};

            if (face.length < 3 || face[0] >= vertices.size()
                    || face[1] >= vertices.size() || face[2] >= vertices.size()) {
                fr.visible = false;
                fr.dotProduct = 0;
                results.add(fr);
                continue;
            }

            Point3D p0 = vertices.get(face[0]);
            Point3D p1 = vertices.get(face[1]);
            Point3D p2 = vertices.get(face[2]);
            double[] v1 = {p1.x - p0.x, p1.y - p0.y, p1.z - p0.z};
            double[] v2 = {p2.x - p0.x, p2.y - p0.y, p2.z - p0.z};

            double[] n = {
                v1[1]*v2[2] - v1[2]*v2[1],
                v1[2]*v2[0] - v1[0]*v2[2],
                v1[0]*v2[1] - v1[1]*v2[0]
            };
            fr.normal = n;

            double cx = 0, cy = 0, cz = 0;
            for (int vi : face) {
                if (vi < vertices.size()) {
                    cx += vertices.get(vi).x;
                    cy += vertices.get(vi).y;
                    cz += vertices.get(vi).z;
                }
            }
            cx /= face.length;
            cy /= face.length;
            cz /= face.length;
            double[] toEye = {eyePos.x - cx, eyePos.y - cy, eyePos.z - cz};
            double dot = n[0]*toEye[0] + n[1]*toEye[1] + n[2]*toEye[2];
            fr.dotProduct = dot;
            fr.visible    = dot > 0;

            results.add(fr);

            if (tableModel != null) {
                double len = Math.sqrt(n[0]*n[0] + n[1]*n[1] + n[2]*n[2]);
                String normalStr = len > 0
                    ? String.format("(%.0f, %.0f, %.0f)", n[0]/len*100, n[1]/len*100, n[2]/len*100)
                    : "(0, 0, 0)";
                tableModel.addRow(new Object[]{
                    i + 1,
                    normalStr,
                    String.format("%.1f", dot),
                    fr.visible ? "Видима ✓" : "Скрыта ✗"
                });
            }
        }
        return results;
    }
}