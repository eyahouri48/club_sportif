package com.clubsportif.ui.components;

import com.clubsportif.model.Activite;
import com.clubsportif.util.PhotoUtils;
import com.clubsportif.util.Theme;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carte activité — images nettes, palette turquoise "Pour La Forme".
 * Images pre-scalees exactement a la taille d'affichage.
 */
public class ActivityCard extends JPanel {

    private static final int CARD_W = 300;
    private static final int CARD_H = 340;
    private static final int IMG_H  = 180;
    private static final int RADIUS = 16;

    private final Activite activite;
    private final int placesRestantes;
    private boolean selected = false;
    private boolean hovering = false;
    private BufferedImage cardImage;

    // Lettres iconiques par sport (au lieu d'emojis)
    private static final Map<String, String> SPORT_LETTER = new LinkedHashMap<>();
    static {
        SPORT_LETTER.put("musculation","M"); SPORT_LETTER.put("crossfit","CF");
        SPORT_LETTER.put("yoga","Y");        SPORT_LETTER.put("natation","N");
        SPORT_LETTER.put("boxe","B");        SPORT_LETTER.put("football","F");
        SPORT_LETTER.put("basket","BK");     SPORT_LETTER.put("tennis","T");
        SPORT_LETTER.put("zumba","Z");       SPORT_LETTER.put("pilates","PL");
        SPORT_LETTER.put("running","R");     SPORT_LETTER.put("arts","AM");
    }

