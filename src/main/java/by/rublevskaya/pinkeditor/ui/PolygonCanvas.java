package by.rublevskaya.pinkeditor.ui;

import by.rublevskaya.pinkeditor.algorithms.lab5.SimpleFloodFillAlgorithm;
import by.rublevskaya.pinkeditor.util.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PolygonCanvas extends JPanel {

    public static final int CELL_SIZE = 20;

    private final List<Point> vertices    = new ArrayList<>();
    private boolean           polygonClosed = false;
    private Point hoverCell = null;
    private final List<Point> filledPixels = new ArrayList<>();
    private boolean[][] boundaryGrid = null;
    private Point seedPoint = null;
    private Runnable onStateChanged = () -> {};

    private static final Color COLOR_GRID     = new Color(230, 220, 225);
    private static final Color COLOR_VERTEX   = new Color(219, 112, 147);
    private static final Color COLOR_EDGE     = new Color(180,  70, 110);
    private static final Color COLOR_PREVIEW  = new Color(180, 180, 180);
    private static final Color COLOR_BOUNDARY = new Color(219, 112, 147, 200);
    private static final Color COLOR_FILL     = new Color(219, 112, 147, 140);
    private static final Color COLOR_SEED     = new Color(80, 200, 120);

    public PolygonCanvas() {
        setBackground(Color.WHITE);

        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { handleClick(e); }
            @Override public void mouseMoved(MouseEvent e)  { handleMove(e); }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    public void setOnStateChanged(Runnable r) { this.onStateChanged = r; }
    public List<Point> getVertices()   { return Collections.unmodifiableList(vertices); }
    public boolean     isPolygonClosed() { return polygonClosed; }
    public Point       getSeedPoint()  { return seedPoint; }
    public int getGridWidth()  { return Math.max(1, getWidth()  / CELL_SIZE); }
    public int getGridHeight() { return Math.max(1, getHeight() / CELL_SIZE); }
    public void removeLastVertex() {
        if (polygonClosed || vertices.isEmpty()) return;
        vertices.remove(vertices.size() - 1);
        repaint();
        onStateChanged.run();
    }

    public void handleClosePolygon() {
        if (vertices.size() >= 3 && !polygonClosed) {
            polygonClosed = true;
            hoverCell = null;
            repaint();
            onStateChanged.run();
        }
    }

    public void setFilledPixels(List<Point> pts) {
        filledPixels.clear();
        if (pts != null) filledPixels.addAll(pts);
        repaint();
    }

    public void addFilledPixels(List<Point> pts) {
        if (pts != null) filledPixels.addAll(pts);
        repaint();
    }

    public void showBoundary() {
        if (polygonClosed && vertices.size() >= 3) {
            boundaryGrid = SimpleFloodFillAlgorithm.rasteriseBoundary(
                    vertices, getGridWidth(), getGridHeight());
        }
        repaint();
    }

    public void clear() {
        vertices.clear();
        polygonClosed = false;
        filledPixels.clear();
        boundaryGrid = null;
        seedPoint    = null;
        hoverCell    = null;
        repaint();
        onStateChanged.run();
    }

    private void handleClick(MouseEvent e) {
        Point cell = toCell(e.getPoint());

        if (!polygonClosed) {
            if (e.getButton() == MouseEvent.BUTTON3 && vertices.size() >= 3) {
                polygonClosed = true;
                hoverCell = null;
                repaint();
                onStateChanged.run();
                return;
            }
            if (e.getClickCount() == 2 && vertices.size() >= 3) {
                polygonClosed = true;
                hoverCell = null;
                repaint();
                onStateChanged.run();
                return;
            }
            vertices.add(cell);
            repaint();
            onStateChanged.run();
        } else {
            if (e.getButton() == MouseEvent.BUTTON1) {
                seedPoint = cell;
                repaint();
                onStateChanged.run();
            }
        }
    }

    private void handleMove(MouseEvent e) {
        hoverCell = toCell(e.getPoint());
        repaint();
    }

    public Point toCell(Point screen) {
        return new Point(screen.x / CELL_SIZE, screen.y / CELL_SIZE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);
        drawBoundaryPixels(g2);
        drawFilledPixels(g2);
        drawPolygon(g2);
        drawSeed(g2);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(COLOR_GRID);
        int w = getWidth(), h = getHeight();
        for (int x = 0; x <= w; x += CELL_SIZE) g2.drawLine(x, 0, x, h);
        for (int y = 0; y <= h; y += CELL_SIZE) g2.drawLine(0, y, w, y);
    }

    private void drawBoundaryPixels(Graphics2D g2) {
        if (boundaryGrid == null) return;
        g2.setColor(COLOR_BOUNDARY);
        int w = boundaryGrid.length;
        int h = w > 0 ? boundaryGrid[0].length : 0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (boundaryGrid[x][y]) {
                    g2.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

    private void drawFilledPixels(Graphics2D g2) {
        g2.setColor(COLOR_FILL);
        for (Point p : filledPixels) {
            g2.fillRect(p.x * CELL_SIZE, p.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
    }

    private void drawPolygon(Graphics2D g2) {
        if (vertices.isEmpty()) return;

        g2.setStroke(new BasicStroke(2f));
        g2.setColor(COLOR_EDGE);
        for (int i = 0; i < vertices.size() - 1; i++) {
            Point a = cellCenter(vertices.get(i));
            Point b = cellCenter(vertices.get(i + 1));
            g2.drawLine(a.x, a.y, b.x, b.y);
        }
        if (polygonClosed && vertices.size() >= 3) {
            Point a = cellCenter(vertices.get(vertices.size() - 1));
            Point b = cellCenter(vertices.get(0));
            g2.drawLine(a.x, a.y, b.x, b.y);
        } else if (hoverCell != null && !vertices.isEmpty()) {
            g2.setColor(COLOR_PREVIEW);
            Point last = cellCenter(vertices.get(vertices.size() - 1));
            Point cur  = cellCenter(hoverCell);
            g2.drawLine(last.x, last.y, cur.x, cur.y);
        }

        for (int i = 0; i < vertices.size(); i++) {
            Point c = cellCenter(vertices.get(i));
            g2.setColor(COLOR_VERTEX);
            g2.fillOval(c.x - 5, c.y - 5, 10, 10);
            g2.setColor(Theme.TEXT_COLOR);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.drawString("V" + i + "(" + vertices.get(i).x + "," + vertices.get(i).y + ")",
                    c.x + 7, c.y - 4);
        }
    }

    private void drawSeed(Graphics2D g2) {
        if (seedPoint == null) return;
        Point c = cellCenter(seedPoint);
        g2.setColor(COLOR_SEED);
        g2.fillOval(c.x - 6, c.y - 6, 12, 12);
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.drawString("S", c.x + 8, c.y + 4);
    }

    private Point cellCenter(Point cell) {
        return new Point(cell.x * CELL_SIZE + CELL_SIZE / 2,
                         cell.y * CELL_SIZE + CELL_SIZE / 2);
    }
}