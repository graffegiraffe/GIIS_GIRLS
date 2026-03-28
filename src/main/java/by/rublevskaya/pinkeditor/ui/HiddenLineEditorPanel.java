package by.rublevskaya.pinkeditor.ui;

import by.rublevskaya.pinkeditor.algorithms.lab8.CohenSutherlandAlgorithm;
import by.rublevskaya.pinkeditor.algorithms.lab8.RobertsAlgorithm;
import by.rublevskaya.pinkeditor.model.geom3d.Matrix4x4;
import by.rublevskaya.pinkeditor.model.geom3d.Point3D;
import by.rublevskaya.pinkeditor.ui.components.StyledButton;
import by.rublevskaya.pinkeditor.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HiddenLineEditorPanel extends JPanel {
    private Canvas2D canvas2D;
    private DefaultTableModel tableModel2D;
    private JLabel statusLabel2D;
    private JComboBox<String> modeSelector;

    private Canvas3D canvas3D;
    private DefaultTableModel tableModel3D;
    private final List<Point3D> vertices3D = new ArrayList<>();
    private final List<int[]>   edges3D    = new ArrayList<>();
    private final List<int[]>   faces3D    = new ArrayList<>();
    private Matrix4x4           transform3D = Matrix4x4.identity();
    private JCheckBox           showHiddenCheck;
    private JTextField          eyeXField, eyeYField, eyeZField;

    public HiddenLineEditorPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_COLOR);

        JTabbedPane innerTabs = new JTabbedPane();
        innerTabs.setFont(Theme.BOLD_FONT);
        innerTabs.setBackground(Color.WHITE);
        innerTabs.addTab("2D Отсечение (Коэн–Сазерленд)", create2DTab());
        innerTabs.addTab("3D Скрытые грани (Алгоритм Робертса)", create3DTab());

        add(innerTabs, BorderLayout.CENTER);
    }

    private JPanel create2DTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG_COLOR);

        JToolBar toolbar = new JToolBar();
        toolbar.setBackground(Color.WHITE);
        toolbar.setFloatable(false);
        toolbar.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Отсечение отрезков: ");
        label.setFont(Theme.HEADER_FONT);
        label.setForeground(Theme.ACCENT_COLOR);

        modeSelector = new JComboBox<>(new String[]{"Задать окно", "Нарисовать отрезок"});
        modeSelector.setFont(Theme.MAIN_FONT);
        modeSelector.setBackground(Color.WHITE);
        modeSelector.addActionListener(e -> updateStatus2D());

        JButton clipBtn = new StyledButton("Отсечь");
        clipBtn.setBackground(new Color(255, 228, 225));
        clipBtn.addActionListener(e -> performClipping());

        JButton deleteBtn = new StyledButton("Удалить последний");
        deleteBtn.setBackground(new Color(255, 235, 205));
        deleteBtn.addActionListener(e -> {
            canvas2D.removeLastSegment();
            updateStatus2D();
        });

        JButton clearBtn = new StyledButton("ОЧИСТИТЬ");
        clearBtn.setBackground(Theme.ACCENT_COLOR);
        clearBtn.setForeground(Color.WHITE);
        clearBtn.addActionListener(e -> clear2D());

        statusLabel2D = new JLabel("Режим 'Задать окно': тяните мышью для определения окна отсечения");
        statusLabel2D.setFont(Theme.SMALL_FONT);
        statusLabel2D.setForeground(Theme.TEXT_COLOR);

        toolbar.add(label);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(new JLabel("Режим: "));
        toolbar.add(modeSelector);
        toolbar.add(Box.createHorizontalStrut(15));
        toolbar.add(clipBtn);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(deleteBtn);
        toolbar.add(Box.createHorizontalStrut(15));
        toolbar.add(statusLabel2D);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(clearBtn);

        canvas2D = new Canvas2D();

        String[] cols = {"Отрезок №", "Код P1 (ВНПЛ)", "Код P2 (ВНПЛ)", "P1 AND P2", "Результат"};
        tableModel2D = new DefaultTableModel(cols, 0);
        JScrollPane sp = createScrollPane(createTable(tableModel2D), "Лог отсечения Коэна–Сазерленда");

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvas2D, sp);
        split.setResizeWeight(0.75);
        split.setDividerSize(5);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void performClipping() {
        tableModel2D.setRowCount(0);

        Rectangle win = canvas2D.getClipWindow();
        if (win == null) {
            JOptionPane.showMessageDialog(this,
                "Сначала задайте окно отсечения: перетащите мышью в режиме 'Задать окно'.",
                "Нет окна", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<int[]> segments = canvas2D.getSegments();
        if (segments.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Добавьте отрезки в режиме 'Нарисовать отрезок'.",
                "Нет отрезков", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        CohenSutherlandAlgorithm alg = new CohenSutherlandAlgorithm(
            win.x, win.y, win.x + win.width, win.y + win.height
        );

        List<CohenSutherlandAlgorithm.ClipResult> results = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            int[] seg = segments.get(i);
            CohenSutherlandAlgorithm.ClipResult r = alg.clip(seg[0], seg[1], seg[2], seg[3]);
            results.add(r);
            tableModel2D.addRow(new Object[]{
                i + 1,
                CohenSutherlandAlgorithm.outcodeString(r.outcode1),
                CohenSutherlandAlgorithm.outcodeString(r.outcode2),
                CohenSutherlandAlgorithm.outcodeString(r.outcode1 & r.outcode2),
                r.status
            });
        }

        canvas2D.setClipResults(results);
        statusLabel2D.setText("Отсечение выполнено. Зелёный — видимая часть.");
    }

    private void clear2D() {
        canvas2D.clear();
        tableModel2D.setRowCount(0);
        updateStatus2D();
    }

    private void updateStatus2D() {
        if (modeSelector.getSelectedIndex() == 0) {
            statusLabel2D.setText("Режим 'Задать окно': тяните мышью — задаёт окно отсечения");
        } else {
            int n = canvas2D.getSegments().size();
            statusLabel2D.setText(n == 0
                ? "Режим 'Отрезок': два клика = один отрезок"
                : "Отрезков: " + n + " | Нажмите 'Отсечь' для запуска алгоритма");
        }
    }

    private JPanel create3DTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG_COLOR);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        JToolBar toolbar = new JToolBar();
        toolbar.setBackground(Color.WHITE);
        toolbar.setFloatable(false);
        toolbar.setBorder(new EmptyBorder(8, 10, 6, 10));

        JLabel label = new JLabel("Алгоритм Робертса: ");
        label.setFont(Theme.HEADER_FONT);
        label.setForeground(Theme.ACCENT_COLOR);

        JButton loadBtn = new StyledButton("Загрузить из файла");
        loadBtn.setBackground(new Color(255, 228, 225));
        loadBtn.addActionListener(e -> loadObject3D());

        JButton demoBtn = new StyledButton("Загрузить куб (демо)");
        demoBtn.setBackground(new Color(220, 255, 215));
        demoBtn.setToolTipText("Загрузить пример — куб 100×100×100");
        demoBtn.addActionListener(e -> loadDemoCube());

        showHiddenCheck = new JCheckBox("Показать скрытые рёбра");
        showHiddenCheck.setFont(Theme.BOLD_FONT);
        showHiddenCheck.setBackground(Color.WHITE);
        showHiddenCheck.addActionListener(e -> updateVisibility());

        JButton resetBtn = new StyledButton("СБРОС");
        resetBtn.setBackground(Theme.BUTTON_COLOR);
        resetBtn.addActionListener(e -> {
            transform3D = Matrix4x4.identity();
            updateVisibility();
            canvas3D.requestFocusInWindow();
        });

        JButton deleteBtn = new StyledButton("Удалить объект");
        deleteBtn.setBackground(new Color(255, 235, 205));
        deleteBtn.addActionListener(e -> clear3D());

        JButton helpBtn = new StyledButton("Справка");
        helpBtn.setBackground(new Color(200, 220, 255));
        helpBtn.addActionListener(e -> showHelp3D());

        toolbar.add(label);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(loadBtn);
        toolbar.add(Box.createHorizontalStrut(6));
        toolbar.add(demoBtn);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(showHiddenCheck);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(resetBtn);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(deleteBtn);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(helpBtn);

        JPanel eyePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 5));
        eyePanel.setBackground(new Color(252, 248, 250));
        eyePanel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(230, 210, 220)));

        JLabel eyeTitle = new JLabel("  Точка зрения наблюдателя:");
        eyeTitle.setFont(Theme.BOLD_FONT);
        eyeTitle.setForeground(Theme.ACCENT_COLOR);
        eyeTitle.setToolTipText(
            "<html>Задаёт позицию наблюдателя в 3D-пространстве.<br>" +
            "Алгоритм Робертса определяет, какие грани обращены к наблюдателю.<br>" +
            "Пример: Z=500 — вид спереди; X=500 — вид справа.</html>");

        eyeXField = new JTextField("0", 6);
        eyeXField.setFont(Theme.MAIN_FONT);
        eyeXField.setToolTipText("X-координата наблюдателя");

        eyeYField = new JTextField("0", 6);
        eyeYField.setFont(Theme.MAIN_FONT);
        eyeYField.setToolTipText("Y-координата наблюдателя");

        eyeZField = new JTextField("500", 6);
        eyeZField.setFont(Theme.MAIN_FONT);
        eyeZField.setToolTipText("Z-координата наблюдателя (обычно большое положительное число)");

        JButton applyEyeBtn = new StyledButton("Применить");
        applyEyeBtn.setBackground(new Color(200, 220, 255));
        applyEyeBtn.setToolTipText("Пересчитать видимость граней с новой точки зрения");
        applyEyeBtn.addActionListener(e -> {
            updateVisibility();
            canvas3D.requestFocusInWindow();
        });

        JLabel hint = new JLabel("  — меняет какие грани видимы (не влияет на изображение)");
        hint.setFont(Theme.SMALL_FONT);
        hint.setForeground(new Color(140, 110, 125));

        eyePanel.add(eyeTitle);
        eyePanel.add(Box.createHorizontalStrut(8));
        eyePanel.add(new JLabel("X:"));
        eyePanel.add(eyeXField);
        eyePanel.add(new JLabel("  Y:"));
        eyePanel.add(eyeYField);
        eyePanel.add(new JLabel("  Z:"));
        eyePanel.add(eyeZField);
        eyePanel.add(Box.createHorizontalStrut(6));
        eyePanel.add(applyEyeBtn);
        eyePanel.add(hint);

        topPanel.add(toolbar, BorderLayout.NORTH);
        topPanel.add(eyePanel, BorderLayout.SOUTH);

        canvas3D = new Canvas3D();
        canvas3D.setFocusable(true);
        canvas3D.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { canvas3D.requestFocusInWindow(); }
        });
        canvas3D.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) { handleKey3D(e); }
        });

        String[] cols = {"Грань №", "Нормаль (×100)", "Скал. произв.", "Видимость"};
        tableModel3D = new DefaultTableModel(cols, 0);
        JScrollPane sp = createScrollPane(createTable(tableModel3D), "Видимость граней (Робертс)");

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvas3D, sp);
        split.setResizeWeight(0.75);
        split.setDividerSize(5);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void loadObject3D() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try (BufferedReader br = new BufferedReader(new FileReader(chooser.getSelectedFile()))) {
            parseAndLoad3D(br);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка чтения файла:\n" + ex.getMessage(),
                "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDemoCube() {
        InputStream is = getClass().getResourceAsStream("/cube_faces.txt");
        if (is != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                parseAndLoad3D(br);
                return;
            } catch (Exception ignored) {}
        }
        loadHardcodedCube();
    }

    private void loadHardcodedCube() {
        vertices3D.clear(); edges3D.clear(); faces3D.clear();
        transform3D = Matrix4x4.identity();
        tableModel3D.setRowCount(0);

        double s = 50;
        vertices3D.add(new Point3D(-s, -s, -s)); // 0
        vertices3D.add(new Point3D( s, -s, -s)); // 1
        vertices3D.add(new Point3D( s,  s, -s)); // 2
        vertices3D.add(new Point3D(-s,  s, -s)); // 3
        vertices3D.add(new Point3D(-s, -s,  s)); // 4
        vertices3D.add(new Point3D( s, -s,  s)); // 5
        vertices3D.add(new Point3D( s,  s,  s)); // 6
        vertices3D.add(new Point3D(-s,  s,  s)); // 7

        int[][] edgeArr = {
            {0,1},{1,2},{2,3},{3,0},
            {4,5},{5,6},{6,7},{7,4},
            {0,4},{1,5},{2,6},{3,7}
        };
        for (int[] e : edgeArr) edges3D.add(e);

        faces3D.add(new int[]{4,5,6,7}); // передняя (z=+50)
        faces3D.add(new int[]{3,2,1,0}); // задняя  (z=-50)
        faces3D.add(new int[]{1,2,6,5}); // правая
        faces3D.add(new int[]{4,7,3,0}); // левая
        faces3D.add(new int[]{7,6,2,3}); // верхняя
        faces3D.add(new int[]{0,1,5,4}); // нижняя

        updateVisibility();
        canvas3D.requestFocusInWindow();
    }

    private void parseAndLoad3D(BufferedReader br) throws Exception {
        vertices3D.clear(); edges3D.clear(); faces3D.clear();
        transform3D = Matrix4x4.identity();
        tableModel3D.setRowCount(0);

        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            if (line.startsWith("v ")) {
                String[] p = line.split("\\s+");
                vertices3D.add(new Point3D(
                    Double.parseDouble(p[1]),
                    Double.parseDouble(p[2]),
                    Double.parseDouble(p[3])));
            } else if (line.startsWith("e ")) {
                String[] p = line.split("\\s+");
                edges3D.add(new int[]{Integer.parseInt(p[1]), Integer.parseInt(p[2])});
            } else if (line.startsWith("f ")) {
                String[] p = line.split("\\s+");
                int[] fv = new int[p.length - 1];
                for (int i = 1; i < p.length; i++) fv[i - 1] = Integer.parseInt(p[i]);
                faces3D.add(fv);
            }
        }

        if (vertices3D.isEmpty()) throw new Exception("Нет вершин в файле.");
        updateVisibility();
        canvas3D.requestFocusInWindow();
    }

    private void updateVisibility() {
        if (vertices3D.isEmpty()) return;

        tableModel3D.setRowCount(0);

        double eyeX = parseDouble(eyeXField.getText(), 0);
        double eyeY = parseDouble(eyeYField.getText(), 0);
        double eyeZ = parseDouble(eyeZField.getText(), 500);

        List<Point3D> tverts = new ArrayList<>();
        for (Point3D p : vertices3D) {
            Point3D t = p.transform(transform3D);
            if (Math.abs(t.w) > 0.001 && t.w != 1.0) {
                t = new Point3D(t.x / t.w, t.y / t.w, t.z / t.w);
            }
            tverts.add(t);
        }

        List<RobertsAlgorithm.FaceResult> results;
        if (!faces3D.isEmpty()) {
            RobertsAlgorithm alg = new RobertsAlgorithm();
            results = alg.computeVisibility(tverts, faces3D, new Point3D(eyeX, eyeY, eyeZ), tableModel3D);
        } else {
            results = new ArrayList<>();
        }

        canvas3D.setData(tverts, edges3D, faces3D, results, showHiddenCheck.isSelected());
    }

    private void handleKey3D(KeyEvent e) {
        if (vertices3D.isEmpty()) return;
        double angle = 5.0;
        Matrix4x4 m = null;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP        -> m = Matrix4x4.rotationX(-angle);
            case KeyEvent.VK_DOWN      -> m = Matrix4x4.rotationX(angle);
            case KeyEvent.VK_LEFT      -> m = Matrix4x4.rotationY(-angle);
            case KeyEvent.VK_RIGHT     -> m = Matrix4x4.rotationY(angle);
            case KeyEvent.VK_PAGE_UP   -> m = Matrix4x4.rotationZ(-angle);
            case KeyEvent.VK_PAGE_DOWN -> m = Matrix4x4.rotationZ(angle);
        }
        if (m != null) {
            transform3D = transform3D.multiply(m);
            updateVisibility();
        }
    }

    private void clear3D() {
        vertices3D.clear(); edges3D.clear(); faces3D.clear();
        transform3D = Matrix4x4.identity();
        tableModel3D.setRowCount(0);
        canvas3D.setData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), false);
    }

    private void showHelp3D() {
        String html = """
            <html><body style='width:440px; font-family:Segoe UI; font-size:12px;'>
            <h2 style='color:#DB7093;'>Алгоритм Робертса — удаление невидимых граней</h2>
            <p>Для каждой грани вычисляется <b>внешняя нормаль</b> (векторное произведение двух рёбер).
            Если скалярное произведение нормали и вектора «грань→наблюдатель» <b>&gt; 0</b>,
            грань <b>видима</b>, иначе — скрыта.</p>

            <table border='1' cellpadding='5' style='border-collapse:collapse; width:100%;'>
              <tr style='background:#FFE0EB'><td><b>Розовая заливка</b></td><td>Видимая грань</td></tr>
              <tr><td><b>Серый контур (пунктир)</b></td><td>Скрытая грань (вкл. «Показать скрытые рёбра»)</td></tr>
              <tr style='background:#FFE0EB'><td><b>Таблица справа</b></td><td>Нормаль каждой грани и знак скалярного произведения</td></tr>
            </table>

            <h3>Для чего нужна точка зрения?</h3>
            <p>Задаёт <b>позицию наблюдателя</b> в 3D-пространстве.<br>
            Алгоритм проверяет: обращена ли нормаль грани к наблюдателю.<br>
            <b>Z=500</b> — смотрим спереди (+Z), видна передняя грань куба.<br>
            <b>Z=-500</b> — смотрим сзади, видна задняя грань.<br>
            <b>X=500</b> — смотрим справа, видна правая грань.<br>
            После изменения нажмите <b>«Применить»</b>.</p>

            <h3>Вращение объекта (клавиатура):</h3>
            <p><i>Сначала кликните мышью по холсту, чтобы передать ему фокус.</i></p>
            <table border='1' cellpadding='4' style='border-collapse:collapse;'>
              <tr><td>↑ / ↓</td><td>Вращение вокруг оси X</td></tr>
              <tr><td>← / →</td><td>Вращение вокруг оси Y</td></tr>
              <tr><td><b>Page Up / Page Down</b></td>
                  <td>Вращение вокруг оси Z<br>
                  <i>(«Стр.↑» и «Стр.↓» — клавиши над стрелками на клавиатуре)</i></td></tr>
            </table>

            <h3>Формат файла (*.txt):</h3>
            <pre style='background:#FFF0F5; padding:6px; font-size:11px;'>
v x y z          # вершина
e i j            # ребро (индексы вершин с 0)
f i j k ...      # грань (вершины против часовой стрелки снаружи)</pre>
            </body></html>""";
        JOptionPane.showMessageDialog(this, new JLabel(html),
            "Справка: Алгоритм Робертса", JOptionPane.INFORMATION_MESSAGE);
        canvas3D.requestFocusInWindow();
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(25);
        JTableHeader header = table.getTableHeader();
        header.setFont(Theme.HEADER_FONT);
        header.setBackground(Theme.BUTTON_COLOR);
        header.setForeground(Theme.TEXT_COLOR);
        return table;
    }

    private JScrollPane createScrollPane(JTable table, String title) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Theme.ACCENT_COLOR, 2),
            title, TitledBorder.CENTER, TitledBorder.TOP,
            Theme.HEADER_FONT, Theme.ACCENT_COLOR));
        sp.setPreferredSize(new Dimension(390, 0));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private double parseDouble(String s, double def) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return def; }
    }

    private class Canvas2D extends JPanel {

        private Rectangle clipWindow;
        private final List<int[]> segments = new ArrayList<>();
        private List<CohenSutherlandAlgorithm.ClipResult> clipResults = new ArrayList<>();

        private Point dragStart, dragEnd;
        private boolean dragging = false;
        private Point pendingFirst = null;

        Canvas2D() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 210, 220)));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (modeSelector.getSelectedIndex() == 0) {
                        dragStart = e.getPoint();
                        dragging = true;
                        clipResults.clear();
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (modeSelector.getSelectedIndex() == 0 && dragging) {
                        dragging = false;
                        if (dragStart != null && dragEnd != null) {
                            int x = Math.min(dragStart.x, dragEnd.x);
                            int y = Math.min(dragStart.y, dragEnd.y);
                            int w = Math.abs(dragEnd.x - dragStart.x);
                            int h = Math.abs(dragEnd.y - dragStart.y);
                            if (w > 10 && h > 10) {
                                clipWindow = new Rectangle(x, y, w, h);
                                segments.clear();
                                clipResults.clear();
                                statusLabel2D.setText(
                                    "Окно задано (" + w + "×" + h + "). Переключитесь в 'Нарисовать отрезок'");
                            }
                            dragEnd = null;
                        }
                        repaint();
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (modeSelector.getSelectedIndex() == 1) {
                        clipResults.clear();
                        if (pendingFirst == null) {
                            pendingFirst = e.getPoint();
                        } else {
                            segments.add(new int[]{
                                pendingFirst.x, pendingFirst.y,
                                e.getX(), e.getY()
                            });
                            pendingFirst = null;
                        }
                        updateStatus2D();
                        repaint();
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (modeSelector.getSelectedIndex() == 0 && dragging) {
                        dragEnd = e.getPoint();
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawGrid(g2);

            if (dragging && dragStart != null && dragEnd != null) {
                int x = Math.min(dragStart.x, dragEnd.x);
                int y = Math.min(dragStart.y, dragEnd.y);
                int w = Math.abs(dragEnd.x - dragStart.x);
                int h = Math.abs(dragEnd.y - dragStart.y);
                g2.setColor(new Color(100, 149, 237, 60));
                g2.fillRect(x, y, w, h);
                g2.setColor(new Color(65, 105, 225));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRect(x, y, w, h);
            }

            if (clipWindow != null) {
                g2.setColor(new Color(100, 149, 237, 35));
                g2.fillRect(clipWindow.x, clipWindow.y, clipWindow.width, clipWindow.height);
                g2.setColor(new Color(65, 105, 225));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRect(clipWindow.x, clipWindow.y, clipWindow.width, clipWindow.height);
                g2.setFont(Theme.SMALL_FONT);
                g2.setColor(new Color(65, 105, 225));
                g2.drawString("Окно отсечения", clipWindow.x + 4, clipWindow.y - 4);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.drawString("(" + clipWindow.x + "," + clipWindow.y + ")",
                    clipWindow.x + 2, clipWindow.y + 10);
                g2.drawString("(" + (clipWindow.x + clipWindow.width) + ","
                    + (clipWindow.y + clipWindow.height) + ")",
                    clipWindow.x + clipWindow.width - 70, clipWindow.y + clipWindow.height - 4);
            }

            if (clipResults.isEmpty()) {
                g2.setColor(new Color(100, 100, 120));
                g2.setStroke(new BasicStroke(2f));
                for (int i = 0; i < segments.size(); i++) {
                    int[] s = segments.get(i);
                    g2.drawLine(s[0], s[1], s[2], s[3]);
                    drawEndpoint(g2, s[0], s[1]);
                    drawEndpoint(g2, s[2], s[3]);
                    drawSegmentLabel(g2, s, i + 1);
                }
            }

            if (!clipResults.isEmpty()) {
                Stroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 1f, new float[]{8f, 4f}, 0f);
                for (int i = 0; i < segments.size() && i < clipResults.size(); i++) {
                    int[] s = segments.get(i);
                    CohenSutherlandAlgorithm.ClipResult r = clipResults.get(i);
                    g2.setStroke(dashed);
                    g2.setColor(new Color(180, 160, 170));
                    g2.drawLine(s[0], s[1], s[2], s[3]);
                    if (r.visible) {
                        g2.setStroke(new BasicStroke(3f));
                        g2.setColor(new Color(34, 160, 60));
                        g2.drawLine((int)r.x1, (int)r.y1, (int)r.x2, (int)r.y2);
                        g2.fillOval((int)r.x1 - 5, (int)r.y1 - 5, 10, 10);
                        g2.fillOval((int)r.x2 - 5, (int)r.y2 - 5, 10, 10);
                    }
                    drawSegmentLabel(g2, s, i + 1);
                }
            }

            if (pendingFirst != null) {
                g2.setColor(Theme.ACCENT_COLOR);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(pendingFirst.x - 6, pendingFirst.y - 6, 12, 12);
                g2.fillOval(pendingFirst.x - 4, pendingFirst.y - 4, 8, 8);
                g2.setFont(Theme.SMALL_FONT);
                g2.drawString("P1", pendingFirst.x + 8, pendingFirst.y - 4);
            }

            drawLegend2D(g2);
        }

        private void drawGrid(Graphics2D g2) {
            g2.setColor(new Color(240, 230, 235));
            g2.setStroke(new BasicStroke(0.5f));
            for (int x = 0; x < getWidth(); x += 30)  g2.drawLine(x, 0, x, getHeight());
            for (int y = 0; y < getHeight(); y += 30) g2.drawLine(0, y, getWidth(), y);
        }

        private void drawEndpoint(Graphics2D g2, int x, int y) {
            g2.setColor(new Color(100, 100, 120));
            g2.fillOval(x - 4, y - 4, 8, 8);
        }

        private void drawSegmentLabel(Graphics2D g2, int[] s, int idx) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(Theme.ACCENT_COLOR);
            g2.drawString(String.valueOf(idx), (s[0] + s[2]) / 2 + 4, (s[1] + s[3]) / 2 - 4);
        }

        private void drawLegend2D(Graphics2D g2) {
            int lx = 12, ly = getHeight() - 88;
            g2.setColor(new Color(255, 255, 255, 210));
            g2.fillRoundRect(lx - 4, ly - 18, 230, 80, 10, 10);
            g2.setColor(Theme.ACCENT_COLOR);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(lx - 4, ly - 18, 230, 80, 10, 10);
            g2.setFont(Theme.SMALL_FONT);

            g2.setColor(new Color(65, 105, 225));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRect(lx, ly, 18, 11);
            g2.setColor(Theme.TEXT_COLOR);
            g2.drawString("Окно отсечения", lx + 26, ly + 10);

            Stroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1f, new float[]{8f, 4f}, 0f);
            g2.setStroke(dashed);
            g2.setColor(new Color(180, 160, 170));
            g2.drawLine(lx, ly + 27, lx + 18, ly + 27);
            g2.setColor(Theme.TEXT_COLOR);
            g2.drawString("Исходный отрезок", lx + 26, ly + 31);

            g2.setStroke(new BasicStroke(3f));
            g2.setColor(new Color(34, 160, 60));
            g2.drawLine(lx, ly + 50, lx + 18, ly + 50);
            g2.setColor(Theme.TEXT_COLOR);
            g2.drawString("Видимая часть", lx + 26, ly + 54);
        }

        Rectangle getClipWindow() { return clipWindow; }
        List<int[]> getSegments() { return segments; }

        void setClipResults(List<CohenSutherlandAlgorithm.ClipResult> r) {
            clipResults = r;
            repaint();
        }

        void removeLastSegment() {
            if (!segments.isEmpty()) {
                segments.remove(segments.size() - 1);
                clipResults.clear();
                tableModel2D.setRowCount(0);
                repaint();
            }
        }

        void clear() {
            clipWindow = null;
            segments.clear();
            clipResults.clear();
            pendingFirst = null;
            dragging = false;
            dragStart = dragEnd = null;
            repaint();
        }
    }

    private static class Canvas3D extends JPanel {

        private List<Point3D> vertices   = new ArrayList<>();
        private List<int[]>   edges      = new ArrayList<>();
        private List<int[]>   faces      = new ArrayList<>();
        private List<RobertsAlgorithm.FaceResult> faceResults = new ArrayList<>();
        private boolean showHidden = false;
        private static final double DISPLAY_SCALE = 3.5;
        private static final double ISO_X = 0.50;
        private static final double ISO_Y = 0.25;

        Canvas3D() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 210, 220)));
        }

        void setData(List<Point3D> v, List<int[]> e, List<int[]> f,
                     List<RobertsAlgorithm.FaceResult> r, boolean showHidden) {
            this.vertices = v; this.edges = e; this.faces = f; this.faceResults = r;
            this.showHidden = showHidden;
            repaint();
        }

        private int[] project(Point3D p, int cx, int cy) {
            int sx = cx + (int) Math.round((p.x + p.z * ISO_X) * DISPLAY_SCALE);
            int sy = cy - (int) Math.round((p.y + p.z * ISO_Y) * DISPLAY_SCALE);
            return new int[]{sx, sy};
        }

        private double avgZ(int[] face) {
            double sum = 0; int cnt = 0;
            for (int vi : face) {
                if (vi < vertices.size()) { sum += vertices.get(vi).z; cnt++; }
            }
            return cnt == 0 ? 0 : sum / cnt;
        }

        private Polygon buildPolygon(int[] face, int cx, int cy) {
            Polygon poly = new Polygon();
            for (int vi : face) {
                if (vi < vertices.size()) {
                    int[] s = project(vertices.get(vi), cx, cy);
                    poly.addPoint(s[0], s[1]);
                }
            }
            return poly;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(240, 230, 235));
            g2.setStroke(new BasicStroke(0.5f));
            for (int x = 0; x < getWidth(); x += 30)  g2.drawLine(x, 0, x, getHeight());
            for (int y = 0; y < getHeight(); y += 30) g2.drawLine(0, y, getWidth(), y);

            if (vertices.isEmpty()) {
                g2.setColor(new Color(190, 160, 175));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String msg = "Нажмите «Загрузить куб (демо)» или «Загрузить из файла»";
                int sw = g2.getFontMetrics().stringWidth(msg);
                g2.drawString(msg, Math.max(10, (getWidth() - sw) / 2), getHeight() / 2);
                return;
            }

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            List<Integer> faceOrder = new ArrayList<>();
            for (int i = 0; i < faces.size(); i++) faceOrder.add(i);
            faceOrder.sort(Comparator.comparingDouble(i -> avgZ(faces.get((int) i))));

            if (showHidden) {
                Stroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 1f, new float[]{6f, 4f}, 0f);
                for (int idx : faceOrder) {
                    if (idx < faceResults.size() && !faceResults.get(idx).visible) {
                        Polygon poly = buildPolygon(faces.get(idx), cx, cy);
                        g2.setColor(new Color(210, 195, 205, 60));
                        g2.fillPolygon(poly);
                        g2.setStroke(dashed);
                        g2.setColor(new Color(155, 135, 145, 190));
                        g2.drawPolygon(poly);
                    }
                }
            }

            for (int idx : faceOrder) {
                if (idx < faceResults.size() && faceResults.get(idx).visible) {
                    Polygon poly = buildPolygon(faces.get(idx), cx, cy);
                    g2.setColor(new Color(255, 170, 193, 230));
                    g2.fillPolygon(poly);
                    g2.setStroke(new BasicStroke(2f));
                    g2.setColor(Theme.ACCENT_COLOR);
                    g2.drawPolygon(poly);
                }
            }

            g2.setColor(new Color(100, 55, 70, 210));
            g2.setStroke(new BasicStroke(1.3f));
            for (int[] edge : edges) {
                if (edge[0] < vertices.size() && edge[1] < vertices.size()) {
                    int[] p1 = project(vertices.get(edge[0]), cx, cy);
                    int[] p2 = project(vertices.get(edge[1]), cx, cy);
                    g2.drawLine(p1[0], p1[1], p2[0], p2[1]);
                }
            }

            g2.setColor(Theme.ACCENT_COLOR);
            for (Point3D p : vertices) {
                int[] s = project(p, cx, cy);
                g2.fillOval(s[0] - 3, s[1] - 3, 6, 6);
            }

            drawLegend3D(g2);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(new Color(120, 90, 105));
            g2.drawString(
                "↑↓ — ось X  |  ←→ — ось Y  |  Page Up / Page Down — ось Z" +
                "  |  Кликните холст для захвата клавиатуры",
                10, getHeight() - 7);
        }

        private void drawLegend3D(Graphics2D g2) {
            int lx = 12, ly = getHeight() - (showHidden ? 104 : 76);
            int boxH = showHidden ? 100 : 68;
            g2.setColor(new Color(255, 255, 255, 215));
            g2.fillRoundRect(lx - 4, ly - 18, 215, boxH, 10, 10);
            g2.setColor(Theme.ACCENT_COLOR);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(lx - 4, ly - 18, 215, boxH, 10, 10);
            g2.setFont(Theme.SMALL_FONT);
            g2.setColor(new Color(255, 170, 193, 230));
            g2.fillRect(lx, ly, 18, 12);
            g2.setColor(Theme.ACCENT_COLOR);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRect(lx, ly, 18, 12);
            g2.setColor(Theme.TEXT_COLOR);
            g2.drawString("Видимая грань (нормаль → наблюдатель)", lx + 26, ly + 10);

            if (showHidden) {
                Stroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 1f, new float[]{5f, 4f}, 0f);
                g2.setColor(new Color(210, 195, 205, 60));
                g2.fillRect(lx, ly + 22, 18, 12);
                g2.setStroke(dashed);
                g2.setColor(new Color(155, 135, 145));
                g2.drawRect(lx, ly + 22, 18, 12);
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(Theme.TEXT_COLOR);
                g2.drawString("Скрытая грань (нормаль от наблюдателя)", lx + 26, ly + 32);

                g2.setColor(new Color(100, 55, 70, 210));
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawLine(lx, ly + 54, lx + 18, ly + 54);
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(Theme.TEXT_COLOR);
                g2.drawString("Рёбра каркаса", lx + 26, ly + 58);
            } else {
                g2.setColor(new Color(100, 55, 70, 210));
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawLine(lx, ly + 28, lx + 18, ly + 28);
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(Theme.TEXT_COLOR);
                g2.drawString("Рёбра каркаса", lx + 26, ly + 32);
            }
        }
    }
}