package com.clubsportif.util;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Génère des images sport réalistes par dessin vectoriel haute qualité.
 * Utilisé quand l'activité n'a pas de photo importée.
 */
public final class SportImageGenerator {

    private SportImageGenerator() {}

    // Palette couleurs par sport
    private static final Map<String, int[]> COLORS = new LinkedHashMap<>();
    static {
        COLORS.put("musculation", new int[]{20, 30, 48, 44, 62, 80});
        COLORS.put("crossfit",    new int[]{30, 20, 10, 120, 60, 10});
        COLORS.put("yoga",        new int[]{60, 30, 80, 120, 60, 140});
        COLORS.put("natation",    new int[]{5, 80, 140, 10, 130, 180});
        COLORS.put("piscine",     new int[]{5, 80, 140, 10, 130, 180});
        COLORS.put("boxe",        new int[]{100, 10, 10, 180, 40, 20});
        COLORS.put("football",    new int[]{10, 60, 20, 30, 120, 40});
        COLORS.put("basket",      new int[]{150, 70, 10, 200, 110, 20});
        COLORS.put("tennis",      new int[]{40, 100, 20, 80, 160, 30});
        COLORS.put("zumba",       new int[]{140, 20, 80, 200, 60, 120});
        COLORS.put("danse",       new int[]{100, 20, 100, 160, 50, 160});
        COLORS.put("pilates",     new int[]{70, 50, 120, 130, 90, 170});
        COLORS.put("running",     new int[]{170, 60, 10, 220, 100, 20});
        COLORS.put("course",      new int[]{170, 60, 10, 220, 100, 20});
        COLORS.put("cyclisme",    new int[]{20, 70, 160, 50, 110, 200});
        COLORS.put("vélo",        new int[]{20, 70, 160, 50, 110, 200});
        COLORS.put("escalade",    new int[]{80, 50, 20, 140, 90, 40});
        COLORS.put("arts",        new int[]{40, 20, 10, 100, 50, 20});
        COLORS.put("golf",        new int[]{30, 90, 30, 60, 140, 60});
        COLORS.put("rugby",       new int[]{100, 40, 10, 160, 80, 20});
        COLORS.put("volleyball",  new int[]{10, 60, 140, 40, 100, 180});
        COLORS.put("handball",    new int[]{120, 30, 30, 180, 60, 40});
        COLORS.put("badminton",   new int[]{20, 100, 80, 40, 160, 120});
    }

    public static BufferedImage generate(String sportName, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        setQualityHints(g2);

        // Couleurs selon le sport
        int[] c = getColors(sportName.toLowerCase());
        Color c1 = new Color(c[0], c[1], c[2]);
        Color c2 = new Color(c[3], c[4], c[5]);

        // Fond dégradé diagonal
        GradientPaint gp = new GradientPaint(0, 0, c1, width, height, c2);
        g2.setPaint(gp);
        g2.fillRect(0, 0, width, height);

        // Motif géométrique superposé
        drawPattern(g2, sportName.toLowerCase(), width, height, c1, c2);

        // Overlay sombre en bas pour lisibilité du texte
        GradientPaint overlay = new GradientPaint(
                0, height * 0.4f, new Color(0,0,0,0),
                0, height, new Color(0,0,0,140));
        g2.setPaint(overlay);
        g2.fillRect(0, 0, width, height);

        g2.dispose();
        return img;
    }

