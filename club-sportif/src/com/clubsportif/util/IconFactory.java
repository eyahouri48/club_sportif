package com.clubsportif.util;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

/**
 * Fabrique d'icônes vectorielles dessinées en Graphics2D.
 * Remplace les emojis Unicode pour un rendu pro et cohérent.
 */
public final class IconFactory {

    private IconFactory() {}

    public static ImageIcon create(String name, int size, Color color) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setColor(color);

        float s = size / 24f; // normalize to 24px base
        g2.setStroke(new BasicStroke(1.8f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (name) {
            case "dashboard" -> drawDashboard(g2, s);
            case "members" -> drawMembers(g2, s);
            case "activity" -> drawActivity(g2, s);
            case "inscription" -> drawInscription(g2, s);
            case "payment" -> drawPayment(g2, s);
            case "notification" -> drawNotification(g2, s);
            case "stats" -> drawStats(g2, s);
            case "profile" -> drawProfile(g2, s);
            case "logout" -> drawLogout(g2, s);
            case "search" -> drawSearch(g2, s);
            case "add" -> drawAdd(g2, s);
            case "edit" -> drawEdit(g2, s);
            case "delete" -> drawDelete(g2, s);
            case "refresh" -> drawRefresh(g2, s);
            case "check" -> drawCheck(g2, s);
            case "cross" -> drawCross(g2, s);
            case "calendar" -> drawCalendar(g2, s);
            case "clock" -> drawClock(g2, s);
            case "heart" -> drawHeart(g2, s);
            case "medal" -> drawMedal(g2, s);
            case "photo" -> drawPhoto(g2, s);
            case "pdf" -> drawPdf(g2, s);
            case "health" -> drawHealth(g2, s);
            case "password" -> drawPassword(g2, s);
            case "filter" -> drawFilter(g2, s);
            case "info" -> drawInfo(g2, s);
            case "warning" -> drawWarning(g2, s);
            default -> drawDefault(g2, s);
        }

        g2.dispose();
        return new ImageIcon(img);
    }

    public static ImageIcon create(String name, int size) {
        return create(name, size, Theme.PRIMARY);
    }

    // ── Sidebar icons (white on dark) ────────────────────────────
    public static ImageIcon sidebar(String name, int size) {
        return create(name, size, Theme.SIDEBAR_TEXT);
    }

    public static ImageIcon sidebarActive(String name, int size) {
        return create(name, size, Theme.SIDEBAR_ACTIVE);
    }

    // ── Individual icon drawings ─────────────────────────────────

    private static void drawDashboard(Graphics2D g, float s) {
        g.drawRoundRect(r(3*s), r(3*s), r(8*s), r(8*s), r(2*s), r(2*s));
        g.drawRoundRect(r(13*s), r(3*s), r(8*s), r(4*s), r(2*s), r(2*s));
        g.drawRoundRect(r(13*s), r(9*s), r(8*s), r(12*s), r(2*s), r(2*s));
        g.drawRoundRect(r(3*s), r(13*s), r(8*s), r(8*s), r(2*s), r(2*s));
    }

    private static void drawMembers(Graphics2D g, float s) {
        g.drawOval(r(9*s), r(2*s), r(6*s), r(6*s));
        g.draw(new Arc2D.Float(r(5*s), r(10*s), r(14*s), r(12*s), 0, 180, Arc2D.OPEN));
        // Second person behind
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g.drawOval(r(15*s), r(4*s), r(5*s), r(5*s));
        g.draw(new Arc2D.Float(r(13*s), r(11*s), r(10*s), r(10*s), 0, 180, Arc2D.OPEN));
        g.setComposite(AlphaComposite.SrcOver);
    }

    private static void drawActivity(Graphics2D g, float s) {
        // Running person
        g.drawOval(r(12*s), r(2*s), r(4*s), r(4*s));
        g.drawLine(r(14*s), r(6*s), r(12*s), r(12*s));
        g.drawLine(r(12*s), r(12*s), r(8*s), r(18*s));
        g.drawLine(r(12*s), r(12*s), r(16*s), r(18*s));
        g.drawLine(r(14*s), r(8*s), r(18*s), r(6*s));
        g.drawLine(r(14*s), r(8*s), r(8*s), r(10*s));
    }

    private static void drawInscription(Graphics2D g, float s) {
        g.drawRoundRect(r(4*s), r(2*s), r(16*s), r(20*s), r(2*s), r(2*s));
        g.drawLine(r(8*s), r(7*s), r(16*s), r(7*s));
        g.drawLine(r(8*s), r(11*s), r(16*s), r(11*s));
        g.drawLine(r(8*s), r(15*s), r(13*s), r(15*s));
    }

    private static void drawPayment(Graphics2D g, float s) {
        g.drawRoundRect(r(2*s), r(4*s), r(20*s), r(16*s), r(3*s), r(3*s));
        g.drawLine(r(2*s), r(10*s), r(22*s), r(10*s));
        g.fillRect(r(5*s), r(14*s), r(4*s), r(3*s));
    }

    private static void drawNotification(Graphics2D g, float s) {
        GeneralPath bell = new GeneralPath();
        bell.moveTo(10*s, 3*s);
        bell.curveTo(10*s, 2*s, 14*s, 2*s, 14*s, 3*s);
        bell.lineTo(18*s, 14*s);
        bell.lineTo(20*s, 17*s);
        bell.lineTo(4*s, 17*s);
        bell.lineTo(6*s, 14*s);
        bell.closePath();
        g.draw(bell);
        g.draw(new Arc2D.Float(r(9*s), r(17*s), r(6*s), r(5*s), 0, 180, Arc2D.OPEN));
    }

    private static void drawStats(Graphics2D g, float s) {
        g.drawLine(r(3*s), r(20*s), r(3*s), r(4*s));
        g.drawLine(r(3*s), r(20*s), r(21*s), r(20*s));
        g.fillRoundRect(r(6*s), r(12*s), r(3*s), r(8*s), r(1*s), r(1*s));
        g.fillRoundRect(r(11*s), r(7*s), r(3*s), r(13*s), r(1*s), r(1*s));
        g.fillRoundRect(r(16*s), r(4*s), r(3*s), r(16*s), r(1*s), r(1*s));
    }

    private static void drawProfile(Graphics2D g, float s) {
        g.drawOval(r(8*s), r(3*s), r(8*s), r(8*s));
        g.draw(new Arc2D.Float(r(4*s), r(13*s), r(16*s), r(12*s), 0, 180, Arc2D.OPEN));
    }

    private static void drawLogout(Graphics2D g, float s) {
        g.drawLine(r(12*s), r(12*s), r(20*s), r(12*s));
        g.drawLine(r(17*s), r(8*s), r(21*s), r(12*s));
        g.drawLine(r(17*s), r(16*s), r(21*s), r(12*s));
        GeneralPath door = new GeneralPath();
        door.moveTo(12*s, 4*s);
        door.lineTo(5*s, 4*s);
        door.lineTo(5*s, 20*s);
        door.lineTo(12*s, 20*s);
        g.draw(door);
    }

    private static void drawSearch(Graphics2D g, float s) {
        g.drawOval(r(3*s), r(3*s), r(12*s), r(12*s));
        g.drawLine(r(13*s), r(13*s), r(20*s), r(20*s));
    }

    private static void drawAdd(Graphics2D g, float s) {
        g.drawLine(r(12*s), r(5*s), r(12*s), r(19*s));
        g.drawLine(r(5*s), r(12*s), r(19*s), r(12*s));
    }

    private static void drawEdit(Graphics2D g, float s) {
        g.drawLine(r(4*s), r(20*s), r(8*s), r(16*s));
        g.drawLine(r(8*s), r(16*s), r(18*s), r(6*s));
        g.drawLine(r(18*s), r(6*s), r(20*s), r(4*s));
        g.drawLine(r(20*s), r(4*s), r(16*s), r(8*s));
    }

    private static void drawDelete(Graphics2D g, float s) {
        g.drawLine(r(6*s), r(6*s), r(18*s), r(6*s));
        g.drawLine(r(10*s), r(3*s), r(14*s), r(3*s));
        g.drawRoundRect(r(7*s), r(6*s), r(10*s), r(15*s), r(2*s), r(2*s));
        g.drawLine(r(10*s), r(10*s), r(10*s), r(17*s));
        g.drawLine(r(14*s), r(10*s), r(14*s), r(17*s));
    }

    private static void drawRefresh(Graphics2D g, float s) {
        g.draw(new Arc2D.Float(r(4*s), r(4*s), r(16*s), r(16*s), 45, 270, Arc2D.OPEN));
        g.drawLine(r(18*s), r(5*s), r(18*s), r(10*s));
        g.drawLine(r(18*s), r(5*s), r(13*s), r(5*s));
    }

    private static void drawCheck(Graphics2D g, float s) {
        g.setStroke(new BasicStroke(2.5f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(r(5*s), r(12*s), r(10*s), r(17*s));
        g.drawLine(r(10*s), r(17*s), r(19*s), r(7*s));
    }

    private static void drawCross(Graphics2D g, float s) {
        g.drawLine(r(6*s), r(6*s), r(18*s), r(18*s));
        g.drawLine(r(18*s), r(6*s), r(6*s), r(18*s));
    }

    private static void drawCalendar(Graphics2D g, float s) {
        g.drawRoundRect(r(3*s), r(5*s), r(18*s), r(16*s), r(2*s), r(2*s));
        g.drawLine(r(3*s), r(10*s), r(21*s), r(10*s));
        g.drawLine(r(8*s), r(3*s), r(8*s), r(7*s));
        g.drawLine(r(16*s), r(3*s), r(16*s), r(7*s));
    }

    private static void drawClock(Graphics2D g, float s) {
        g.drawOval(r(3*s), r(3*s), r(18*s), r(18*s));
        g.drawLine(r(12*s), r(12*s), r(12*s), r(7*s));
        g.drawLine(r(12*s), r(12*s), r(16*s), r(14*s));
    }

    private static void drawHeart(Graphics2D g, float s) {
        GeneralPath h = new GeneralPath();
        h.moveTo(12*s, 20*s);
        h.curveTo(4*s, 14*s, 2*s, 8*s, 6*s, 5*s);
        h.curveTo(9*s, 3*s, 12*s, 6*s, 12*s, 6*s);
        h.curveTo(12*s, 6*s, 15*s, 3*s, 18*s, 5*s);
        h.curveTo(22*s, 8*s, 20*s, 14*s, 12*s, 20*s);
        g.draw(h);
    }

    private static void drawMedal(Graphics2D g, float s) {
        g.drawOval(r(7*s), r(9*s), r(10*s), r(10*s));
        g.drawLine(r(9*s), r(9*s), r(6*s), r(3*s));
        g.drawLine(r(15*s), r(9*s), r(18*s), r(3*s));
    }

    private static void drawPhoto(Graphics2D g, float s) {
        g.drawRoundRect(r(3*s), r(5*s), r(18*s), r(14*s), r(2*s), r(2*s));
        g.drawOval(r(9*s), r(8*s), r(6*s), r(6*s));
        GeneralPath mountain = new GeneralPath();
        mountain.moveTo(3*s, 19*s);
        mountain.lineTo(8*s, 13*s);
        mountain.lineTo(11*s, 16*s);
        mountain.lineTo(16*s, 11*s);
        mountain.lineTo(21*s, 17*s);
        g.draw(mountain);
    }

    private static void drawPdf(Graphics2D g, float s) {
        g.drawRoundRect(r(5*s), r(2*s), r(14*s), r(20*s), r(2*s), r(2*s));
        g.setFont(new Font("Segoe UI", Font.BOLD, r(7*s)));
        g.drawString("P", r(9*s), r(14*s));
    }

    private static void drawHealth(Graphics2D g, float s) {
        g.setStroke(new BasicStroke(2.2f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(r(9*s), r(6*s), r(9*s), r(18*s));
        g.drawLine(r(3*s), r(12*s), r(15*s), r(12*s));
        g.drawOval(r(2*s), r(2*s), r(20*s), r(20*s));
    }

    private static void drawPassword(Graphics2D g, float s) {
        g.drawOval(r(7*s), r(3*s), r(10*s), r(10*s));
        g.drawRoundRect(r(5*s), r(11*s), r(14*s), r(10*s), r(2*s), r(2*s));
        g.fillOval(r(11*s), r(15*s), r(3*s), r(3*s));
    }

    private static void drawFilter(Graphics2D g, float s) {
        g.drawLine(r(3*s), r(5*s), r(21*s), r(5*s));
        g.drawLine(r(6*s), r(12*s), r(18*s), r(12*s));
        g.drawLine(r(9*s), r(19*s), r(15*s), r(19*s));
    }

    private static void drawInfo(Graphics2D g, float s) {
        g.drawOval(r(3*s), r(3*s), r(18*s), r(18*s));
        g.fillOval(r(11*s), r(7*s), r(2.5f*s), r(2.5f*s));
        g.drawLine(r(12*s), r(11*s), r(12*s), r(17*s));
    }

    private static void drawWarning(Graphics2D g, float s) {
        GeneralPath tri = new GeneralPath();
        tri.moveTo(12*s, 3*s);
        tri.lineTo(22*s, 20*s);
        tri.lineTo(2*s, 20*s);
        tri.closePath();
        g.draw(tri);
        g.fillOval(r(11*s), r(15*s), r(2.5f*s), r(2.5f*s));
        g.drawLine(r(12*s), r(9*s), r(12*s), r(13*s));
    }

    private static void drawDefault(Graphics2D g, float s) {
        g.drawOval(r(4*s), r(4*s), r(16*s), r(16*s));
    }

    private static int r(float v) { return Math.round(v); }
}
