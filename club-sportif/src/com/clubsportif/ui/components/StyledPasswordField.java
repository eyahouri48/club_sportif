package com.clubsportif.ui.components;

import com.clubsportif.util.Theme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StyledPasswordField extends JPasswordField {

    private final String placeholder;
    private boolean focused = false;

    public StyledPasswordField(String placeholder) {
        this.placeholder = placeholder;
        setFont(Theme.FONT_BODY);
        setForeground(Theme.TEXT_PRIMARY);
        setCaretColor(Theme.PRIMARY);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        setPreferredSize(new Dimension(250, Theme.FIELD_HEIGHT));

        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                focused = true;
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.PRIMARY, 2, true),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)));
            }
            @Override public void focusLost(FocusEvent e) {
                focused = false;
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getPassword().length == 0 && !focused) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(Theme.TEXT_MUTED);
            g2.setFont(getFont());
            Insets ins = getInsets();
            g2.drawString(placeholder, ins.left, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
            g2.dispose();
        }
    }
}
