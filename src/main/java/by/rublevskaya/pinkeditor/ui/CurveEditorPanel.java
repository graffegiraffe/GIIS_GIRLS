package by.rublevskaya.pinkeditor.ui;

import by.rublevskaya.pinkeditor.algorithms.lab2.*;
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

public class CurveEditorPanel extends JPanel {
    private DrawingPanel canvas;
    private DefaultTableModel tableModel;
    private JComboBox<CurveType> curveSelector;
    private JCheckBox debugModeCheck;

    private JTextField param1Field;
    private JTextField param2Field;
    private JLabel param1Label;
    private JLabel param2Label;

    public CurveEditorPanel() {
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

        JLabel label = new JLabel("Кривые: ");
        label.setFont(Theme.HEADER_FONT);
        label.setForeground(Theme.ACCENT_COLOR);

        curveSelector = new JComboBox<>(CurveType.values());
        curveSelector.setBackground(Color.WHITE);
        curveSelector.setFont(Theme.MAIN_FONT);
        curveSelector.addActionListener(e -> updateParamFields());

        JPanel paramsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        paramsPanel.setBackground(Color.WHITE);

        param1Label = new JLabel("R:");
        param1Label.setFont(Theme.BOLD_FONT);
        param1Field = new JTextField("8", 3);
        param1Field.setFont(Theme.MAIN_FONT);

        param2Label = new JLabel("B:");
        param2Label.setFont(Theme.BOLD_FONT);
        param2Field = new JTextField("5", 3);
        param2Field.setFont(Theme.MAIN_FONT);

        paramsPanel.add(param1Label);
        paramsPanel.add(param1Field);
        paramsPanel.add(param2Label);
        paramsPanel.add(param2Field);

        debugModeCheck = new JCheckBox("Сетка");
        debugModeCheck.setFont(Theme.BOLD_FONT);
        debugModeCheck.setForeground(Theme.TEXT_COLOR);
        debugModeCheck.setBackground(Color.WHITE);
        debugModeCheck.addActionListener(e -> canvas.setDebugMode(debugModeCheck.isSelected()));

        JButton manualInputBtn = new StyledButton("Ввод параметров");
        manualInputBtn.setBackground(new Color(255, 228, 225));
        manualInputBtn.addActionListener(e -> showManualInputDialog());

        JButton clearBtn = new StyledButton("ОЧИСТИТЬ");
        clearBtn.setBackground(Theme.ACCENT_COLOR);
        clearBtn.addActionListener(e -> {
            canvas.clear();
            tableModel.setRowCount(0);
        });

        toolbar.add(label);
        toolbar.add(curveSelector);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(paramsPanel);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(debugModeCheck);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(manualInputBtn);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(clearBtn);

        add(toolbar, BorderLayout.NORTH);

        updateParamFields();
    }

    private void updateParamFields() {
        CurveType type = (CurveType) curveSelector.getSelectedItem();
        if (type == CurveType.CIRCLE) {
            param1Label.setText("R:");
            param2Label.setVisible(false);
            param2Field.setVisible(false);
        } else if (type == CurveType.PARABOLA) {
            param1Label.setText("P:");
            param2Label.setVisible(false);
            param2Field.setVisible(false);
        } else {
            param1Label.setText("A:");
            param2Label.setVisible(true);
            param2Label.setText("B:");
            param2Field.setVisible(true);
        }
        revalidate();
        repaint();
    }

