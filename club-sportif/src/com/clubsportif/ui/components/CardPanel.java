package com.clubsportif.ui.components;

import com.clubsportif.util.Theme;
import javax.swing.*;
import java.awt.*;

/**
 * Panneau carte moderne — fond blanc, ombre subtile, coins arrondis.
 * v3.0 : ombre plus douce, border fine propre
 */
public class CardPanel extends JPanel {

    private boolean elevated = false;
    private Color accentBorderColor = null;

    public CardPanel() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(
                Theme.CARD_INSETS.top, Theme.CARD_INSETS.left,
                Theme.CARD_INSETS.bottom, Theme.CARD_INSETS.right));
    }

    public CardPanel(boolean elevated) {
        this();
        this.elevated = elevated;
    }

    public void setAccentBorder(Color c) {
        this.accentBorderColor = c;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int r = Theme.CARD_RADIUS;

        // Ombre portée moderne (offset + flou simulé)
        int sh = elevated ? 4 : 2;
        for (int i = sh; i >= 1; i--) {
            int alpha = elevated ? (12 - i) : (8 - i);
            g2.setColor(new Color(15, 23, 42, Math.max(3, alpha)));
            g2.fillRoundRect(i, i + sh/2, w - i, h - i + sh/2, r + 2, r + 2);
        }

        // Fond blanc
        g2.setColor(Theme.BG_CARD);
        g2.fillRoundRect(0, 0, w - sh, h - sh, r, r);

        // Bordure d'accent (barre gauche colorée)
        if (accentBorderColor != null) {
            g2.setColor(accentBorderColor);
            g2.fillRoundRect(0, 8, 3, h - sh - 16, 3, 3);
        }

        // Bordure légère
        g2.setColor(new Color(226, 232, 240, 180));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, w - sh - 1, h - sh - 1, r, r);

        g2.dispose();
        super.paintComponent(g);
    }
}
