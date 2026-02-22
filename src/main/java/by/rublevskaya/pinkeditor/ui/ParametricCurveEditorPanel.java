package by.rublevskaya.pinkeditor.ui;

import by.rublevskaya.pinkeditor.algorithms.lab3.*;
import by.rublevskaya.pinkeditor.model.Pixel;
import by.rublevskaya.pinkeditor.ui.components.StyledButton;
import by.rublevskaya.pinkeditor.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ParametricCurveEditorPanel extends JPanel {
    private DrawingPanel canvas;
    private JTable logTable;
    private DefaultTableModel tableModel;
    private JComboBox<CurveMethodType> methodSelector;
    private JCheckBox debugModeCheck;
    private JCheckBox showControlLinesCheck;

    private List<Point> controlPoints = new ArrayList<>();
    private Point draggingPoint = null;
    private int draggingIndex = -1;

    private Timer animationTimer;
    private static final int POINT_RADIUS = 6;

    public ParametricCurveEditorPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_COLOR);
        initToolbar();
        initMainArea();
    }

    private void initToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setBackground(Color.WHITE);
        toolbar.setFloatable(false);
        toolbar.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Метод: ");
        label.setFont(Theme.HEADER_FONT);
        label.setForeground(Theme.ACCENT_COLOR);

        methodSelector = new JComboBox<>(CurveMethodType.values());
        methodSelector.setBackground(Color.WHITE);
        methodSelector.setFont(Theme.MAIN_FONT);
        methodSelector.addActionListener(e -> updateCurve());

        debugModeCheck = new JCheckBox("Сетка");
        debugModeCheck.setFont(Theme.BOLD_FONT);
        debugModeCheck.setForeground(Theme.TEXT_COLOR);
        debugModeCheck.setBackground(Color.WHITE);
        debugModeCheck.addActionListener(e -> {
            canvas.setDebugMode(debugModeCheck.isSelected());
            canvas.repaint();
        });

        showControlLinesCheck = new JCheckBox("Линии управления");
        showControlLinesCheck.setFont(Theme.BOLD_FONT);
        showControlLinesCheck.setForeground(Theme.TEXT_COLOR);
        showControlLinesCheck.setBackground(Color.WHITE);
        showControlLinesCheck.setSelected(true);
        showControlLinesCheck.addActionListener(e -> canvas.repaint());

        JButton manualInputBtn = new StyledButton("Ввод точек");
        manualInputBtn.setBackground(new Color(255, 228, 225));
        manualInputBtn.addActionListener(e -> showManualInputDialog(false));

        JButton debugBtn = new StyledButton("ОТЛАДКА");
        debugBtn.setBackground(new Color(255, 200, 220));
        debugBtn.addActionListener(e -> showManualInputDialog(true));

        JButton clearBtn = new StyledButton("ОЧИСТИТЬ");
        clearBtn.setBackground(Theme.ACCENT_COLOR);
        clearBtn.addActionListener(e -> {
            stopAnimation();
            controlPoints.clear();
            canvas.clear();
            tableModel.setRowCount(0);
        });

        JButton helpBtn = new StyledButton("?");
        helpBtn.setBackground(new Color(200, 220, 255));
        helpBtn.addActionListener(e -> showHelp());

        toolbar.add(label);
        toolbar.add(methodSelector);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(debugModeCheck);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(showControlLinesCheck);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(manualInputBtn);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(debugBtn);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(helpBtn);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(clearBtn);

        add(toolbar, BorderLayout.NORTH);
    }

    private void initMainArea() {
        canvas = new DrawingPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int gs = 30;
                boolean isDebug = debugModeCheck.isSelected();

                java.util.function.Function<Point, Point> toScreen = (p) -> {
                    if (isDebug) {
                        return new Point(p.x * gs + gs / 2, p.y * gs + gs / 2);
                    }
                    return p;
                };

                if (showControlLinesCheck.isSelected() && controlPoints.size() > 1) {
                    g2.setColor(new Color(150, 150, 150, 150));
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                            0, new float[]{5, 5}, 0));

                    CurveMethodType method = (CurveMethodType) methodSelector.getSelectedItem();

                    if (method == CurveMethodType.HERMITE && controlPoints.size() >= 4) {
                        Point p1 = toScreen.apply(controlPoints.get(0));
                        Point p4 = toScreen.apply(controlPoints.get(1));
                        Point r1 = toScreen.apply(controlPoints.get(2));
                        Point r4 = toScreen.apply(controlPoints.get(3));
                        g2.drawLine(p1.x, p1.y, r1.x, r1.y);
                        g2.drawLine(p4.x, p4.y, r4.x, r4.y);
                    } else {
                        for (int i = 0; i < controlPoints.size() - 1; i++) {
                            Point pStart = toScreen.apply(controlPoints.get(i));
                            Point pEnd = toScreen.apply(controlPoints.get(i+1));
                            g2.drawLine(pStart.x, pStart.y, pEnd.x, pEnd.y);
                        }
                    }
                }

                for (int i = 0; i < controlPoints.size(); i++) {
                    Point p = toScreen.apply(controlPoints.get(i));

                    g2.setColor(new Color(0, 0, 0, 50));
                    g2.fillOval(p.x - POINT_RADIUS + 2, p.y - POINT_RADIUS + 2, POINT_RADIUS * 2, POINT_RADIUS * 2);

                    g2.setColor(i == draggingIndex ? new Color(255, 100, 150) : new Color(100, 150, 255));
                    g2.fillOval(p.x - POINT_RADIUS, p.y - POINT_RADIUS, POINT_RADIUS * 2, POINT_RADIUS * 2);

                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawOval(p.x - POINT_RADIUS, p.y - POINT_RADIUS, POINT_RADIUS * 2, POINT_RADIUS * 2);

                    g2.setColor(Theme.TEXT_COLOR);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    g2.drawString(getPointLabel(i), p.x + POINT_RADIUS + 4, p.y - 4);
                }
            }
        };

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point click = e.getPoint();

                for (int i = 0; i < controlPoints.size(); i++) {
                    Point p = controlPoints.get(i);
                    if (click.distance(p) <= POINT_RADIUS) {
                        draggingPoint = p;
                        draggingIndex = i;
                        return;
                    }
                }

                controlPoints.add(click);
                updateCurve();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (draggingPoint != null) {
                    draggingPoint = null;
                    draggingIndex = -1;
                    updateCurve();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggingPoint != null) {
                    draggingPoint.setLocation(e.getPoint());
                    canvas.repaint();
                }
            }
        };

        canvas.addMouseListener(mouseHandler);
        canvas.addMouseMotionListener(mouseHandler);

        String[] columns = {"t / Шаг", "Тип", "Координаты", "Коэфф. 1-2", "Коэфф. 3-4"};
        tableModel = new DefaultTableModel(columns, 0);

        logTable = new JTable(tableModel);
        logTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logTable.setRowHeight(25);

        logTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        logTable.getColumnModel().getColumn(1).setPreferredWidth(70);
        logTable.getColumnModel().getColumn(2).setPreferredWidth(100);

        JTableHeader header = logTable.getTableHeader();
        header.setFont(Theme.HEADER_FONT);
        header.setBackground(Theme.BUTTON_COLOR);
        header.setForeground(Theme.TEXT_COLOR);

        JScrollPane scrollPane = new JScrollPane(logTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.ACCENT_COLOR, 2),
                "Таблица вычислений", TitledBorder.CENTER, TitledBorder.TOP,
                Theme.HEADER_FONT, Theme.ACCENT_COLOR
        ));
        scrollPane.setPreferredSize(new Dimension(500, 0));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvas, scrollPane);
        splitPane.setResizeWeight(0.65);
        splitPane.setDividerSize(5);
        add(splitPane, BorderLayout.CENTER);
    }

    private String getPointLabel(int index) {
        CurveMethodType method = (CurveMethodType) methodSelector.getSelectedItem();

        if (method == CurveMethodType.HERMITE) {
            return switch (index) {
                case 0 -> "P1";
                case 1 -> "P4";
                case 2 -> "R1";
                case 3 -> "R4";
                default -> "P" + (index + 1);
            };
        } else if (method == CurveMethodType.BEZIER) {
            return "P" + (index + 1);
        } else {
            return "V" + index;
        }
    }

    private void updateCurve() {
        stopAnimation();
        tableModel.setRowCount(0);

        CurveMethodType method = (CurveMethodType) methodSelector.getSelectedItem();
        ParametricCurveAlgorithm algorithm = getAlgorithm(method);

        int required = algorithm.getRequiredPoints();
        if (required > 0 && controlPoints.size() < required) {
            canvas.setPixels(new ArrayList<>());
            return;
        }

        if (method == CurveMethodType.BSPLINE && controlPoints.size() < 4) {
            canvas.setPixels(new ArrayList<>());
            return;
        }

        int[][] points = new int[controlPoints.size()][2];
        for (int i = 0; i < controlPoints.size(); i++) {
            points[i][0] = controlPoints.get(i).x;
            points[i][1] = controlPoints.get(i).y;
        }

        List<Pixel> pixels = algorithm.calculate(points, tableModel);
        canvas.setPixels(pixels);
    }

    private ParametricCurveAlgorithm getAlgorithm(CurveMethodType method) {
        return switch (method) {
            case HERMITE -> new HermiteAlgorithm();
            case BEZIER -> new BezierAlgorithm();
            case BSPLINE -> new BSplineAlgorithm();
        };
    }

    private void showManualInputDialog(boolean isDebug) {
        CurveMethodType method = (CurveMethodType) methodSelector.getSelectedItem();
        ParametricCurveAlgorithm algorithm = getAlgorithm(method);

        String title = isDebug ? "Ввод точек (ОТЛАДКА)" : "Ввод контрольных точек";
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Theme.BG_COLOR);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel infoLabel = new JLabel("<html><b>Метод: " + method + "</b><br>" +
                String.join("<br>", algorithm.getPointLabels()) + "</html>");
        infoLabel.setFont(Theme.MAIN_FONT);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        dialog.add(infoLabel, gbc);
        gbc.gridwidth = 1;

        List<JTextField> xFields = new ArrayList<>();
        List<JTextField> yFields = new ArrayList<>();

        int numPoints = method == CurveMethodType.BSPLINE ? 6 : 4;

        int currentRow = 1;
        for (int i = 0; i < numPoints; i++) {
            JLabel label = new JLabel(getPointLabel(i) + ":");
            label.setFont(Theme.BOLD_FONT);
            label.setForeground(Theme.ACCENT_COLOR);
            gbc.gridx = 0; gbc.gridy = currentRow; gbc.gridwidth = 2;
            dialog.add(label, gbc);
            currentRow++;

            // Поля X и Y
            gbc.gridwidth = 1;
            JTextField xField = new JTextField("0", 8);
            JTextField yField = new JTextField("0", 8);
            xField.setFont(Theme.MAIN_FONT);
            yField.setFont(Theme.MAIN_FONT);

            // X координата
            gbc.gridx = 0; gbc.gridy = currentRow;
            JLabel xLabel = new JLabel("  X:");
            xLabel.setFont(Theme.MAIN_FONT);
            dialog.add(xLabel, gbc);
            gbc.gridx = 1;
            dialog.add(xField, gbc);
            currentRow++;

            // Y координата
            gbc.gridx = 0; gbc.gridy = currentRow;
            JLabel yLabel = new JLabel("  Y:");
            yLabel.setFont(Theme.MAIN_FONT);
            dialog.add(yLabel, gbc);
            gbc.gridx = 1;
            dialog.add(yField, gbc);
            currentRow++;

            xFields.add(xField);
            yFields.add(yField);
        }

        JButton drawBtn = new StyledButton(isDebug ? "Запустить" : "Построить");
        if (isDebug) drawBtn.setBackground(new Color(255, 200, 220));

        drawBtn.addActionListener(e -> {
            try {
                controlPoints.clear();
                for (int i = 0; i < xFields.size(); i++) {
                    int x = Integer.parseInt(xFields.get(i).getText());
                    int y = Integer.parseInt(yFields.get(i).getText());
                    controlPoints.add(new Point(x, y));
                }

                if (isDebug) {
                    startAnimation();
                } else {
                    updateCurve();
                }
                canvas.repaint();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Введите целые числа!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = currentRow + 1; gbc.gridwidth = 2;
        dialog.add(drawBtn, gbc);
        dialog.setVisible(true);
    }

    private void startAnimation() {
        stopAnimation();
        tableModel.setRowCount(0);

        CurveMethodType method = (CurveMethodType) methodSelector.getSelectedItem();
        ParametricCurveAlgorithm algorithm = getAlgorithm(method);

        int[][] points = new int[controlPoints.size()][2];
        for (int i = 0; i < controlPoints.size(); i++) {
            points[i][0] = controlPoints.get(i).x;
            points[i][1] = controlPoints.get(i).y;
        }

        DefaultTableModel tempModel = new DefaultTableModel();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            tempModel.addColumn(tableModel.getColumnName(i));
        }

        List<Pixel> allPixels = algorithm.calculate(points, tempModel);

        final int[] rowIndex = {0};
        final int[] pixelIndex = {0};
        int totalRows = tempModel.getRowCount();

        List<Pixel> currentPixels = new ArrayList<>();

        int pixelsPerStep = allPixels.size() / Math.max(1, totalRows);

        animationTimer = new Timer(100, e -> {
            if (rowIndex[0] < totalRows) {
                tableModel.addRow((Object[]) tempModel.getDataVector().get(rowIndex[0]).toArray());

                int limit = Math.min(pixelIndex[0] + pixelsPerStep, allPixels.size());
                for (int i = pixelIndex[0]; i < limit; i++) {
                    currentPixels.add(allPixels.get(i));
                }
                pixelIndex[0] = limit;

                canvas.setPixels(currentPixels);

                if (logTable != null) {
                    logTable.scrollRectToVisible(logTable.getCellRect(logTable.getRowCount() - 1, 0, true));
                }

                rowIndex[0]++;
            } else {
                if (pixelIndex[0] < allPixels.size()) {
                    currentPixels.addAll(allPixels.subList(pixelIndex[0], allPixels.size()));
                    canvas.setPixels(currentPixels);
                }
                ((Timer)e.getSource()).stop();
            }
        });
        animationTimer.start();
    }

    private void stopAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }

    private void showHelp() {
        String helpText = """
            <html><body style='width: 300px; font-family: Segoe UI;'>
            <h2 style='color: #DB7093;'>Как пользоваться:</h2>
            
            <h3>Эрмит:</h3>
            <ul>
            <li>P1, P4 - концевые точки</li>
            <li>R1, R4 - векторы касательных</li>
            <li>Нужно 4 точки</li>
            </ul>
            
            <h3>Безье:</h3>
            <ul>
            <li>P1, P4 - концевые точки</li>
            <li>P2, P3 - опорные точки</li>
            <li>Нужно 4 точки</li>
            </ul>
            
            <h3>B-сплайн:</h3>
            <ul>
            <li>Минимум 4 точки</li>
            <li>Кривая не проходит через точки</li>
            <li>Можно добавлять сколько угодно</li>
            </ul>
            
            <p><b>Управление:</b></p>
            <ul>
            <li>Клик - добавить точку</li>
            <li>Перетаскивание - переместить точку</li>
            </ul>
            </body></html>
            """;

        JOptionPane.showMessageDialog(this, helpText, "Справка", JOptionPane.INFORMATION_MESSAGE);
    }
}