    private void initMainArea() {
        canvas = new DrawingPanel();

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point center = canvas.toGridPoint(e.getPoint());
                try {
                    int p1 = Integer.parseInt(param1Field.getText());
                    int p2 = 0;
                    if (param2Field.isVisible()) p2 = Integer.parseInt(param2Field.getText());
                    drawCurve(center.x, center.y, p1, p2);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CurveEditorPanel.this, "Проверьте числа в полях R/A/B");
                }
            }
        });

        String[] columns = {"Шаг", "Del_i", "d", "d*", "Пиксель", "X", "Y", "Del_i+1", "Plot (x, y)"};
        tableModel = new DefaultTableModel(columns, 0);

        JTable logTable = new JTable(tableModel);
        logTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logTable.setRowHeight(25);

        logTable.getColumnModel().getColumn(0).setPreferredWidth(35); // Шаг
        logTable.getColumnModel().getColumn(4).setPreferredWidth(50); // Пиксель
        logTable.getColumnModel().getColumn(8).setPreferredWidth(80); // Plot

        JTableHeader header = logTable.getTableHeader();
        header.setFont(Theme.HEADER_FONT);
        header.setBackground(Theme.BUTTON_COLOR);
        header.setForeground(Theme.TEXT_COLOR);

        JScrollPane scrollPane = new JScrollPane(logTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.ACCENT_COLOR, 2),
                "Красивая таблица", TitledBorder.CENTER, TitledBorder.TOP, Theme.HEADER_FONT, Theme.ACCENT_COLOR
        ));
        scrollPane.setPreferredSize(new Dimension(550, 0));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvas, scrollPane);
        splitPane.setResizeWeight(0.60);
        splitPane.setDividerSize(5);
        add(splitPane, BorderLayout.CENTER);
    }

    private void showManualInputDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Параметры кривой", true);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Theme.BG_COLOR);
        dialog.setSize(350, 300);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField xcField = new JTextField("0");
        JTextField ycField = new JTextField("0");
        JTextField p1Field = new JTextField(param1Field.getText());
        JTextField p2Field = new JTextField(param2Field.getText());

        CurveType type = (CurveType) curveSelector.getSelectedItem();
        String p1Name = (type == CurveType.CIRCLE) ? "Радиус (R):" : (type == CurveType.PARABOLA ? "Параметр (P):" : "Полуось A:");
        boolean showP2 = (type == CurveType.ELLIPSE || type == CurveType.HYPERBOLA);

        addInputRow(dialog, gbc, 0, "Центр X:", xcField);
        addInputRow(dialog, gbc, 1, "Центр Y:", ycField);
        addInputRow(dialog, gbc, 2, p1Name, p1Field);
        if (showP2) {
            addInputRow(dialog, gbc, 3, "Полуось B:", p2Field);
        }

        JButton drawBtn = new StyledButton("Построить");
        drawBtn.addActionListener(e -> {
            try {
                int xc = Integer.parseInt(xcField.getText());
                int yc = Integer.parseInt(ycField.getText());
                int p1 = Integer.parseInt(p1Field.getText());
                int p2 = showP2 ? Integer.parseInt(p2Field.getText()) : 0;

                param1Field.setText(String.valueOf(p1));
                if(showP2) param2Field.setText(String.valueOf(p2));

                canvas.clear();
                tableModel.setRowCount(0);
                drawCurve(xc, yc, p1, p2);
                canvas.repaint();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Введите целые числа!ПОНЯЛИ??", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        dialog.add(drawBtn, gbc);
        dialog.setVisible(true);
    }

    private void addInputRow(JDialog d, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        JLabel l = new JLabel(label); l.setFont(Theme.BOLD_FONT); l.setForeground(Theme.TEXT_COLOR);
        d.add(l, gbc);
        gbc.gridx = 1; field.setFont(Theme.MAIN_FONT); d.add(field, gbc);
    }

    private void drawCurve(int xc, int yc, int p1, int p2) {
        CurveType type = (CurveType) curveSelector.getSelectedItem();
        int[] params = {p1, p2};

        CurveAlgorithm algorithm = switch (type) {
            case CIRCLE -> new CircleAlgorithm();
            case ELLIPSE -> new EllipseAlgorithm();
            case HYPERBOLA -> new HyperbolaAlgorithm();
            case PARABOLA -> new ParabolaAlgorithm();
        };

        Graphics2D g2 = (Graphics2D) canvas.getGraphics();
        List<Pixel> pixels = algorithm.calculate(xc, yc, params, tableModel);
        canvas.setPixels(pixels);
    }
}