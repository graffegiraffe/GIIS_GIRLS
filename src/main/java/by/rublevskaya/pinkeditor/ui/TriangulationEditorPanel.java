package by.rublevskaya.pinkeditor.ui;

import by.rublevskaya.pinkeditor.algorithms.lab7.*;
import by.rublevskaya.pinkeditor.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Панель Лабораторной работы 7 — «Триангуляция. Диаграмма Вороного».
 *
 * Функции:
 *  - Добавление точек кликом по холсту
 *  - Триангуляция Делоне (алгоритм Боуера–Уотсона)
 *  - Диаграмма Вороного (двойственная к Делоне)
 *  - Пошаговая отладка (анимация)
 *  - Удаление последней точки / полная очистка
 */
public class TriangulationEditorPanel extends JPanel {

    // ---- canvas ----
    private final TriCanvas canvas;

    // ---- log ----
    private JTable            logTable;
    private DefaultTableModel tableModel;

    // ---- state ----
    private List<DelaunayTriangulation.Step> delaunaySteps = new ArrayList<>();
    private List<Triangle>                   finalTriangles = new ArrayList<>();
    private List<VoronoiDiagram.VoronoiEdge> voronoiEdges  = new ArrayList<>();
    private Timer animationTimer;

    // ---- controls ----
    private JLabel statusLabel;
    private JCheckBox showDelaunayBox;
    private JCheckBox showVoronoiBox;
    private JCheckBox showCircleBox;

