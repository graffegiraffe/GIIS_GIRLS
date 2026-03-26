package by.rublevskaya.pinkeditor.ui;

import by.rublevskaya.pinkeditor.algorithms.lab5.*;
import by.rublevskaya.pinkeditor.ui.components.StyledButton;
import by.rublevskaya.pinkeditor.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class PolygonFillEditorPanel extends JPanel {
    private PolygonCanvas             canvas;
    private JTable                    logTable;
    private DefaultTableModel         tableModel;
    private JComboBox<PolygonFillAlgorithmType> algoSelector;
    private JLabel                    statusLabel;
    private Timer animationTimer;

    public PolygonFillEditorPanel() {
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

        JLabel label = new JLabel("Заполнение полигонов: ");
        label.setFont(Theme.HEADER_FONT);
        label.setForeground(Theme.ACCENT_COLOR);

        algoSelector = new JComboBox<>(PolygonFillAlgorithmType.values());
        algoSelector.setBackground(Color.WHITE);
        algoSelector.setFont(Theme.MAIN_FONT);
        algoSelector.addActionListener(e -> updateStatus());

        JButton closePolyBtn = new StyledButton("Закрыть полигон");
        closePolyBtn.setBackground(new Color(255, 220, 230));
        closePolyBtn.addActionListener(e -> {
            if (canvas.getVertices().size() >= 3 && !canvas.isPolygonClosed()) {
                canvas.handleClosePolygon();
                updateStatus();
            }
        });

        JButton fillBtn = new StyledButton("Заполнить");
        fillBtn.setBackground(new Color(255, 228, 225));
        fillBtn.addActionListener(e -> runFill(false));

        JButton debugBtn = new StyledButton("ОТЛАДКА");
        debugBtn.setBackground(new Color(255, 200, 220));
        debugBtn.addActionListener(e -> runFill(true));

        JButton undoBtn = new StyledButton("← Удалить");
        undoBtn.setBackground(new Color(255, 235, 205));
        undoBtn.addActionListener(e -> resetState());

        JButton clearBtn = new StyledButton("ОЧИСТИТЬ");
        clearBtn.setBackground(Theme.ACCENT_COLOR);
        clearBtn.addActionListener(e -> resetState());

        statusLabel = new JLabel("Кликайте на холст для добавления вершин");
        statusLabel.setFont(Theme.SMALL_FONT);
        statusLabel.setForeground(Theme.TEXT_COLOR);

        toolbar.add(label);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(algoSelector);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(closePolyBtn);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(fillBtn);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(debugBtn);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(undoBtn);
        toolbar.add(Box.createHorizontalStrut(16));
        toolbar.add(statusLabel);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(clearBtn);

        add(toolbar, BorderLayout.NORTH);
    }

    private void initMainArea() {
        canvas = new PolygonCanvas();
        canvas.setOnStateChanged(this::updateStatus);

        String[] cols = {"Шаг", "Y", "X нач.", "X кон.", "Пикселей"};
        tableModel = new DefaultTableModel(cols, 0);

        logTable = new JTable(tableModel);
        logTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        logTable.setRowHeight(24);

        JTableHeader header = logTable.getTableHeader();
        header.setFont(Theme.HEADER_FONT);
        header.setBackground(Theme.BUTTON_COLOR);
        header.setForeground(Theme.TEXT_COLOR);

        JScrollPane scrollPane = new JScrollPane(logTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.ACCENT_COLOR, 2),
                "Лог выполнения", TitledBorder.CENTER, TitledBorder.TOP,
                Theme.HEADER_FONT, Theme.ACCENT_COLOR));
        scrollPane.setPreferredSize(new Dimension(370, 0));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvas, scrollPane);
        split.setResizeWeight(0.75);
        split.setDividerSize(5);
        add(split, BorderLayout.CENTER);
    }

    private void runFill(boolean animate) {
        if (animationTimer != null && animationTimer.isRunning()) animationTimer.stop();

        if (!canvas.isPolygonClosed()) {
            JOptionPane.showMessageDialog(this,
                    "Сначала закройте полигон (правая кнопка мыши или кнопка «Закрыть полигон»).",
                    "Полигон не закрыт", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        PolygonFillAlgorithmType type =
                (PolygonFillAlgorithmType) algoSelector.getSelectedItem();

        boolean needsSeed = type == PolygonFillAlgorithmType.SIMPLE_FLOOD_FILL
                         || type == PolygonFillAlgorithmType.SCANLINE_FLOOD_FILL;

        if (needsSeed && canvas.getSeedPoint() == null) {
            JOptionPane.showMessageDialog(this,
                    "Кликните левой кнопкой мыши внутри полигона для выбора затравки.",
                    "Затравка не задана", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (needsSeed) canvas.showBoundary();
        PolygonFillAlgorithm algorithm = createAlgorithm(type);
        String[]              cols      = algorithm.getTableColumns();
        DefaultTableModel tempModel = new DefaultTableModel(cols, 0);
        int gw = canvas.getGridWidth();
        int gh = canvas.getGridHeight();
        List<List<Point>> allSteps = algorithm.fill(
                new ArrayList<>(canvas.getVertices()),
                canvas.getSeedPoint(), gw, gh, tempModel);
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(cols);
        canvas.setFilledPixels(new ArrayList<>());

        if (!animate) {
            List<Point> all = new ArrayList<>();
            for (List<Point> step : allSteps) all.addAll(step);
            canvas.setFilledPixels(all);
            for (int r = 0; r < tempModel.getRowCount(); r++) {
                tableModel.addRow(getRow(tempModel, r));
            }
        } else {
            final int[] idx = {0};
            final List<Point> accumulated = new ArrayList<>();

            animationTimer = new Timer(120, (ActionEvent ev) -> {
                if (idx[0] < allSteps.size()) {
                    accumulated.addAll(allSteps.get(idx[0]));
                    canvas.setFilledPixels(accumulated);

                    if (idx[0] < tempModel.getRowCount()) {
                        tableModel.addRow(getRow(tempModel, idx[0]));
                        logTable.scrollRectToVisible(
                                logTable.getCellRect(logTable.getRowCount() - 1, 0, true));
                    }
                    idx[0]++;
                } else {
                    while (idx[0] < tempModel.getRowCount()) {
                        tableModel.addRow(getRow(tempModel, idx[0]++));
                    }
                    ((Timer) ev.getSource()).stop();
                }
            });
            animationTimer.start();
        }
    }

    @SuppressWarnings("unchecked")
    private Vector<Object> getRow(DefaultTableModel m, int row) {
        return new Vector<>((Vector<?>) m.getDataVector().get(row));
    }

    private PolygonFillAlgorithm createAlgorithm(PolygonFillAlgorithmType type) {
        return switch (type) {
            case ORDERED_EDGE_LIST  -> new OrderedEdgeListAlgorithm();
            case ACTIVE_EDGE_LIST   -> new ActiveEdgeListAlgorithm();
            case SIMPLE_FLOOD_FILL  -> new SimpleFloodFillAlgorithm();
            case SCANLINE_FLOOD_FILL -> new ScanLineFloodFillAlgorithm();
        };
    }

    private void resetState() {
        if (animationTimer != null && animationTimer.isRunning()) animationTimer.stop();
        canvas.clear();
        tableModel.setRowCount(0);
        updateStatus();
    }

    private void updateStatus() {
        if (!canvas.isPolygonClosed()) {
            int n = canvas.getVertices().size();
            statusLabel.setText(n == 0
                    ? "Кликайте на холст для добавления вершин"
                    : "Вершин добавлено: " + n + " | ПКМ чтобы закрыть полигон");
        } else {
            PolygonFillAlgorithmType type =
                    (PolygonFillAlgorithmType) algoSelector.getSelectedItem();
            boolean needsSeed = type == PolygonFillAlgorithmType.SIMPLE_FLOOD_FILL
                             || type == PolygonFillAlgorithmType.SCANLINE_FLOOD_FILL;
            if (needsSeed && canvas.getSeedPoint() == null) {
                statusLabel.setText("Полигон закрыт. Кликните внутри для выбора затравки (зелёный маркер)");
            } else {
                statusLabel.setText("Полигон готов. Нажмите «Заполнить» или «ОТЛАДКА»");
            }
        }
    }
}