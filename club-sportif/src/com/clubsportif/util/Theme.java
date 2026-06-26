package com.clubsportif.util;

import java.awt.*;

/**
 * Palette "Pour La Forme" — Bleu marine, beige, blanc.
 */
public final class Theme {

    private Theme() {}

    // ── Bleu marine (principal) ──────────────────────────────────
    public static final Color PRIMARY         = new Color(27, 42, 74);
    public static final Color PRIMARY_DARK    = new Color(15, 25, 50);
    public static final Color PRIMARY_LIGHT   = new Color(220, 228, 240);
    public static final Color PRIMARY_HOVER   = new Color(35, 55, 95);

    // ── Beige / Crème (accent chaud) ─────────────────────────────
    public static final Color BEIGE           = new Color(245, 237, 220);  // Warm beige
    public static final Color BEIGE_DARK      = new Color(210, 195, 170);
    public static final Color BEIGE_LIGHT     = new Color(252, 249, 243);  // Near-white warm

    // ── Bleu sportif (accent secondaire) ─────────────────────────
    public static final Color ACCENT          = new Color(59, 130, 246);
    public static final Color ACCENT_HOVER    = new Color(37, 99, 235);
    public static final Color ACCENT_LIGHT    = new Color(219, 234, 254);

    // ── Statut ───────────────────────────────────────────────────
    public static final Color SUCCESS         = new Color(5, 150, 105);
    public static final Color SUCCESS_LIGHT   = new Color(209, 250, 229);
    public static final Color WARNING         = new Color(217, 119, 6);
    public static final Color WARNING_LIGHT   = new Color(254, 243, 199);
    public static final Color DANGER          = new Color(220, 38, 38);
    public static final Color DANGER_LIGHT    = new Color(254, 226, 226);

    // ── Sidebar ──────────────────────────────────────────────────
    public static final Color BG_SIDEBAR      = new Color(15, 23, 42);
    public static final Color BG_SIDEBAR_DARK = new Color(2, 6, 23);
    public static final Color BG_SIDEBAR_ITEM = new Color(30, 41, 59);
    public static final Color SIDEBAR_TEXT    = new Color(148, 163, 184);
    public static final Color SIDEBAR_ACTIVE  = new Color(96, 165, 250);

    // ── Fonds & neutres ──────────────────────────────────────────
    public static final Color BG_MAIN         = new Color(250, 248, 244);  // Warm off-white
    public static final Color BG_CARD         = Color.WHITE;
    public static final Color TEXT_PRIMARY    = new Color(15, 23, 42);
    public static final Color TEXT_SECONDARY  = new Color(71, 85, 105);
    public static final Color TEXT_LIGHT      = new Color(203, 213, 225);
    public static final Color TEXT_MUTED      = new Color(148, 163, 184);
    public static final Color BORDER          = new Color(226, 232, 240);
    public static final Color SHADOW          = new Color(0, 0, 0, 20);

    // ── Polices ──────────────────────────────────────────────────
    public static final Font FONT_TITLE       = new Font("Segoe UI", Font.BOLD,   22);
    public static final Font FONT_SUBTITLE    = new Font("Segoe UI", Font.BOLD,   16);
    public static final Font FONT_LOGO        = new Font("Segoe UI", Font.BOLD,   18);
    public static final Font FONT_BODY        = new Font("Segoe UI", Font.PLAIN,  14);
    public static final Font FONT_BODY_BOLD   = new Font("Segoe UI", Font.BOLD,   14);
    public static final Font FONT_SMALL       = new Font("Segoe UI", Font.PLAIN,  12);
    public static final Font FONT_SMALL_BOLD  = new Font("Segoe UI", Font.BOLD,   12);
    public static final Font FONT_BUTTON      = new Font("Segoe UI", Font.BOLD,   13);
    public static final Font FONT_SIDEBAR     = new Font("Segoe UI", Font.PLAIN,  13);
    public static final Font FONT_SIDEBAR_ACT = new Font("Segoe UI", Font.BOLD,   13);
    public static final Font FONT_CAPTION     = new Font("Segoe UI", Font.PLAIN,  11);

    // ── Dimensions ───────────────────────────────────────────────
    public static final int SIDEBAR_WIDTH      = 248;
    public static final int SIDEBAR_COLLAPSED  = 64;
    public static final int FIELD_HEIGHT       = 40;
    public static final int BUTTON_HEIGHT      = 38;
    public static final Dimension BUTTON_SIZE  = new Dimension(150, BUTTON_HEIGHT);
    public static final int BORDER_RADIUS      = 8;
    public static final int CARD_RADIUS        = 12;
    public static final int PADDING            = 20;

    public static final Insets CARD_INSETS  = new Insets(20, 24, 20, 24);
    public static final Insets FIELD_INSETS = new Insets(8, 12, 8, 12);

    public static GradientPaint logoGradient(int x, int y, int w, int h) {
        return new GradientPaint(x, y, PRIMARY, x + w, y + h, ACCENT);
    }
    public static GradientPaint sidebarGradient(int h) {
        return new GradientPaint(0, 0, BG_SIDEBAR, 0, h, BG_SIDEBAR_DARK);
    }
}