    public TriangulationEditorPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_COLOR);
        canvas = new TriCanvas();
        initToolbar();
        initMainArea();
    }

    // ------------------------------------------------------------------ //
    //  Toolbar                                                              //
    // ------------------------------------------------------------------ //

    private void initToolbar() {
        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(Color.WHITE);
        north.setBorder(new EmptyBorder(4, 8, 4, 8));

        // --- строка 1 ---
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        row1.setBackground(Color.WHITE);

        JLabel lbl = new JLabel("Lab 7 — Триангуляция & Вороной:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(Theme.ACCENT_COLOR);

        JButton computeBtn = btn("Вычислить",      new Color(255, 228, 225));
        JButton debugBtn   = btn("Отладка (шаги)", new Color(255, 200, 220));
        JButton deleteBtn  = btn("← Удалить точку",new Color(255, 235, 205));
        JButton clearBtn   = btn("ОЧИСТИТЬ",        new Color(219, 112, 147));
        clearBtn.setForeground(Color.WHITE);

        computeBtn.addActionListener(e -> runCompute(false));
        debugBtn  .addActionListener(e -> runCompute(true));
        deleteBtn .addActionListener(e -> { canvas.removeLastPoint(); clearResults(); updateStatus(); });
        clearBtn  .addActionListener(e -> resetAll());

        row1.add(lbl);
        row1.add(computeBtn);
        row1.add(debugBtn);
        row1.add(deleteBtn);
        row1.add(clearBtn);

        // --- строка 2: настройки отображения ---
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 3));
        row2.setBackground(new Color(250, 245, 248));

        JLabel visLbl = new JLabel("Показать: ");
        visLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        visLbl.setForeground(Theme.TEXT_COLOR);

        showDelaunayBox = check("Триангуляция Делоне", true);
        showVoronoiBox  = check("Диаграмма Вороного",  true);
        showCircleBox   = check("Описанные окружности", false);

        showDelaunayBox.addActionListener(e -> canvas.repaint());
        showVoronoiBox .addActionListener(e -> canvas.repaint());
        showCircleBox  .addActionListener(e -> canvas.repaint());

        row2.add(visLbl);
        row2.add(showDelaunayBox);
        row2.add(showVoronoiBox);
        row2.add(showCircleBox);

        // --- статус ---
        statusLabel = new JLabel("Кликайте на холст для добавления точек");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Theme.TEXT_COLOR);
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        statusPanel.setBackground(Color.WHITE);
        statusPanel.add(statusLabel);

        JPanel rows = new JPanel(new GridLayout(2, 1));
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

    private JCheckBox check(String text, boolean selected) {
        JCheckBox cb = new JCheckBox(text, selected);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(new Color(250, 245, 248));
        cb.setForeground(Theme.TEXT_COLOR);
        cb.setFocusPainted(false);
        return cb;
    }

    // ------------------------------------------------------------------ //
    //  Основная область                                                    //
    // ------------------------------------------------------------------ //

    private void initMainArea() {
        canvas.setOnPointAdded(this::updateStatus);

        String[] cols = DelaunayTriangulation.getTableColumns();
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

    // ------------------------------------------------------------------ //
    //  Логика                                                              //
    // ------------------------------------------------------------------ //

    private void runCompute(boolean animate) {
        if (animationTimer != null && animationTimer.isRunning()) animationTimer.stop();

        List<Point2D.Double> pts = canvas.getPoints();
        if (pts.size() < 3) {
            JOptionPane.showMessageDialog(this,
                    "Добавьте не менее 3 точек.", "Недостаточно точек",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Вычислить Делоне
        DefaultTableModel tempModel = new DefaultTableModel(DelaunayTriangulation.getTableColumns(), 0);
        delaunaySteps = DelaunayTriangulation.compute(new ArrayList<>(pts), tempModel);

        // Финальная триангуляция — последний шаг
        finalTriangles = delaunaySteps.isEmpty()
                ? new ArrayList<>()
                : delaunaySteps.get(delaunaySteps.size() - 1).triangulation;

        // Вороной из финальной триангуляции
        voronoiEdges = VoronoiDiagram.build(finalTriangles, canvas.getWidth(), canvas.getHeight());

        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(DelaunayTriangulation.getTableColumns());

        if (!animate) {
            canvas.setDelaunayTriangles(finalTriangles, null);
            canvas.setVoronoiEdges(voronoiEdges);
            copyRows(tempModel);
            statusLabel.setText("Готово: " + finalTriangles.size() + " треугольников, " +
                    voronoiEdges.size() + " рёбер Вороного.");
        } else {
            canvas.setVoronoiEdges(null);
            final int[] idx = {0};
            animationTimer = new Timer(300, ev -> {
                if (idx[0] < delaunaySteps.size()) {
                    DelaunayTriangulation.Step step = delaunaySteps.get(idx[0]);
                    canvas.setDelaunayTriangles(step.triangulation, step.badTriangles);
                    if (idx[0] < tempModel.getRowCount()) {
                        addRowFromModel(tempModel, idx[0]);
                        scrollLog();
                    }
                    statusLabel.setText("Шаг " + (idx[0] + 1) + "/" + delaunaySteps.size() +
                            ": " + step.description);
                    idx[0]++;
                } else {
                    // Финал: показать Вороного
                    canvas.setDelaunayTriangles(finalTriangles, null);
                    canvas.setVoronoiEdges(voronoiEdges);
                    while (idx[0] < tempModel.getRowCount()) addRowFromModel(tempModel, idx[0]++);
                    ((Timer) ev.getSource()).stop();
                    statusLabel.setText("Готово: " + finalTriangles.size() + " треугольников, " +
                            voronoiEdges.size() + " рёбер Вороного.");
                }
            });
            animationTimer.start();
            statusLabel.setText("Анимация...");
        }
    }

    private void clearResults() {
        if (animationTimer != null && animationTimer.isRunning()) animationTimer.stop();
        delaunaySteps.clear();
        finalTriangles.clear();
        voronoiEdges.clear();
        canvas.setDelaunayTriangles(null, null);
        canvas.setVoronoiEdges(null);
        tableModel.setRowCount(0);
    }

    private void resetAll() {
        clearResults();
        canvas.clear();
        updateStatus();
    }

    private void updateStatus() {
        int n = canvas.getPoints().size();
        statusLabel.setText(n == 0
                ? "Кликайте на холст для добавления точек"
                : "Точек: " + n + (n < 3 ? "  (нужно ещё " + (3 - n) + ")" : "  — готово к вычислению"));
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

    // ================================================================== //
    //  Внутренний холст                                                   //
    // ================================================================== //

    class TriCanvas extends JPanel {

        private static final int GRID = 50;
        private static final int PT_R = 5;

        private final List<Point2D.Double> points = new ArrayList<>();
        private List<Triangle>                  delaunayTris  = null;
        private List<Triangle>                  badTris       = null;
        private List<VoronoiDiagram.VoronoiEdge> voronoiEdges = null;

        private Runnable onPointAdded = () -> {};

        // Цвета
        private static final Color COL_GRID     = new Color(230, 220, 225);
        private static final Color COL_POINT    = new Color(219, 112, 147);
        private static final Color COL_DELAUNAY = new Color(100, 140, 220);
        private static final Color COL_BAD      = new Color(220, 80, 80, 120);
        private static final Color COL_VORONOI  = new Color(40, 160, 80);
        private static final Color COL_CIRCLE   = new Color(180, 180, 60, 100);
        private static final Color COL_CIRCLINE = new Color(160, 160, 40, 160);

        TriCanvas() {
            setBackground(Color.WHITE);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        points.add(new Point2D.Double(e.getX(), e.getY()));
                        // Автосброс старых результатов при добавлении новой точки
                        clearResults();
                        repaint();
                        onPointAdded.run();
                    }
                }
            });
        }

        void setOnPointAdded(Runnable r)                          { this.onPointAdded = r; }
        List<Point2D.Double> getPoints()                          { return java.util.Collections.unmodifiableList(points); }

        void removeLastPoint() {
            if (!points.isEmpty()) { points.remove(points.size() - 1); repaint(); }
        }

        void clear() { points.clear(); repaint(); }

        void setDelaunayTriangles(List<Triangle> tris, List<Triangle> bad) {
            this.delaunayTris = tris;
            this.badTris      = bad;
            repaint();
        }

        void setVoronoiEdges(List<VoronoiDiagram.VoronoiEdge> edges) {
            this.voronoiEdges = edges;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawGrid(g2);

            // Описанные окружности (опционально)
            if (showCircleBox.isSelected() && delaunayTris != null) drawCircles(g2);

            // Плохие треугольники (во время анимации)
            if (badTris != null && !badTris.isEmpty()) drawBadTriangles(g2);

            // Делоне
            if (showDelaunayBox.isSelected() && delaunayTris != null) drawDelaunay(g2);

            // Вороной
            if (showVoronoiBox.isSelected() && voronoiEdges != null) drawVoronoi(g2);

            // Точки (рисуем поверх всего)
            drawPoints(g2);
        }

        private void drawGrid(Graphics2D g2) {
            g2.setColor(COL_GRID);
            int w = getWidth(), h = getHeight();
            for (int x = 0; x <= w; x += GRID) g2.drawLine(x, 0, x, h);
            for (int y = 0; y <= h; y += GRID) g2.drawLine(0, y, w, y);
        }

        private void drawDelaunay(Graphics2D g2) {
            g2.setColor(COL_DELAUNAY);
            g2.setStroke(new BasicStroke(1.5f));
            for (Triangle t : delaunayTris) {
                g2.drawLine((int)t.a.x, (int)t.a.y, (int)t.b.x, (int)t.b.y);
                g2.drawLine((int)t.b.x, (int)t.b.y, (int)t.c.x, (int)t.c.y);
                g2.drawLine((int)t.c.x, (int)t.c.y, (int)t.a.x, (int)t.a.y);
            }
        }

        private void drawBadTriangles(Graphics2D g2) {
            g2.setColor(COL_BAD);
            for (Triangle t : badTris) {
                int[] xs = {(int)t.a.x, (int)t.b.x, (int)t.c.x};
                int[] ys = {(int)t.a.y, (int)t.b.y, (int)t.c.y};
                g2.fillPolygon(xs, ys, 3);
            }
        }

        private void drawVoronoi(Graphics2D g2) {
            g2.setColor(COL_VORONOI);
            g2.setStroke(new BasicStroke(1.8f));
            // Clip to canvas bounds so boundary rays don't overflow visually
            Shape oldClip = g2.getClip();
            g2.setClip(0, 0, getWidth(), getHeight());
            for (VoronoiDiagram.VoronoiEdge e : voronoiEdges) {
                g2.drawLine((int)e.p1.x, (int)e.p1.y, (int)e.p2.x, (int)e.p2.y);
            }
            g2.setClip(oldClip);
        }

        private void drawCircles(Graphics2D g2) {
            for (Triangle t : delaunayTris) {
                double r = Math.sqrt(t.circumRadiusSq);
                int cx = (int) t.circumCenter.x, cy = (int) t.circumCenter.y, ir = (int) r;
                // Центр описанной окружности
                g2.setColor(COL_CIRCLE);
                g2.fillOval(cx - 3, cy - 3, 6, 6);
                // Окружность
                g2.setColor(COL_CIRCLINE);
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1, new float[]{4, 4}, 0));
                g2.drawOval(cx - ir, cy - ir, ir * 2, ir * 2);
            }
        }

        private void drawPoints(Graphics2D g2) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            for (int i = 0; i < points.size(); i++) {
                Point2D.Double p = points.get(i);
                int px = (int) p.x, py = (int) p.y;
                g2.setColor(COL_POINT);
                g2.fillOval(px - PT_R, py - PT_R, PT_R * 2, PT_R * 2);
                g2.setColor(Theme.TEXT_COLOR);
                g2.drawString("P" + i, px + 7, py - 3);
            }
        }
    }
}
