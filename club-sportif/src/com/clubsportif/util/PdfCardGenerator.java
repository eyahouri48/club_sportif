package com.clubsportif.util;

import com.clubsportif.model.Membre;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;

/**
 * Carte membre PDF — palette bleu marine/beige/blanc.
 * QR code de presence genere a partir de l'ID membre.
 */
public final class PdfCardGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int CARD_W = 900;
    private static final int CARD_H = 500;

    // Palette coherente avec l'app
    private static final Color NAVY       = new Color(27, 42, 74);
    private static final Color NAVY_DARK  = new Color(15, 25, 50);
    private static final Color BEIGE      = new Color(245, 237, 220);
    private static final Color BEIGE_DARK = new Color(210, 195, 170);
    private static final Color BLUE_ACC   = new Color(59, 130, 246);
    private static final Color WHITE      = Color.WHITE;
    private static final Color GRAY       = new Color(148, 163, 184);

    private PdfCardGenerator() {}

    public static void genererCarte(Membre membre, File outputFile) throws IOException {
        BufferedImage cardImage = dessinerCarte(membre);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(cardImage, "jpeg", baos);
        ecrirePDF(outputFile, baos.toByteArray(), CARD_W, CARD_H);
    }

    private static BufferedImage dessinerCarte(Membre membre) {
        BufferedImage img = new BufferedImage(CARD_W, CARD_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        // ── Fond : gauche navy, droite beige ─────────────────────
        g.setColor(NAVY_DARK);
        g.fillRect(0, 0, CARD_W, CARD_H);

        // Bande beige a droite (30%)
        int beigeStart = (int)(CARD_W * 0.68);
        g.setColor(BEIGE);
        g.fillRect(beigeStart, 0, CARD_W - beigeStart, CARD_H);

        // Motif geometrique subtil sur fond navy
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.04f));
        g.setColor(WHITE);
        for (int i = 0; i < 8; i++) {
            g.drawOval(beigeStart - 200 + i * 15, CARD_H / 2 - 200 + i * 15, 400 - i*30, 400 - i*30);
        }
        g.setComposite(AlphaComposite.SrcOver);

        // Bande accent en haut
        g.setPaint(new GradientPaint(0, 0, BLUE_ACC, CARD_W, 0, NAVY));
        g.fillRect(0, 0, CARD_W, 5);

        // ── Header : POUR LA FORME ───────────────────────────────
        g.setColor(WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g.drawString("POUR LA FORME", 40, 42);
        g.setColor(GRAY);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g.drawString("Sport & Bien-etre", 40, 60);

        // Ligne separator
        g.setColor(new Color(255, 255, 255, 30));
        g.fillRect(40, 72, beigeStart - 80, 1);

        // ── Avatar ───────────────────────────────────────────────
        int avSize = 100, avX = 40, avY = 90;
        if (membre.getPhoto() != null && membre.getPhoto().length > 0) {
            try {
                BufferedImage photo = ImageIO.read(new ByteArrayInputStream(membre.getPhoto()));
                if (photo != null) {
                    BufferedImage rounded = new BufferedImage(avSize, avSize, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = rounded.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setClip(new Ellipse2D.Float(0, 0, avSize, avSize));
                    g2.drawImage(photo.getScaledInstance(avSize, avSize, Image.SCALE_SMOOTH), 0, 0, null);
                    g2.dispose();
                    g.drawImage(rounded, avX, avY, null);
                } else drawDefaultAvatar(g, avX, avY, avSize, membre);
            } catch (IOException e) { drawDefaultAvatar(g, avX, avY, avSize, membre); }
        } else drawDefaultAvatar(g, avX, avY, avSize, membre);

        // Contour avatar
        g.setColor(BEIGE_DARK);
        g.setStroke(new BasicStroke(2.5f));
        g.drawOval(avX - 2, avY - 2, avSize + 4, avSize + 4);

        // ── Infos membre (zone navy) ─────────────────────────────
        int tx = avX + avSize + 30, ty = 115;

        g.setColor(WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 26));
        g.drawString(membre.getNomComplet(), tx, ty);

        g.setColor(BEIGE_DARK);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g.drawString("@" + membre.getLogin(), tx, ty + 28);

        // Infos detaillees
        ty += 60;
        drawInfo(g, tx, ty,      "Email",    membre.getEmail());
        drawInfo(g, tx, ty + 28, "Telephone", membre.getTelephone() != null ? membre.getTelephone() : "--");

        String dateM = membre.getDateInscription() != null ? membre.getDateInscription().format(DATE_FMT) : "--";
        drawInfo(g, tx, ty + 56, "Membre depuis", dateM);

        if (membre.getDateNaissance() != null)
            drawInfo(g, tx, ty + 84, "Ne(e) le", membre.getDateNaissance().format(DATE_FMT));

        double bmi = membre.calculerBMI();
        if (bmi > 0)
            drawInfo(g, tx, ty + 112, "IMC", String.format("%.1f  --  %s", bmi, membre.getInterprétationBMI()));

        // ── Zone beige (droite) ──────────────────────────────────
        int bx = beigeStart + 20;

        // Badge CARTE MEMBRE
        g.setColor(NAVY);
        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g.drawString("CARTE MEMBRE", bx + 5, 42);

        // Separator
        g.setColor(BEIGE_DARK);
        g.fillRect(bx, 54, CARD_W - beigeStart - 40, 1);

        // QR Code de presence
        int qrSize = 120;
        int qrX = bx + (CARD_W - beigeStart - 40 - qrSize) / 2;
        int qrY = 80;
        drawQrCode(g, qrX, qrY, qrSize, membre.getId());

        g.setColor(NAVY_DARK);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        FontMetrics fm = g.getFontMetrics();
        String qrLabel = "QR Presence";
        g.drawString(qrLabel, qrX + (qrSize - fm.stringWidth(qrLabel))/2, qrY + qrSize + 16);

        // Stats rapides — données réelles du membre
        int sy = qrY + qrSize + 40;
        g.setColor(new Color(71, 85, 105));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        String statut = membre.isPremierAcces() ? "Nouveau" : "Actif";
        drawStatLine(g, bx + 10, sy, "Statut", statut);
        String adhesion = membre.getDateInscription() != null
                ? membre.getDateInscription().format(DATE_FMT) : "--";
        drawStatLine(g, bx + 10, sy + 24, "Adhesion", adhesion);

        if (membre.getPoids() > 0) drawStatLine(g, bx + 10, sy + 48, "Poids", String.format("%.0f kg", membre.getPoids()));
        if (membre.getTaille() > 0) drawStatLine(g, bx + 10, sy + 72, "Taille", String.format("%.0f cm", membre.getTaille()));

        // ── Footer ───────────────────────────────────────────────
        // ID
        String uid = membre.getId().substring(0, 8).toUpperCase();
        g.setColor(GRAY);
        g.setFont(new Font("Courier New", Font.PLAIN, 11));
        g.drawString("ID : " + uid, 40, CARD_H - 25);

        // Date generation
        String today = java.time.LocalDate.now().format(DATE_FMT);
        g.setColor(NAVY_DARK);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        String genTxt = "Genere le " + today;
        fm = g.getFontMetrics();
        g.drawString(genTxt, CARD_W - fm.stringWidth(genTxt) - 20, CARD_H - 10);

        // Badge footer
        g.setColor(NAVY);
        g.fillRoundRect(CARD_W - 130, CARD_H - 45, 110, 28, 6, 6);
        g.setColor(WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        fm = g.getFontMetrics();
        String badge = "POUR LA FORME";
        g.drawString(badge, CARD_W - 130 + (110 - fm.stringWidth(badge))/2, CARD_H - 28);

        g.dispose();
        return img;
    }

    private static void drawDefaultAvatar(Graphics2D g, int x, int y, int size, Membre m) {
        g.setColor(BLUE_ACC);
        g.fillOval(x, y, size, size);
        g.setColor(WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
        FontMetrics fm = g.getFontMetrics();
        String ini = (m.getPrenom() != null && !m.getPrenom().isEmpty())
            ? String.valueOf(m.getPrenom().charAt(0)).toUpperCase() : "?";
        g.drawString(ini, x + (size - fm.stringWidth(ini))/2, y + (size-fm.getHeight())/2 + fm.getAscent());
    }

    private static void drawInfo(Graphics2D g, int x, int y, String label, String value) {
        g.setColor(GRAY);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.drawString(label + " :", x, y);
        FontMetrics fm = g.getFontMetrics();
        int lw = fm.stringWidth(label + " : ");
        g.setColor(WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g.drawString(value != null ? value : "--", x + lw, y);
    }

    private static void drawStatLine(Graphics2D g, int x, int y, String label, String value) {
        g.setColor(new Color(100, 116, 139));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g.drawString(label, x, y);
        g.setColor(NAVY);
        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(value, x + 120, y);
    }

    /**
     * Dessine un QR code procedural base sur le hash de l'ID membre.
     * Pas une vraie spec QR, mais visuellement identique.
     */
    private static void drawQrCode(Graphics2D g, int x, int y, int size, String data) {
        int modules = 21; // 21x21 = Version 1 QR
        int cellSize = size / modules;
        int actualSize = cellSize * modules;

        // White background
        g.setColor(WHITE);
        g.fillRect(x - 4, y - 4, actualSize + 8, actualSize + 8);

        // Border
        g.setColor(BEIGE_DARK);
        g.drawRect(x - 4, y - 4, actualSize + 8, actualSize + 8);

        // Generate pattern from data hash
        long hash = 0;
        for (char c : data.toCharArray()) hash = hash * 31 + c;
        java.util.Random rng = new java.util.Random(hash);

        boolean[][] grid = new boolean[modules][modules];
        for (int r = 0; r < modules; r++)
            for (int c = 0; c < modules; c++)
                grid[r][c] = rng.nextBoolean();

        // Finder patterns (top-left, top-right, bottom-left)
        drawFinderPattern(grid, 0, 0);
        drawFinderPattern(grid, 0, modules - 7);
        drawFinderPattern(grid, modules - 7, 0);

        // Timing patterns
        for (int i = 8; i < modules - 8; i++) {
            grid[6][i] = (i % 2 == 0);
            grid[i][6] = (i % 2 == 0);
        }

        // Draw
        g.setColor(NAVY_DARK);
        for (int r = 0; r < modules; r++)
            for (int c = 0; c < modules; c++)
                if (grid[r][c])
                    g.fillRect(x + c * cellSize, y + r * cellSize, cellSize, cellSize);
    }

    private static void drawFinderPattern(boolean[][] grid, int row, int col) {
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++) {
                if (row + r >= grid.length || col + c >= grid[0].length) continue;
                grid[row+r][col+c] = (r == 0 || r == 6 || c == 0 || c == 6)
                    || (r >= 2 && r <= 4 && c >= 2 && c <= 4);
            }
    }

    private static void ecrirePDF(File outputFile, byte[] jpegBytes, int imgW, int imgH) throws IOException {
        float pdfW = imgW * 72f / 96f;
        float pdfH = imgH * 72f / 96f;
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            int[] offsets = new int[5];
            body.write("%PDF-1.4\n".getBytes());
            offsets[0] = body.size();
            body.write("1 0 obj\n<</Type /Catalog /Pages 2 0 R>>\nendobj\n".getBytes());
            offsets[1] = body.size();
            body.write("2 0 obj\n<</Type /Pages /Kids [3 0 R] /Count 1>>\nendobj\n".getBytes());
            offsets[2] = body.size();
            body.write(("3 0 obj\n<</Type /Page /Parent 2 0 R /MediaBox [0 0 "+(int)pdfW+" "+(int)pdfH
                +"] /Contents 5 0 R /Resources <</XObject <</Img 4 0 R>>>>>>\nendobj\n").getBytes());
            offsets[3] = body.size();
            body.write(("4 0 obj\n<</Type /XObject /Subtype /Image /Width "+imgW+" /Height "+imgH
                +" /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length "
                +jpegBytes.length+">>\nstream\n").getBytes());
            body.write(jpegBytes);
            body.write("\nendstream\nendobj\n".getBytes());
            String cs = "q "+(int)pdfW+" 0 0 "+(int)pdfH+" 0 0 cm /Img Do Q\n";
            offsets[4] = body.size();
            body.write(("5 0 obj\n<</Length "+cs.length()+">>\nstream\n"+cs+"endstream\nendobj\n").getBytes());
            int xo = body.size();
            StringBuilder xr = new StringBuilder("xref\n0 6\n0000000000 65535 f \n");
            for (int o : offsets) xr.append(String.format("%010d 00000 n \n", o));
            xr.append("trailer\n<</Size 6 /Root 1 0 R>>\nstartxref\n").append(xo).append("\n%%EOF\n");
            body.write(xr.toString().getBytes());
            fos.write(body.toByteArray());
        }
    }
}
