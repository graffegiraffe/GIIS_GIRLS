package by.rublevskaya.pinkeditor.ui;

import by.rublevskaya.pinkeditor.algorithms.lab6.*;
import by.rublevskaya.pinkeditor.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
public class PolygonBuildEditorPanel extends JPanel {

    private final PolyBuildCanvas canvas;
    private JTable            logTable;
    private DefaultTableModel tableModel;
    private JComboBox<ConvexHullType> hullSelector;
    private JLabel statusLabel;
    private Timer  animationTimer;

    private enum Mode { POLYGON, TEST_POINT, SEGMENT }
    private Mode currentMode = Mode.POLYGON;

    public PolygonBuildEditorPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_COLOR);
        canvas = new PolyBuildCanvas();
        initToolbar();
        initMainArea();
    }
    private void initToolbar() {
        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(Color.WHITE);
        north.setBorder(new EmptyBorder(4, 8, 4, 8));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        row1.setBackground(Color.WHITE);

        JLabel lbl = new JLabel("Lab 5 — Построение полигонов:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(Theme.ACCENT_COLOR);

        hullSelector = new JComboBox<>(ConvexHullType.values());
        hullSelector.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hullSelector.setBackground(Color.WHITE);

        JButton closeBtn   = btn("Закрыть полигон",    new Color(255, 220, 230));
        JButton hullBtn    = btn("Выпуклая оболочка",  new Color(255, 228, 225));
        JButton debugBtn   = btn("Отладка",            new Color(255, 200, 220));
        JButton deleteBtn  = btn("← Удалить вершину",  new Color(255, 235, 205));
        JButton clearBtn   = btn("ОЧИСТИТЬ",           new Color(219, 112, 147));
        clearBtn.setForeground(Color.WHITE);

        closeBtn.addActionListener(e -> {
            if (canvas.getVertices().size() >= 3 && !canvas.isPolygonClosed()) {
                canvas.closePoly(); setMode(Mode.POLYGON); updateStatus();
            } else if (canvas.getVertices().size() < 3) {
                showInfo("Нужно минимум 3 вершины для закрытия полигона.");
            }
        });
        hullBtn.addActionListener(e -> runHull(false));
        debugBtn.addActionListener(e -> runHull(true));
        deleteBtn.addActionListener(e -> { canvas.removeLastVertex(); updateStatus(); });
        clearBtn.addActionListener(e -> resetAll());

        row1.add(lbl);
        row1.add(Box.createHorizontalStrut(4));
        row1.add(hullSelector);
        row1.add(closeBtn);
        row1.add(hullBtn);
        row1.add(debugBtn);
        row1.add(deleteBtn);
        row1.add(clearBtn);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        row2.setBackground(new Color(250, 245, 248));

        JLabel lbl2 = new JLabel("Анализ: ");
        lbl2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl2.setForeground(Theme.TEXT_COLOR);

        JButton convexBtn  = btn("Проверить выпуклость", new Color(220, 240, 255));
        JButton normalBtn  = btn("Нормали",              new Color(220, 255, 230));
        JButton pointBtn   = btn("Точка в полигоне",     new Color(255, 245, 200));
        JButton segBtn     = btn("Пересечение отрезка",  new Color(240, 220, 255));

        convexBtn.addActionListener(e -> checkConvexity());
        normalBtn.addActionListener(e -> showNormals());
        pointBtn.addActionListener(e -> {
            if (!requireClosed()) return;
            setMode(Mode.TEST_POINT);
            statusLabel.setText("Режим: кликните на холст — проверка принадлежности точки");
        });
        segBtn.addActionListener(e -> {
            if (!requireClosed()) return;
            canvas.clearSegment();
            setMode(Mode.SEGMENT);
            statusLabel.setText("Режим: кликните 2 точки на холсте для задания отрезка");
        });

        row2.add(lbl2);
        row2.add(convexBtn);
        row2.add(normalBtn);
        row2.add(pointBtn);
        row2.add(segBtn);

        statusLabel = new JLabel("Кликайте на холст для добавления вершин");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Theme.TEXT_COLOR);
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        statusPanel.setBackground(Color.WHITE);
        statusPanel.add(statusLabel);

        JPanel rows = new JPanel(new GridLayout(2, 1, 0, 0));
        rows.add(row1);
        rows.add(row2);

        north.add(rows, BorderLayout.NORTH);
        north.add(statusPanel, BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
    }
    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Theme.TEXT_COLOR);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.ACCENT_COLOR, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
    private void initMainArea() {
        canvas.setOnStateChanged(this::updateStatus);
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) return;
                if (currentMode == Mode.TEST_POINT) {
                    testPoint(e.getPoint());
                } else if (currentMode == Mode.SEGMENT) {
                    canvas.addSegmentPoint(e.getPoint());
                    if (canvas.isSegmentComplete()) {
                        runSegmentTest();
                        setMode(Mode.POLYGON);
                        updateStatus();
                    } else {
                        statusLabel.setText("P1 задана. Кликните для P2.");
                    }
                }
            }
        });

        String[] cols = {"Шаг", "Действие", "Состояние", "Примечание"};
        tableModel = new DefaultTableModel(cols, 0);

        logTable = new JTable(tableModel);
        logTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logTable.setRowHeight(22);
        logTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        logTable.getTableHeader().setBackground(Theme.BUTTON_COLOR);
        logTable.getTableHeader().setForeground(Theme.TEXT_COLOR);

        JScrollPane scrollPane = new JScrollPane(logTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.ACCENT_COLOR, 2),
                "Лог", TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), Theme.ACCENT_COLOR));
        scrollPane.setPreferredSize(new Dimension(360, 0));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvas, scrollPane);
        split.setResizeWeight(0.75);
        split.setDividerSize(5);
        add(split, BorderLayout.CENTER);
    }
    private void runHull(boolean animate) {
        if (animationTimer != null && animationTimer.isRunning()) animationTimer.stop();
        List<Point> pts = canvas.getVertices();
        if (pts.size() < 3) { showInfo("Добавьте не менее 3 вершин."); return; }

        ConvexHullType type = (ConvexHullType) hullSelector.getSelectedItem();
        DefaultTableModel tempModel = new DefaultTableModel(getHullColumns(type), 0);
        List<List<Point>> steps = (type == ConvexHullType.GRAHAM)
                ? GrahamScan.compute(new ArrayList<>(pts), tempModel)
                : JarvisMarch.compute(new ArrayList<>(pts), tempModel);

        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(getHullColumns(type));
        canvas.setHullPoints(null);

        if (!animate) {
            if (!steps.isEmpty()) canvas.setHullPoints(steps.get(steps.size() - 1));
            copyRows(tempModel);
            statusLabel.setText("Выпуклая оболочка построена (" + type + ").");
        } else {
            final int[] idx = {0};
            animationTimer = new Timer(250, ev -> {
                if (idx[0] < steps.size()) {
                    canvas.setHullPoints(steps.get(idx[0]));
                    if (idx[0] < tempModel.getRowCount()) { addRowFromModel(tempModel, idx[0]); scrollLog(); }
                    idx[0]++;
                } else {
                    while (idx[0] < tempModel.getRowCount()) addRowFromModel(tempModel, idx[0]++);
                    ((Timer) ev.getSource()).stop();
                    statusLabel.setText("Готово.");
                }
            });
            animationTimer.start();
            statusLabel.setText("Анимация...");
        }
    }

    private String[] getHullColumns(ConvexHullType t) {
        return t == ConvexHullType.GRAHAM ? GrahamScan.getTableColumns() : JarvisMarch.getTableColumns();
    }

    private void checkConvexity() {
        if (!requireClosed()) return;
        boolean convex = PolygonAnalysis.isConvex(canvas.getVertices());
        canvas.setConvexStatus(convex);
        String msg = convex ? "✔ Полигон ВЫПУКЛЫЙ" : "✘ Полигон НЕВЫПУКЛЫЙ";
        statusLabel.setText(msg);
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new String[]{"Результат"});
        tableModel.addRow(new Object[]{msg});
        JOptionPane.showMessageDialog(this, msg, "Проверка выпуклости",
                convex ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    private void showNormals() {
        if (!requireClosed()) return;
        List<PolygonAnalysis.Normal> normals = PolygonAnalysis.getInnerNormals(canvas.getVertices());
        canvas.setNormals(normals);
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new String[]{"№ ребра", "MidX", "MidY", "Nx", "Ny"});
        for (int i = 0; i < normals.size(); i++) {
            PolygonAnalysis.Normal n = normals.get(i);
            tableModel.addRow(new Object[]{
                    "V" + i + "→V" + ((i + 1) % normals.size()),
                    String.format("%.1f", n.midX), String.format("%.1f", n.midY),
                    String.format("%.3f", n.nx),   String.format("%.3f", n.ny)
            });
        }
        statusLabel.setText("Нормали отображены (" + normals.size() + " рёбер).");
    }
    private void testPoint(Point screenPt) {
        List<Point> pts = canvas.getVertices();
        if (!canvas.isPolygonClosed() || pts.size() < 3) return;
        boolean inside = PolygonAnalysis.isPointInside(pts, screenPt);
        canvas.setTestPoint(screenPt, inside);
        String msg = "Точка (" + screenPt.x + "," + screenPt.y + "): " +
                     (inside ? "ВНУТРИ" : "СНАРУЖИ") + " полигона";
        statusLabel.setText(msg);
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new String[]{"X", "Y", "Результат"});
        tableModel.addRow(new Object[]{screenPt.x, screenPt.y, inside ? "Внутри" : "Снаружи"});
        setMode(Mode.POLYGON);
    }
    private void runSegmentTest() {
        if (!canvas.isPolygonClosed()) return;
        Point p1 = canvas.getSegmentP1(), p2 = canvas.getSegmentP2();
        List<Point> inter = PolygonAnalysis.segmentIntersections(canvas.getVertices(), p1, p2);
        canvas.setIntersections(inter);
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new String[]{"№", "X", "Y", "Примечание"});
        if (inter.isEmpty()) {
            tableModel.addRow(new Object[]{"-", "-", "-", "Пересечений нет"});
            statusLabel.setText("Отрезок не пересекает полигон.");
        } else {
            for (int i = 0; i < inter.size(); i++) {
                Point p = inter.get(i);
                tableModel.addRow(new Object[]{i + 1, p.x, p.y, "Пересечение"});
            }
            statusLabel.setText("Найдено пересечений: " + inter.size());
        }
    }

    private void resetAll() {
        if (animationTimer != null && animationTimer.isRunning()) animationTimer.stop();
        canvas.clear();
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new String[]{"Шаг", "Действие", "Состояние", "Примечание"});
        setMode(Mode.POLYGON);
        updateStatus();
    }

    private boolean requireClosed() {
        if (!canvas.isPolygonClosed() || canvas.getVertices().size() < 3) {
            showInfo("Сначала постройте и закройте полигон (≥3 вершин).");
            return false;
        }
        return true;
    }

    private void setMode(Mode m) { currentMode = m; }

    private void updateStatus() {
        if (currentMode != Mode.POLYGON) return;
        List<Point> verts = canvas.getVertices();
        if (!canvas.isPolygonClosed()) {
            statusLabel.setText(verts.isEmpty()
                    ? "Кликайте на холст для добавления вершин"
                    : "Вершин: " + verts.size() + "  |  ПКМ или двойной клик — закрыть полигон");
        } else {
            statusLabel.setText("Полигон закрыт (" + verts.size() + " вершин). Выберите операцию.");
        }
    }

    private void copyRows(DefaultTableModel src) {
        for (int r = 0; r < src.getRowCount(); r++) addRowFromModel(src, r);
    }

    @SuppressWarnings("unchecked")
    private void addRowFromModel(DefaultTableModel src, int row) {
        tableModel.addRow(new java.util.Vector<>((java.util.Vector<?>) src.getDataVector().get(row)));
    }

    private void scrollLog() {
        SwingUtilities.invokeLater(() ->
                logTable.scrollRectToVisible(logTable.getCellRect(logTable.getRowCount() - 1, 0, true)));
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Информация", JOptionPane.INFORMATION_MESSAGE);
    }

    static class PolyBuildCanvas extends JPanel {

        private static final int GRID = 50;

        private final List<Point> vertices = new ArrayList<>();
        private boolean closed = false;
        private Point   hover  = null;

        private List<Point>                 hullPoints    = null;
        private List<PolygonAnalysis.Normal> normals      = null;
        private Boolean                      convexStatus = null;
        private Point                        testPoint    = null;
        private boolean                      testInside   = false;
        private Point                        segP1        = null;
        private Point                        segP2        = null;
        private List<Point>                  intersections = null;

        private Runnable onStateChanged = () -> {};

        private static final Color COL_GRID    = new Color(230, 220, 225);
        private static final Color COL_VERTEX  = new Color(219, 112, 147);
        private static final Color COL_EDGE    = new Color(180,  70, 110);
        private static final Color COL_HULL    = new Color(80, 140, 255, 220);
        private static final Color COL_HULL_PT = new Color(50,  90, 220);
        private static final Color COL_NORMAL  = new Color(40, 180,  80);
        private static final Color COL_SEG     = new Color(170,  60, 210);
        private static final Color COL_INTER   = new Color(255, 110,  20);

        PolyBuildCanvas() {
            setBackground(Color.WHITE);
            MouseAdapter ma = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { onMousePressed(e); }
                @Override public void mouseMoved(MouseEvent e)   { hover = e.getPoint(); repaint(); }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        void setOnStateChanged(Runnable r) { this.onStateChanged = r; }

        List<Point> getVertices()     { return java.util.Collections.unmodifiableList(vertices); }
        boolean     isPolygonClosed() { return closed; }

        void closePoly() {
            if (vertices.size() >= 3 && !closed) {
                closed = true; hover = null; repaint(); onStateChanged.run();
            }
        }

        void removeLastVertex() {
            if (closed) {
                closed = false;
                hullPoints = null;
                normals    = null;
                convexStatus = null;
                testPoint  = null;
                segP1 = null; segP2 = null; intersections = null;
                repaint(); onStateChanged.run();
                return;
            }
            if (vertices.isEmpty()) return;
            vertices.remove(vertices.size() - 1);
            repaint(); onStateChanged.run();
        }

        void clear() {
            vertices.clear(); closed = false; hover = null;
            hullPoints = null; normals = null; convexStatus = null;
            testPoint = null; segP1 = null; segP2 = null; intersections = null;
            repaint(); onStateChanged.run();
        }

        void setHullPoints(List<Point> pts)                      { hullPoints = pts;    repaint(); }
        void setNormals(List<PolygonAnalysis.Normal> n)          { normals    = n;      repaint(); }
        void setConvexStatus(Boolean v)                          { convexStatus = v;    repaint(); }
        void setTestPoint(Point p, boolean inside)               { testPoint = p; testInside = inside; repaint(); }
        void clearSegment()                                       { segP1 = null; segP2 = null; intersections = null; repaint(); }
        void addSegmentPoint(Point p)                            { if (segP1 == null) segP1 = p; else if (segP2 == null) segP2 = p; repaint(); }
        boolean isSegmentComplete()                              { return segP1 != null && segP2 != null; }
        Point   getSegmentP1()                                   { return segP1; }
        Point   getSegmentP2()                                   { return segP2; }
        void setIntersections(List<Point> pts)                   { intersections = pts; repaint(); }

        private void onMousePressed(MouseEvent e) {
            if (closed) return;
            if (e.getButton() == MouseEvent.BUTTON3 && vertices.size() >= 3) { closePoly(); return; }
            if (e.getClickCount() == 2  && vertices.size() >= 3) { closePoly(); return; }
            if (e.getButton() == MouseEvent.BUTTON1) {
                vertices.add(e.getPoint()); repaint(); onStateChanged.run();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawGrid(g2);
            drawPolygon(g2);
            drawHull(g2);
            drawNormals(g2);
            drawTestPoint(g2);
            drawSegment(g2);
        }

        private void drawGrid(Graphics2D g2) {
            g2.setColor(COL_GRID);
            int w = getWidth(), h = getHeight();
            for (int x = 0; x <= w; x += GRID) g2.drawLine(x, 0, x, h);
            for (int y = 0; y <= h; y += GRID) g2.drawLine(0, y, w, y);
        }

        private void drawPolygon(Graphics2D g2) {
            if (vertices.isEmpty()) return;
            if (closed && convexStatus != null) {
                Color fill = convexStatus
                        ? new Color(80, 200, 100, 70)
                        : new Color(220, 80, 80, 70);
                g2.setColor(fill);
                g2.fillPolygon(toAwtPolygon(vertices));
            }

            g2.setStroke(new BasicStroke(2f));
            g2.setColor(COL_EDGE);
            for (int i = 0; i < vertices.size() - 1; i++) {
                Point a = vertices.get(i), b = vertices.get(i + 1);
                g2.drawLine(a.x, a.y, b.x, b.y);
            }
            if (closed && vertices.size() >= 3) {
                Point a = vertices.get(vertices.size() - 1), b = vertices.get(0);
                g2.drawLine(a.x, a.y, b.x, b.y);
            } else if (hover != null && !vertices.isEmpty()) {
                g2.setColor(new Color(180, 180, 180));
                Point last = vertices.get(vertices.size() - 1);
                g2.drawLine(last.x, last.y, hover.x, hover.y);
            }

            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            for (int i = 0; i < vertices.size(); i++) {
                Point p = vertices.get(i);
                g2.setColor(COL_VERTEX);
                g2.fillOval(p.x - 5, p.y - 5, 10, 10);
                g2.setColor(Theme.TEXT_COLOR);
                g2.drawString("V" + i, p.x + 7, p.y - 3);
            }
        }

        private void drawHull(Graphics2D g2) {
            if (hullPoints == null || hullPoints.size() < 2) return;
            g2.setColor(COL_HULL);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1, new float[]{8, 4}, 0));
            for (int i = 0; i < hullPoints.size(); i++) {
                Point a = hullPoints.get(i), b = hullPoints.get((i + 1) % hullPoints.size());
                g2.drawLine(a.x, a.y, b.x, b.y);
            }
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(COL_HULL_PT);
            for (Point p : hullPoints) g2.fillOval(p.x - 6, p.y - 6, 12, 12);
        }

        private void drawNormals(Graphics2D g2) {
            if (normals == null) return;
            g2.setColor(COL_NORMAL);
            g2.setStroke(new BasicStroke(2f));
            int len = 35;
            for (PolygonAnalysis.Normal n : normals) {
                int x1 = (int) n.midX, y1 = (int) n.midY;
                int x2 = (int)(n.midX + n.nx * len), y2 = (int)(n.midY + n.ny * len);
                g2.drawLine(x1, y1, x2, y2);
                double ang = Math.atan2(y2 - y1, x2 - x1);
                g2.drawLine(x2, y2, (int)(x2 - 9 * Math.cos(ang - 0.4)), (int)(y2 - 9 * Math.sin(ang - 0.4)));
                g2.drawLine(x2, y2, (int)(x2 - 9 * Math.cos(ang + 0.4)), (int)(y2 - 9 * Math.sin(ang + 0.4)));
            }
        }

        private void drawTestPoint(Graphics2D g2) {
            if (testPoint == null) return;
            g2.setColor(testInside ? new Color(40, 180, 90) : new Color(210, 50, 50));
            g2.fillOval(testPoint.x - 8, testPoint.y - 8, 16, 16);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.drawString(testInside ? "IN" : "OUT", testPoint.x - 10, testPoint.y + 4);
        }

        private void drawSegment(Graphics2D g2) {
            if (segP1 == null) return;
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(COL_SEG);
            g2.fillOval(segP1.x - 5, segP1.y - 5, 10, 10);
            if (segP2 != null) {
                g2.drawLine(segP1.x, segP1.y, segP2.x, segP2.y);
                g2.fillOval(segP2.x - 5, segP2.y - 5, 10, 10);
            }
            if (intersections != null) {
                g2.setColor(COL_INTER);
                for (Point p : intersections) g2.fillOval(p.x - 6, p.y - 6, 12, 12);
            }
        }

        private static Polygon toAwtPolygon(List<Point> pts) {
            return new Polygon(
                    pts.stream().mapToInt(p -> p.x).toArray(),
                    pts.stream().mapToInt(p -> p.y).toArray(),
                    pts.size());
        }
    }
}