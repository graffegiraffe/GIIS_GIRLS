package by.rublevskaya.pinkeditor.ui;

import by.rublevskaya.pinkeditor.algorithms.lab1.*;
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
import java.util.Vector;

public class LineEditorPanel extends JPanel {
    private DrawingPanel canvas;
    private JTable logTable;
    private DefaultTableModel tableModel;
    private JComboBox<AlgorithmType> algoSelector;
    private JCheckBox debugModeCheck;
    private Point startPoint = null;
    private Point endPoint = null;

    private Timer animationTimer;
    public LineEditorPanel() {
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

        JLabel label = new JLabel("Отрезки: ");
        label.setFont(Theme.HEADER_FONT);
        label.setForeground(Theme.ACCENT_COLOR);

        algoSelector = new JComboBox<>(AlgorithmType.values());
        algoSelector.setBackground(Color.WHITE);
        algoSelector.setFont(Theme.MAIN_FONT);

        debugModeCheck = new JCheckBox("Сетка");
        debugModeCheck.setFont(Theme.BOLD_FONT);
        debugModeCheck.setForeground(Theme.TEXT_COLOR);
        debugModeCheck.setBackground(Color.WHITE);
        debugModeCheck.addActionListener(e -> {
            canvas.setDebugMode(debugModeCheck.isSelected());
            resetState();
        });

        JButton manualInputBtn = new StyledButton("Ввод координат");
        manualInputBtn.setBackground(new Color(255, 228, 225));
        manualInputBtn.addActionListener(e -> showManualInputDialog(false));

        JButton debugBtn = new StyledButton("ОТЛАДКА");
        debugBtn.setBackground(new Color(255, 200, 220));
        debugBtn.addActionListener(e -> showManualInputDialog(true));

        JButton clearBtn = new StyledButton("ОЧИСТИТЬ");
        clearBtn.setBackground(Theme.ACCENT_COLOR);
        clearBtn.addActionListener(e -> resetState());

        toolbar.add(label);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(algoSelector);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(debugModeCheck);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(manualInputBtn);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(debugBtn);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(clearBtn);

        add(toolbar, BorderLayout.NORTH);
    }

