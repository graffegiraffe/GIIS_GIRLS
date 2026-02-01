package by.rublevskaya.pinkeditor.ui;

import by.rublevskaya.pinkeditor.algorithms.AlgorithmType;
import by.rublevskaya.pinkeditor.algorithms.BresenhamAlgorithm;
import by.rublevskaya.pinkeditor.algorithms.DDAAlgorithm;
import by.rublevskaya.pinkeditor.algorithms.LineAlgorithm;
import by.rublevskaya.pinkeditor.algorithms.WuAlgorithm;
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
import java.util.List;

public class MainFrame extends JFrame {
    private DrawingPanel canvas;
    private JTable logTable;
    private DefaultTableModel tableModel;
    private JComboBox<AlgorithmType> algoSelector;
    private JCheckBox debugModeCheck;

    private Point startPoint = null;
    private Point endPoint = null;

    public MainFrame() {
        setTitle("Pink Graphic Editor | Lab 1 (Refactored)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setupGlobalUI();
        initMenu();
        initToolbar();
        initMainArea();

        setVisible(true);
    }

    private void setupGlobalUI() {
        UIManager.put("Panel.background", Theme.BG_COLOR);
        UIManager.put("CheckBox.background", Theme.BG_COLOR);
        UIManager.put("SplitPane.background", Theme.BG_COLOR);
        UIManager.put("OptionPane.background", Theme.BG_COLOR);
        UIManager.put("Button.font", Theme.BOLD_FONT);
        UIManager.put("Label.font", Theme.MAIN_FONT);
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0,0,2,0, new Color(240, 230, 235)));

        JMenu fileMenu = new JMenu("Файл");
        fileMenu.setFont(Theme.MAIN_FONT);
        JMenuItem exitItem = new JMenuItem("Выход");
        exitItem.setFont(Theme.MAIN_FONT);
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    private void initToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setBackground(Color.WHITE);
        toolbar.setFloatable(false);
        toolbar.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Инструменты: ");
        label.setFont(Theme.HEADER_FONT);
        label.setForeground(Theme.ACCENT_COLOR);

        algoSelector = new JComboBox<>(AlgorithmType.values());
        algoSelector.setBackground(Color.WHITE);
        algoSelector.setFont(Theme.MAIN_FONT);

        debugModeCheck = new JCheckBox("Сетка (Отладка)");
        debugModeCheck.setFont(Theme.BOLD_FONT);
        debugModeCheck.setForeground(Theme.TEXT_COLOR);
        debugModeCheck.setBackground(Color.WHITE);
        debugModeCheck.addActionListener(e -> {
            canvas.setDebugMode(debugModeCheck.isSelected());
            resetState();
        });

        JButton manualInputBtn = new StyledButton("Ввести координаты");
        manualInputBtn.setBackground(new Color(255, 228, 225));
        manualInputBtn.addActionListener(e -> showManualInputDialog());

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
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(clearBtn);

        add(toolbar, BorderLayout.NORTH);
    }

    private void initMainArea() {
        canvas = new DrawingPanel();

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
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

        String[] columns = {"Шаг", "X", "Y", "Ошибка (e)", "Plot(x,y)", "Интенс."};
        tableModel = new DefaultTableModel(columns, 0);
        JTable logTable = new JTable(tableModel);
        logTable.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        logTable.setRowHeight(30);

        JTableHeader header = logTable.getTableHeader();
        header.setFont(Theme.HEADER_FONT);
        header.setBackground(Theme.BUTTON_COLOR);
        header.setForeground(Theme.TEXT_COLOR);

        JScrollPane scrollPane = new JScrollPane(logTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.ACCENT_COLOR, 2),
                "Пошаговое решение", TitledBorder.CENTER, TitledBorder.TOP, Theme.HEADER_FONT, Theme.ACCENT_COLOR
        ));
        scrollPane.setPreferredSize(new Dimension(400, 0));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvas, scrollPane);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerSize(8);
        add(splitPane, BorderLayout.CENTER);
    }

    private void resetState() {
        startPoint = null;
        endPoint = null;
        canvas.clear();
        tableModel.setRowCount(0);
    }

    private void recalcLine() {
        if (startPoint == null || endPoint == null) return;
        tableModel.setRowCount(0);

        AlgorithmType type = (AlgorithmType) algoSelector.getSelectedItem();
        LineAlgorithm algorithm = switch (type) {
            case DDA -> new DDAAlgorithm();
            case BRESENHAM -> new BresenhamAlgorithm();
            case WU -> new WuAlgorithm();
            default -> new DDAAlgorithm();
        };

        List<Pixel> pixels = algorithm.calculate(startPoint.x, startPoint.y, endPoint.x, endPoint.y, tableModel);
        canvas.setPixels(pixels);
    }

    private void showManualInputDialog() {
        JDialog dialog = new JDialog(this, "Ввод координат", true);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Theme.BG_COLOR);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField x1Field = new JTextField(); x1Field.setFont(Theme.MAIN_FONT);
        JTextField y1Field = new JTextField(); y1Field.setFont(Theme.MAIN_FONT);
        JTextField x2Field = new JTextField(); x2Field.setFont(Theme.MAIN_FONT);
        JTextField y2Field = new JTextField(); y2Field.setFont(Theme.MAIN_FONT);

        addInputRow(dialog, gbc, 0, "X1:", x1Field);
        addInputRow(dialog, gbc, 1, "Y1:", y1Field);
        addInputRow(dialog, gbc, 2, "X2:", x2Field);
        addInputRow(dialog, gbc, 3, "Y2:", y2Field);

        JButton drawBtn = new StyledButton("Нарисовать");
        drawBtn.addActionListener(e -> {
            try {
                int x1 = Integer.parseInt(x1Field.getText());
                int y1 = Integer.parseInt(y1Field.getText());
                int x2 = Integer.parseInt(x2Field.getText());
                int y2 = Integer.parseInt(y2Field.getText());
                startPoint = new Point(x1, y1);
                endPoint = new Point(x2, y2);
                recalcLine();
                startPoint = null; endPoint = null;
                canvas.repaint();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Введите целые числа!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        dialog.add(drawBtn, gbc);
        dialog.setVisible(true);
    }

    private void addInputRow(JDialog d, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        JLabel l = new JLabel(label);
        l.setFont(Theme.BOLD_FONT);
        l.setForeground(Theme.TEXT_COLOR);
        d.add(l, gbc);
        gbc.gridx = 1;
        d.add(field, gbc);
    }
}
