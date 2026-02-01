package by.rublevskaya.pinkeditor.ui;

import by.rublevskaya.pinkeditor.util.Theme;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Pink Graphic Editor | Labs 1-2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 850);
        setLocationRelativeTo(null);

        setupGlobalUI();
        initMenu();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(Theme.HEADER_FONT);
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("Отрезки (Lab 1)", new LineEditorPanel());
        tabbedPane.addTab("Кривые (Lab 2)", new CurveEditorPanel());

        add(tabbedPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void setupGlobalUI() {
        UIManager.put("Panel.background", Theme.BG_COLOR);
        UIManager.put("CheckBox.background", Theme.BG_COLOR);
        UIManager.put("SplitPane.background", Theme.BG_COLOR);
        UIManager.put("OptionPane.background", Theme.BG_COLOR);
        UIManager.put("Button.font", Theme.BOLD_FONT);
        UIManager.put("Label.font", Theme.MAIN_FONT);
        UIManager.put("TabbedPane.selected", Theme.BUTTON_COLOR);
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
}
