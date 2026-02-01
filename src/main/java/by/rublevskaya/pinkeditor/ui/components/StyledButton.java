package by.rublevskaya.pinkeditor.ui.components;

import by.rublevskaya.pinkeditor.util.Theme;

import javax.swing.*;
import java.awt.*;

public class StyledButton extends JButton {
    public StyledButton(String text) {
        super(text);
        setBackground(Theme.BUTTON_COLOR);
        setForeground(Theme.TEXT_COLOR);
        setFocusPainted(false);
        setFont(Theme.BOLD_FONT);
        setContentAreaFilled(true);
        setOpaque(true);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.ACCENT_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