    public ActivityCard(Activite activite, int placesRestantes) {
        this.activite        = activite;
        this.placesRestantes = placesRestantes;
        setOpaque(false);
        setPreferredSize(new Dimension(CARD_W, CARD_H));
        setMinimumSize(new Dimension(CARD_W, CARD_H));
        setMaximumSize(new Dimension(CARD_W, CARD_H));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        new SwingWorker<BufferedImage, Void>() {
            @Override protected BufferedImage doInBackground() {
                // On n'utilise QUE la photo fournie par l'utilisateur. Aucune image
                // n'est téléchargée ni générée automatiquement (#9).
                if (activite.getPhoto() != null && activite.getPhoto().length > 0) {
                    try {
                        BufferedImage orig = ImageIO.read(new ByteArrayInputStream(activite.getPhoto()));
                        if (orig != null) return PhotoUtils.scaleHighQuality(orig, CARD_W - 4, IMG_H);
                    } catch (Exception ignored) {}
                }
                return null; // → placeholder gris dessiné dans paintComponent
            }
            @Override protected void done() {
                try { cardImage = get(); repaint(); } catch (Exception ignored) {}
            }
        }.execute();

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { hovering=true;  repaint(); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { hovering=false; repaint(); }
        });
    }

    private static String normalize(String s) {
        return s.toLowerCase()
            .replace("é","e").replace("è","e").replace("ê","e")
            .replace("à","a").replace("â","a").replace("ç","c")
            .replace("ü","u").replace("û","u").replace("ù","u")
            .replace("î","i").replace("ô","o");
    }

    public void setSelected(boolean s) { selected = s; repaint(); }
    public Activite getActivite()      { return activite; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        // Clear background (fixes scroll artifacts)
        g2.setColor(getParent() != null ? getParent().getBackground() : Theme.BG_MAIN);
        g2.fillRect(0, 0, getWidth(), getHeight());

        int w = CARD_W - 4, h = CARD_H - 6;

        // Shadow
        for (int i = 6; i >= 1; i--) {
            int alpha = hovering ? 22 - i*3 : 12 - i*2;
            g2.setColor(new Color(0,0,0,Math.max(2, alpha)));
            g2.fillRoundRect(i+1, i+3, w-i+2, h-i+4, RADIUS, RADIUS);
        }

        // White background
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, w, h, RADIUS, RADIUS);

        // Image
        Shape savedClip = g2.getClip();
        g2.setClip(new RoundRectangle2D.Float(0, 0, w, IMG_H + RADIUS, RADIUS, RADIUS));
        g2.clipRect(0, 0, w, IMG_H);

        if (cardImage != null) {
            g2.drawImage(cardImage, 0, 0, null);
        } else {
            // Placeholder neutre gris (aucune image fournie) — pas d'image auto-générée
            g2.setColor(new Color(237, 240, 245));
            g2.fillRect(0, 0, w, IMG_H);
            // Petite icône "image" stylisée au centre
            int cx = w / 2, cy = IMG_H / 2;
            g2.setColor(new Color(203, 211, 222));
            g2.fillRoundRect(cx - 26, cy - 20, 52, 40, 8, 8);
            g2.setColor(new Color(237, 240, 245));
            g2.fillOval(cx - 14, cy - 12, 10, 10);
            int[] xs = {cx - 22, cx - 6, cx + 6, cx + 22};
            int[] ys = {cy + 16, cy - 2, cy + 8, cy - 10};
            g2.fillPolygon(new int[]{xs[0], xs[1], xs[2], xs[3], cx + 22, cx - 22},
                           new int[]{ys[0], ys[1], ys[2], ys[3], cy + 16, cy + 16}, 6);
            g2.setColor(new Color(160, 170, 184));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            String ph = "Aucune image";
            FontMetrics fmp = g2.getFontMetrics();
            g2.drawString(ph, cx - fmp.stringWidth(ph) / 2, cy + 34);
        }

        // Gradient overlay
        g2.setPaint(new GradientPaint(0, IMG_H-60, new Color(0,0,0,0), 0, IMG_H, new Color(0,0,0,140)));
        g2.fillRect(0, IMG_H-60, w, IMG_H);
        g2.setClip(savedClip);

        // Badge places
        boolean complet = (placesRestantes == 0);
        Color badgeColor = complet ? new Color(220,38,38,230) : new Color(5,150,105,215);
        int bw = 90, bh = 26;
        g2.setColor(badgeColor);
        g2.fillRoundRect(w - bw - 10, 10, bw, bh, 13, 13);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.setColor(Color.WHITE);
        String badge = complet ? "Complet" : placesRestantes + " places";
        FontMetrics fmb = g2.getFontMetrics();
        g2.drawString(badge, w - bw - 10 + (bw - fmb.stringWidth(badge))/2, 10 + bh/2 + 4);

        // Sport letter icon (bottom left of image)
        String letter = "S";
        String norm = normalize(activite.getNom());
        for (Map.Entry<String,String> e : SPORT_LETTER.entrySet())
            if (norm.contains(e.getKey())) { letter = e.getValue(); break; }
        g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g2.setColor(new Color(255,255,255,200));
        g2.drawString(letter, 12, IMG_H - 10);

        // Text zone
        int ty = IMG_H + 18;
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        g2.setColor(Theme.TEXT_PRIMARY);
        g2.drawString(truncate(activite.getNom(), 26), 14, ty);
        ty += 18;

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2.setColor(Theme.TEXT_SECONDARY);
        g2.drawString(truncate(activite.getDescription() != null ? activite.getDescription() : "", 42), 14, ty);
        ty += 18;

        // Day + time with icons
        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.setColor(Theme.PRIMARY);
        g2.drawString(activite.getJour() + "  |  " + activite.getHoraires(), 14, ty);

        if (activite.getPrix() > 0) {
            String prix = String.format("%.0f DT/mois", activite.getPrix());
            g2.setColor(Theme.TEXT_SECONDARY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(prix, w - fm.stringWidth(prix) - 14, ty);
        }
        ty += 22;

        // Progress bar
        int total = activite.getCapaciteMax();
        int inscrits = total - placesRestantes;
        float ratio = total > 0 ? (float) inscrits / total : 0f;
        int bx = 14, by = ty, bw2 = w - 28, bh2 = 5;
        g2.setColor(new Color(226, 232, 240));
        g2.fillRoundRect(bx, by, bw2, bh2, bh2, bh2);
        Color fillC = ratio >= 1f ? Theme.DANGER : ratio >= 0.75f ? Theme.WARNING : Theme.SUCCESS;
        g2.setColor(fillC);
        if (ratio > 0) g2.fillRoundRect(bx, by, (int)(bw2 * ratio), bh2, bh2, bh2);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(Theme.TEXT_MUTED);
        g2.drawString(inscrits + " / " + total + " inscrits", bx, by + bh2 + 13);

        // Selection border
        g2.setColor(selected ? Theme.PRIMARY :
                hovering ? new Color(Theme.PRIMARY.getRed(), Theme.PRIMARY.getGreen(), Theme.PRIMARY.getBlue(), 80)
                         : new Color(225, 230, 242));
        g2.setStroke(new BasicStroke(selected ? 2.5f : 1.5f));
        g2.drawRoundRect(0, 0, w, h, RADIUS, RADIUS);

        g2.dispose();
    }

    private static String truncate(String s, int max) {
        if (s == null || s.length() <= max) return s == null ? "" : s;
        return s.substring(0, max - 1) + "...";
    }
}
