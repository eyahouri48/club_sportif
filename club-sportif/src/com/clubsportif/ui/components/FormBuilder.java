package com.clubsportif.ui.components;

import com.clubsportif.util.Theme;
import javax.swing.*;
import java.awt.*;

/**
 * Constructeur de formulaires en grille label/champ.
 */
public class FormBuilder {

    private final JPanel panel;

    public FormBuilder() {
        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
    }

    public FormBuilder addField(String label, JComponent field) {
        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx = 0; lc.gridy = GridBagConstraints.RELATIVE;
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(6, 2, 2, 12);
        lc.fill = GridBagConstraints.NONE;

        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL_BOLD);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        panel.add(lbl, lc);

        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = 1; fc.gridy = GridBagConstraints.RELATIVE;
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets = new Insets(6, 0, 2, 2);
        panel.add(field, fc);

        return this;
    }

    public FormBuilder addSeparator() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridwidth = 2;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 0, 8, 0);
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        panel.add(sep, gc);
        return this;
    }

    public JPanel build() {
        // No vertical glue — let the scroll pane handle overflow
        return panel;
    }
}
