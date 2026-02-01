package by.rublevskaya.pinkeditor;

import by.rublevskaya.pinkeditor.ui.MainFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
