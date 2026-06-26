package com.clubsportif.ui.components;

import com.clubsportif.util.Theme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Bouton stylé avec animations hover — palette turquoise "Pour La Forme".
 */
public class StyledButton extends JButton {

    public enum Style { PRIMARY, SUCCESS, DANGER, OUTLINE, GHOST }

    private final Style style;
    private boolean hovering = false;
    private float hoverAlpha = 0f;
    private Timer hoverTimer;

    public StyledButton(String text) { this(text, Style.PRIMARY); }

    public StyledButton(String text, Style style) {
        super(text);
        this.style = style;
        setFont(Theme.FONT_BUTTON);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(
            Math.max(getFontMetrics(getFont()).stringWidth(text) + 36, 120),
            Theme.BUTTON_HEIGHT));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                hovering = true;
                animateHover(1f);
            }
            @Override public void mouseExited(MouseEvent e) {
                hovering = false;
                animateHover(0f);
            }
        });
    }

    private void animateHover(float target) {
        if (hoverTimer != null) hoverTimer.stop();
        hoverTimer = new Timer(16, e -> {
            hoverAlpha += (target - hoverAlpha) * 0.2f;
            if (Math.abs(hoverAlpha - target) < 0.02f) {
                hoverAlpha = target;
                hoverTimer.stop();
            }
            repaint();
        });
        hoverTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight(), r = 10;

        Color bg, fg, hoverBg;
        switch (style) {
            case SUCCESS -> { bg = Theme.SUCCESS; fg = Color.WHITE; hoverBg = new Color(4, 120, 87); }
            case DANGER  -> { bg = Theme.DANGER;  fg = Color.WHITE; hoverBg = new Color(185, 28, 28); }
            case OUTLINE -> {
                // Fond légèrement teinté + bordure marquée → bouton visible AU REPOS
                // (corrige #6 : boutons qui n'apparaissaient qu'au survol sur fond blanc)
                bg = new Color(238, 242, 248); fg = Theme.PRIMARY; hoverBg = new Color(219, 234, 254);
                g2.setColor(isEnabled() ? Theme.PRIMARY : new Color(200, 200, 200));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, r, r);
            }
            case GHOST   -> { bg = new Color(0,0,0,0); fg = Theme.PRIMARY; hoverBg = Theme.PRIMARY_LIGHT; }
            default      -> { bg = Theme.PRIMARY; fg = Color.WHITE; hoverBg = Theme.PRIMARY_HOVER; }
        }

        // Background with hover interpolation
        Color paintBg = interpolate(bg, hoverBg, hoverAlpha);
        g2.setColor(paintBg);
        g2.fillRoundRect(0, 0, w - 1, h - 1, r, r);

        // Text
        g2.setFont(getFont());
        g2.setColor(isEnabled() ? fg : Theme.TEXT_MUTED);
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(getText())) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), tx, ty);

        g2.dispose();
    }

    private Color interpolate(Color a, Color b, float t) {
        return new Color(
            Math.round(a.getRed()   + (b.getRed()   - a.getRed())   * t),
            Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            Math.round(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t),
            Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t)
        );
    }
}
