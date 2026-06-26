package com.clubsportif.ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * JPanel scrollable qui utilise la largeur du viewport pour le WrapLayout.
 * Resout le probleme des cartes non-scrollables dans un JScrollPane.
 */
public class ScrollableWrapPanel extends JPanel implements Scrollable {

    public ScrollableWrapPanel(LayoutManager lm) {
        super(lm);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 30;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? visibleRect.height - 30 : visibleRect.width - 30;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true; // Critical: stretch to viewport width so WrapLayout wraps properly
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false; // Allow vertical scrolling
    }
}