    private static void drawPattern(Graphics2D g2, String sport, int w, int h, Color c1, Color c2) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));

        // Cercles décoratifs
        Color light = new Color(255, 255, 255, 80);
        g2.setColor(light);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(w - 90, -30, 160, 160);
        g2.drawOval(w - 50, h - 80, 120, 120);
        g2.drawOval(-40, h - 100, 140, 140);

        // Lignes diagonales
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(new Color(255, 255, 255, 40));
        for (int i = -h; i < w + h; i += 30) {
            g2.drawLine(i, 0, i + h, h);
        }

        // Forme spécifique par sport
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
        g2.setColor(new Color(255, 255, 255, 120));
        g2.setStroke(new BasicStroke(3f));

        if (sport.contains("football") || sport.contains("foot")) {
            // Hexagones ballon foot
            drawHexPattern(g2, w/2, h/2, 45);
        } else if (sport.contains("basket")) {
            // Lignes terrain basket
            g2.drawOval(w/2 - 35, h/2 - 35, 70, 70);
            g2.drawLine(0, h/2, w, h/2);
        } else if (sport.contains("tennis")) {
            // Courbe tennis
            g2.setStroke(new BasicStroke(4f));
            g2.draw(new Arc2D.Float(w/2-60, h/2-60, 120, 120, 30, 120, Arc2D.OPEN));
            g2.draw(new Arc2D.Float(w/2-60, h/2-60, 120, 120, 210, 120, Arc2D.OPEN));
        } else if (sport.contains("natation") || sport.contains("piscine")) {
            // Vagues
            drawWaves(g2, w, h);
        } else if (sport.contains("yoga") || sport.contains("pilates")) {
            // Cercle zen
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    0, new float[]{8, 6}, 0));
            g2.drawOval(w/2 - 50, h/2 - 50, 100, 100);
            g2.drawOval(w/2 - 30, h/2 - 30, 60, 60);
        } else if (sport.contains("boxe")) {
            // Octogone
            drawPolygon(g2, w/2, h/2, 8, 50);
        } else if (sport.contains("running") || sport.contains("course")) {
            // Piste ovale
            g2.drawOval(w/2 - 70, h/2 - 35, 140, 70);
        } else {
            // Forme générique étoile
            drawStar(g2, w/2, h/2 - 10, 5, 45, 20);
        }
    }

    private static void drawHexPattern(Graphics2D g2, int cx, int cy, int r) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int x = cx + (int)(dx * r * 1.8);
                int y = cy + (int)(dy * r * 1.55);
                int[] xs = new int[6], ys = new int[6];
                for (int i = 0; i < 6; i++) {
                    xs[i] = x + (int)(r * Math.cos(Math.PI / 180 * (60 * i + 30)));
                    ys[i] = y + (int)(r * Math.sin(Math.PI / 180 * (60 * i + 30)));
                }
                g2.drawPolygon(xs, ys, 6);
            }
        }
    }

    private static void drawPolygon(Graphics2D g2, int cx, int cy, int sides, int r) {
        int[] xs = new int[sides], ys = new int[sides];
        for (int i = 0; i < sides; i++) {
            xs[i] = cx + (int)(r * Math.cos(2 * Math.PI * i / sides - Math.PI / 2));
            ys[i] = cy + (int)(r * Math.sin(2 * Math.PI * i / sides - Math.PI / 2));
        }
        g2.drawPolygon(xs, ys, sides);
    }

    private static void drawStar(Graphics2D g2, int cx, int cy, int points, int outerR, int innerR) {
        int[] xs = new int[points * 2], ys = new int[points * 2];
        for (int i = 0; i < points * 2; i++) {
            double angle = Math.PI * i / points - Math.PI / 2;
            int r = (i % 2 == 0) ? outerR : innerR;
            xs[i] = cx + (int)(r * Math.cos(angle));
            ys[i] = cy + (int)(r * Math.sin(angle));
        }
        g2.drawPolygon(xs, ys, points * 2);
    }

    private static void drawWaves(Graphics2D g2, int w, int h) {
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int y = h / 3; y <= 2 * h / 3; y += 18) {
            Path2D wave = new Path2D.Float();
            wave.moveTo(0, y);
            for (int x = 0; x <= w; x += 20) {
                wave.curveTo(x + 5, y - 10, x + 15, y + 10, x + 20, y);
            }
            g2.draw(wave);
        }
    }

    private static int[] getColors(String sport) {
        for (Map.Entry<String, int[]> entry : COLORS.entrySet()) {
            if (sport.contains(entry.getKey())) return entry.getValue();
        }
        return new int[]{13, 71, 161, 25, 118, 210}; // bleu par défaut
    }

    private static void setQualityHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }
}
