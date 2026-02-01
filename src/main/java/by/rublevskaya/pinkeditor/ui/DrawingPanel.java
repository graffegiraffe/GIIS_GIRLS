package by.rublevskaya.pinkeditor.ui;

import by.rublevskaya.pinkeditor.model.Pixel;
import by.rublevskaya.pinkeditor.util.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DrawingPanel extends JPanel {
    private boolean isDebug = false;
    private List<Pixel> pixels = new ArrayList<>();
    private int gridSize = 30;

    public Point tempLineStart = null;
    public Point currentMouse = null;

    public DrawingPanel() {
        setBackground(Color.WHITE);
    }

    public void setDebugMode(boolean debug) {
        this.isDebug = debug;
        repaint();
    }

    public void setPixels(List<Pixel> pixels) {
        this.pixels = pixels;
        repaint();
    }

    public void clear() {
        pixels.clear();
        tempLineStart = null;
        currentMouse = null;
        repaint();
    }

    public Point toGridPoint(Point screenPoint) {
        if (isDebug) {
            return new Point(screenPoint.x / gridSize, screenPoint.y / gridSize);
        } else {
            return screenPoint;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isDebug) {
            drawGrid(g2);
            drawPixelsOnGrid(g2);
        } else {
            drawPixelsNormal(g2);
        }

        if (tempLineStart != null && currentMouse != null) {
            g2.setColor(new Color(180, 180, 180));
            g2.setStroke(new BasicStroke(2.0f));
            if (isDebug) {
                g2.drawLine(tempLineStart.x * gridSize + gridSize/2, tempLineStart.y * gridSize + gridSize/2,
                        currentMouse.x * gridSize + gridSize/2, currentMouse.y * gridSize + gridSize/2);
            } else {
                g2.drawLine(tempLineStart.x, tempLineStart.y, currentMouse.x, currentMouse.y);
            }
        }
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(230, 220, 225));
        int w = getWidth();
        int h = getHeight();
        for (int i = 0; i <= w; i += gridSize) g2.drawLine(i, 0, i, h);
        for (int i = 0; i <= h; i += gridSize) g2.drawLine(0, i, w, i);
    }

    private void drawPixelsOnGrid(Graphics2D g2) {
        for (Pixel p : pixels) {
            int alpha = (int)(p.getIntensity() * 255);
            if (alpha > 255) alpha = 255; if (alpha < 0) alpha = 0;

            Color c = new Color(Theme.ACCENT_COLOR.getRed(), Theme.ACCENT_COLOR.getGreen(), Theme.ACCENT_COLOR.getBlue(), alpha);
            g2.setColor(c);
            g2.fillRect(p.getX() * gridSize, p.getY() * gridSize, gridSize, gridSize);
            g2.setColor(Color.GRAY);
            g2.drawRect(p.getX() * gridSize, p.getY() * gridSize, gridSize, gridSize);

            if (gridSize > 25) {
                g2.setColor(Theme.TEXT_COLOR);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.drawString(p.getX() + "," + p.getY(), p.getX() * gridSize + 4, p.getY() * gridSize + 18);
            }
        }
    }

    private void drawPixelsNormal(Graphics2D g2) {
        for (Pixel p : pixels) {
            int alpha = (int)(p.getIntensity() * 255);
            g2.setColor(new Color(Theme.ACCENT_COLOR.getRed(), Theme.ACCENT_COLOR.getGreen(), Theme.ACCENT_COLOR.getBlue(), alpha));
            g2.fillRect(p.getX(), p.getY(), 3, 3);
        }
    }
}