    private void initMainArea() {
        canvas = new DrawingPanel();

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (animationTimer != null && animationTimer.isRunning()) {
                    animationTimer.stop();
                }

                if (startPoint == null) {
                    startPoint = canvas.toGridPoint(e.getPoint());
                    tableModel.setRowCount(0);
                    canvas.tempLineStart = null;
                } else {
                    endPoint = canvas.toGridPoint(e.getPoint());
                    recalcLine();
                    startPoint = null;
                    endPoint = null;
                }
                canvas.repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (startPoint != null) {
                    canvas.tempLineStart = new Point(startPoint.x, startPoint.y);
                    canvas.currentMouse = canvas.toGridPoint(e.getPoint());
                    canvas.repaint();
                }
            }
        };

        canvas.addMouseListener(mouseHandler);
        canvas.addMouseMotionListener(mouseHandler);

        String[] columns = {"Шаг", "X", "Y", "Ошибка", "Plot", "I"};
        tableModel = new DefaultTableModel(columns, 0);

        logTable = new JTable(tableModel);
        logTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        logTable.setRowHeight(25);

        JTableHeader header = logTable.getTableHeader();
        header.setFont(Theme.HEADER_FONT);
        header.setBackground(Theme.BUTTON_COLOR);
        header.setForeground(Theme.TEXT_COLOR);

        JScrollPane scrollPane = new JScrollPane(logTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.ACCENT_COLOR, 2),
                "Лог выполнения", TitledBorder.CENTER, TitledBorder.TOP, Theme.HEADER_FONT, Theme.ACCENT_COLOR
        ));
        scrollPane.setPreferredSize(new Dimension(350, 0));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvas, scrollPane);
        splitPane.setResizeWeight(0.75);
        splitPane.setDividerSize(5);
        add(splitPane, BorderLayout.CENTER);
    }

    private void resetState() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        startPoint = null;
        endPoint = null;
        canvas.clear();
        tableModel.setRowCount(0);
    }

    private void recalcLine() {
        if (startPoint == null || endPoint == null) return;
        tableModel.setRowCount(0);

        configureTableColumns();

        LineAlgorithm algorithm = getSelectedAlgorithm();
        List<Pixel> pixels = algorithm.calculate(startPoint.x, startPoint.y, endPoint.x, endPoint.y, tableModel);
        canvas.setPixels(pixels);
    }

    private void startAnimation(int x1, int y1, int x2, int y2) {

        resetState();
        configureTableColumns();
        LineAlgorithm algorithm = getSelectedAlgorithm();

        DefaultTableModel tempModel = new DefaultTableModel();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            tempModel.addColumn(tableModel.getColumnName(i));
        }

        List<Pixel> fullPixelList = algorithm.calculate(x1, y1, x2, y2, tempModel);

        final int[] currentIndex = {0};
        List<Pixel> currentPixels = new ArrayList<>();

        animationTimer = new Timer(200, e -> {
            if (currentIndex[0] < fullPixelList.size()) {
                currentPixels.add(fullPixelList.get(currentIndex[0]));
                canvas.setPixels(currentPixels);

                if (currentIndex[0] < tempModel.getRowCount()) {
                    Vector rowData = (Vector) tempModel.getDataVector().get(currentIndex[0]);
                    Vector newRow = new Vector(rowData);
                    tableModel.addRow(newRow);

                    logTable.scrollRectToVisible(logTable.getCellRect(logTable.getRowCount() - 1, 0, true));
                }

                currentIndex[0]++;
            } else {
                while (currentIndex[0] < tempModel.getRowCount()) {
                    Vector rowData = (Vector) tempModel.getDataVector().get(currentIndex[0]);
                    tableModel.addRow(new Vector(rowData));
                    currentIndex[0]++;
                }
                ((Timer) e.getSource()).stop();
            }
        });
        animationTimer.start();
    }

    private void configureTableColumns() {
        AlgorithmType type = (AlgorithmType) algoSelector.getSelectedItem();
        if (type == AlgorithmType.BRESENHAM) {
            tableModel.setColumnIdentifiers(new String[]{"i", "e", "x", "y", "e'", "Plot (x,y)"});
        } else {
            tableModel.setColumnIdentifiers(new String[]{"Шаг", "X", "Y", "Ошибка", "Plot", "Интенс."});
        }
    }

    private LineAlgorithm getSelectedAlgorithm() {
        AlgorithmType type = (AlgorithmType) algoSelector.getSelectedItem();
        return switch (type) {
            case DDA -> new DDAAlgorithm();
            case BRESENHAM -> new BresenhamAlgorithm();
            case WU -> new WuAlgorithm();
            default -> new DDAAlgorithm();
        };
    }

    private void showManualInputDialog(boolean isDebug) {
        String title = isDebug ? "Ввод координат (ОТЛАДКА)" : "Ввод координат";
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Theme.BG_COLOR);
        dialog.setSize(350, 280);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField x1F = new JTextField(); JTextField y1F = new JTextField();
        JTextField x2F = new JTextField(); JTextField y2F = new JTextField();

        addInputRow(dialog, gbc, 0, "X1:", x1F);
        addInputRow(dialog, gbc, 1, "Y1:", y1F);
        addInputRow(dialog, gbc, 2, "X2:", x2F);
        addInputRow(dialog, gbc, 3, "Y2:", y2F);

        JButton drawBtn = new StyledButton(isDebug ? "Запустить" : "Нарисовать");
        if (isDebug) drawBtn.setBackground(new Color(255, 200, 220));

        drawBtn.addActionListener(e -> {
            try {
                int x1 = Integer.parseInt(x1F.getText());
                int y1 = Integer.parseInt(y1F.getText());
                int x2 = Integer.parseInt(x2F.getText());
                int y2 = Integer.parseInt(y2F.getText());

                if (isDebug) {
                    startAnimation(x1, y1, x2, y2);
                } else {
                    startPoint = new Point(x1, y1);
                    endPoint = new Point(x2, y2);
                    recalcLine();
                    startPoint = null; endPoint = null;
                    canvas.repaint();
                }
                dialog.dispose();
            } catch (Exception ex) { JOptionPane.showMessageDialog(dialog, "Ошибка ввода!"); }
        });

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        dialog.add(drawBtn, gbc);
        dialog.setVisible(true);
    }

    private void addInputRow(JDialog d, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        JLabel l = new JLabel(label); l.setFont(Theme.BOLD_FONT); l.setForeground(Theme.TEXT_COLOR);
        d.add(l, gbc);
        gbc.gridx = 1; field.setFont(Theme.MAIN_FONT); d.add(field, gbc);
    }
}