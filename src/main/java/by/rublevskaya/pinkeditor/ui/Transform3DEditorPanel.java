package by.rublevskaya.pinkeditor.ui;

import by.rublevskaya.pinkeditor.algorithms.lab1.BresenhamAlgorithm;
import by.rublevskaya.pinkeditor.model.Pixel;
import by.rublevskaya.pinkeditor.model.geom3d.Matrix4x4;
import by.rublevskaya.pinkeditor.model.geom3d.Object3D;
import by.rublevskaya.pinkeditor.model.geom3d.Point3D;
import by.rublevskaya.pinkeditor.ui.components.StyledButton;
import by.rublevskaya.pinkeditor.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Transform3DEditorPanel extends JPanel {
    private DrawingPanel canvas;
    private JTable logTable;
    private DefaultTableModel tableModel;
    private Object3D object3D;
    private JCheckBox perspectiveCheck;
    private JTextField dField;

    public Transform3DEditorPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_COLOR);
        object3D = new Object3D();
        initToolbar();
        initMainArea();

        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
    }

    private void initToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setBackground(Color.WHITE);
        toolbar.setFloatable(false);
        toolbar.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("3D Преобразования: ");
        label.setFont(Theme.HEADER_FONT);
        label.setForeground(Theme.ACCENT_COLOR);

        JButton loadBtn = new StyledButton("Загрузить из файла");
        loadBtn.setBackground(new Color(255, 228, 225));
        loadBtn.addActionListener(e -> loadObjectFromFile());

        perspectiveCheck = new JCheckBox("Перспектива");
        perspectiveCheck.setFont(Theme.BOLD_FONT);
        perspectiveCheck.setBackground(Color.WHITE);
        perspectiveCheck.addActionListener(e -> drawObject());

        dField = new JTextField("500", 4);
        dField.setFont(Theme.MAIN_FONT);

        JButton resetBtn = new StyledButton("СБРОС");
        resetBtn.setBackground(Theme.BUTTON_COLOR);
        resetBtn.addActionListener(e -> {
            object3D.resetTransform();
            drawObject();
            requestFocusInWindow();
        });

        JButton helpBtn = new StyledButton("Управление");
        helpBtn.setBackground(new Color(200, 220, 255));
        helpBtn.addActionListener(e -> showHelp());

        toolbar.add(label);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(loadBtn);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(perspectiveCheck);
        toolbar.add(new JLabel(" d: "));
        toolbar.add(dField);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(resetBtn);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(helpBtn);

        add(toolbar, BorderLayout.NORTH);
    }

    private void initMainArea() {
        canvas = new DrawingPanel();
        canvas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                requestFocusInWindow();
            }
        });

        String[] columns = {"Действие", "X", "Y", "Z"};
        tableModel = new DefaultTableModel(columns, 0);

        logTable = new JTable(tableModel);
        logTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logTable.setRowHeight(25);

        JTableHeader header = logTable.getTableHeader();
        header.setFont(Theme.HEADER_FONT);
        header.setBackground(Theme.BUTTON_COLOR);
        header.setForeground(Theme.TEXT_COLOR);

        JScrollPane scrollPane = new JScrollPane(logTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.ACCENT_COLOR, 2),
                "Лог Трансформаций", TitledBorder.CENTER, TitledBorder.TOP, Theme.HEADER_FONT, Theme.ACCENT_COLOR
        ));
        scrollPane.setPreferredSize(new Dimension(350, 0));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvas, scrollPane);
        splitPane.setResizeWeight(0.75);
        splitPane.setDividerSize(5);
        add(splitPane, BorderLayout.CENTER);
    }

    private void handleKeyPress(KeyEvent e) {
        if (object3D.vertices.isEmpty()) return;

        Matrix4x4 m = Matrix4x4.identity();
        String action = "";
        String valX = "-";
        String valY = "-";
        String valZ = "-";

        double step = 10.0;
        double angle = 5.0;
        double scaleStep = 1.1;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> { m = Matrix4x4.translation(0, -step, 0); action = "Сдвиг Y-"; valY = String.valueOf(-step); }
            case KeyEvent.VK_S -> { m = Matrix4x4.translation(0, step, 0); action = "Сдвиг Y+"; valY = String.valueOf(step); }
            case KeyEvent.VK_A -> { m = Matrix4x4.translation(-step, 0, 0); action = "Сдвиг X-"; valX = String.valueOf(-step); }
            case KeyEvent.VK_D -> { m = Matrix4x4.translation(step, 0, 0); action = "Сдвиг X+"; valX = String.valueOf(step); }
            case KeyEvent.VK_Q -> { m = Matrix4x4.translation(0, 0, -step); action = "Сдвиг Z-"; valZ = String.valueOf(-step); }
            case KeyEvent.VK_E -> { m = Matrix4x4.translation(0, 0, step); action = "Сдвиг Z+"; valZ = String.valueOf(step); }

            case KeyEvent.VK_UP -> { m = Matrix4x4.rotationX(-angle); action = "Поворот X-"; valX = -angle + "°"; }
            case KeyEvent.VK_DOWN -> { m = Matrix4x4.rotationX(angle); action = "Поворот X+"; valX = angle + "°"; }
            case KeyEvent.VK_LEFT -> { m = Matrix4x4.rotationY(-angle); action = "Поворот Y-"; valY = -angle + "°"; }
            case KeyEvent.VK_RIGHT -> { m = Matrix4x4.rotationY(angle); action = "Поворот Y+"; valY = angle + "°"; }
            case KeyEvent.VK_PAGE_UP -> { m = Matrix4x4.rotationZ(-angle); action = "Поворот Z-"; valZ = -angle + "°"; }
            case KeyEvent.VK_PAGE_DOWN -> { m = Matrix4x4.rotationZ(angle); action = "Поворот Z+"; valZ = angle + "°"; }

            case KeyEvent.VK_ADD, KeyEvent.VK_EQUALS -> {
                m = Matrix4x4.scale(scaleStep, scaleStep, scaleStep);
                action = "Масштаб +";
                valX = "x" + scaleStep;
                valY = "x" + scaleStep;
                valZ = "x" + scaleStep;
            }
            case KeyEvent.VK_SUBTRACT, KeyEvent.VK_MINUS -> {
                m = Matrix4x4.scale(1/scaleStep, 1/scaleStep, 1/scaleStep);
                action = "Масштаб -";
                String scaleStr = String.format(java.util.Locale.US, "x%.2f", 1/scaleStep);
                valX = scaleStr;
                valY = scaleStr;
                valZ = scaleStr;
            }

            case KeyEvent.VK_X -> { m = Matrix4x4.reflection(true, false, false); action = "Отражение X"; valX = "-1"; }
            case KeyEvent.VK_Y -> { m = Matrix4x4.reflection(false, true, false); action = "Отражение Y"; valY = "-1"; }
            case KeyEvent.VK_Z -> { m = Matrix4x4.reflection(false, false, true); action = "Отражение Z"; valZ = "-1"; }
        }

        if (!action.isEmpty()) {
            object3D.applyTransform(m);
            tableModel.addRow(new Object[]{action, valX, valY, valZ});
            logTable.scrollRectToVisible(logTable.getCellRect(logTable.getRowCount() - 1, 0, true));
            drawObject();
        }
    }

    private void drawObject() {
        if (object3D.vertices.isEmpty()) return;

        List<Point3D> transformedVerts = new ArrayList<>();
        Matrix4x4 perspectiveMat = Matrix4x4.identity();

        if (perspectiveCheck.isSelected()) {
            try {
                double d = Double.parseDouble(dField.getText());
                perspectiveMat = Matrix4x4.perspective(d);
            } catch (Exception ignored) {}
        }

        Matrix4x4 finalMatrix = object3D.currentTransform.multiply(perspectiveMat);

        for (Point3D p : object3D.vertices) {
            Point3D tp = p.transform(finalMatrix).normalize();
            transformedVerts.add(tp);
        }

        List<Pixel> renderPixels = new ArrayList<>();
        BresenhamAlgorithm bresenham = new BresenhamAlgorithm();

        int cx = canvas.getWidth() / 2;
        int cy = canvas.getHeight() / 2;

        DefaultTableModel dummyModel = new DefaultTableModel();

        for (int[] edge : object3D.edges) {
            Point3D p1 = transformedVerts.get(edge[0]);
            Point3D p2 = transformedVerts.get(edge[1]);

            if (Math.abs(p1.x) > 10000 || Math.abs(p1.y) > 10000 ||
                    Math.abs(p2.x) > 10000 || Math.abs(p2.y) > 10000) {
                continue;
            }

            int x1 = (int) Math.round(p1.x) + cx;
            int y1 = cy - (int) Math.round(p1.y);
            int x2 = (int) Math.round(p2.x) + cx;
            int y2 = cy - (int) Math.round(p2.y);
            renderPixels.addAll(bresenham.calculate(x1, y1, x2, y2, dummyModel));
        }
        canvas.setPixels(renderPixels);
    }

    private void loadObjectFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            object3D = new Object3D();
            tableModel.setRowCount(0);

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("v ")) {
                        String[] parts = line.split("\\s+");
                        object3D.addVertex(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
                    } else if (line.startsWith("e ")) {
                        String[] parts = line.split("\\s+");
                        object3D.addEdge(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                    }
                }
                tableModel.addRow(new Object[]{"Файл загружен", object3D.vertices.size() + " верш.", object3D.edges.size() + " реб.", ""});
                drawObject();
                requestFocusInWindow();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка чтения файла: " + ex.getMessage());
            }
        }
    }

    private void showHelp() {
        String helpText = """
            <html><body style='width: 350px; font-family: Segoe UI;'>
            <h2 style='color: #DB7093;'>Управление объектом (Клавиатура):</h2>
            <p><b>Перемещение:</b> W, A, S, D, Q, E (Оси X, Y, Z)</p>
            <p><b>Вращение:</b> Стрелки (X, Y) и PageUp/PageDown (Z)</p>
            <p><b>Масштаб:</b> Кнопки '+' и '-'</p>
            <p><b>Отражение:</b> Кнопки 'X', 'Y', 'Z'</p>
            <hr>
            <h3>Формат файла (*.txt):</h3>
            <pre>
            v x y z  (Добавление вершины)
            e i j    (Ребро между вершинами, индекс с 0)
            </pre>
            <i>Пример простого куба:</i><br>
            v -50 -50 -50<br>
            v 50 -50 -50<br>
            v 50 50 -50<br>
            ...<br>
            e 0 1
            </body></html>
            """;
        JOptionPane.showMessageDialog(this, helpText, "Управление 3D", JOptionPane.INFORMATION_MESSAGE);
        requestFocusInWindow();
    }
}