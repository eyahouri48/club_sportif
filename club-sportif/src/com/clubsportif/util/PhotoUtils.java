package com.clubsportif.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;

/**
 * Utilitaires photos v3.0 — Rendu HAUTE QUALITÉ avec préservation de résolution.
 * FIX BLUR : stockage des bytes originaux + rendu bicubique multi-étapes.
 */
public final class PhotoUtils {

    private PhotoUtils() {}

    public static byte[] choisirPhoto(Component parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choisir une photo");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images (JPG, PNG, GIF, WebP)", "jpg", "jpeg", "png", "gif", "webp"));
        int result = chooser.showOpenDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) return null;

        try {
            File file = chooser.getSelectedFile();
            // Lire les bytes originaux sans modification
            byte[] bytes = Files.readAllBytes(file.toPath());
            // Valider que c'est une vraie image
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            if (img == null) {
                JOptionPane.showMessageDialog(parent, "Fichier image invalide.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            // Re-encoder en PNG pour préserver la qualité maximale
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "PNG", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent, "Erreur lors de la lecture : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Conversion bytes → ImageIcon HAUTE QUALITÉ.
     * Utilise le redimensionnement progressif (halving) + bicubic interpolation.
     */
    public static ImageIcon toImageIcon(byte[] photoBytes, int width, int height) {
        if (photoBytes == null || photoBytes.length == 0) return null;
        try {
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(photoBytes));
            if (original == null) return null;
            return new ImageIcon(scaleHighQuality(original, width, height));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Redimensionnement HAUTE QUALITÉ avec étapes progressives.
     * Résout le flou causé par un scaling direct sur grands écarts.
     */
    public static BufferedImage scaleHighQuality(BufferedImage src, int targetW, int targetH) {
        int currentW = src.getWidth();
        int currentH = src.getHeight();
        if (currentW <= 0 || currentH <= 0) return src;

        // Calcul cover : remplir la zone cible sans déformation
        double ratioSrc  = (double) currentW / currentH;
        double ratioDest = (double) targetW   / targetH;
        int drawW, drawH, offsetX = 0, offsetY = 0;

        if (ratioSrc > ratioDest) {
            drawH = targetH;
            drawW = (int) Math.round(targetH * ratioSrc);
            offsetX = (drawW - targetW) / 2;
        } else {
            drawW = targetW;
            drawH = (int) Math.round(targetW / ratioSrc);
            offsetY = (drawH - targetH) / 2;
        }

        // Réduction progressive par moitiés pour éviter l'aliasing
        BufferedImage img = src;
        while (img.getWidth() / 2 > drawW || img.getHeight() / 2 > drawH) {
            int stepW = Math.max(drawW, img.getWidth()  / 2);
            int stepH = Math.max(drawH, img.getHeight() / 2);
            img = renderStep(img, stepW, stepH);
        }
        // Étape finale
        BufferedImage step = renderStep(img, drawW, drawH);

        // Crop centré
        BufferedImage result = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        applyQualityHints(g2);
        g2.drawImage(step, -offsetX, -offsetY, null);
        g2.dispose();
        return result;
    }

    private static BufferedImage renderStep(BufferedImage src, int w, int h) {
        // Assurer des dimensions minimales
        w = Math.max(1, w);
        h = Math.max(1, h);
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        applyQualityHints(g2);
        g2.drawImage(src, 0, 0, w, h, null);
        g2.dispose();
        return out;
    }

    public static void applyQualityHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,       RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,           RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,        RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,     RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,      RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,   RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    /**
     * Avatar circulaire haute qualité avec reflet subtil.
     */
    public static JLabel createAvatarLabel(byte[] photoBytes, int size) {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(size, size));
        label.setMinimumSize(new Dimension(size, size));
        label.setMaximumSize(new Dimension(size, size));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        if (photoBytes != null && photoBytes.length > 0) {
            try {
                BufferedImage original = ImageIO.read(new ByteArrayInputStream(photoBytes));
                if (original != null) {
                    // Rendu à 2x pour HiDPI puis réduction
                    int renderSize = Math.max(size * 2, size);
                    BufferedImage scaled = scaleHighQuality(original, renderSize, renderSize);
                    
                    // Clip circulaire sur image 2x
                    BufferedImage rounded = new BufferedImage(renderSize, renderSize, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = rounded.createGraphics();
                    applyQualityHints(g2);
                    g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, renderSize, renderSize));
                    g2.drawImage(scaled, 0, 0, null);
                    g2.dispose();

                    // Réduction finale au size demandé
                    BufferedImage final_img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D gf = final_img.createGraphics();
                    applyQualityHints(gf);
                    gf.drawImage(rounded, 0, 0, size, size, null);
                    gf.dispose();

                    label.setIcon(new ImageIcon(final_img));
                    return label;
                }
            } catch (IOException ignored) {}
        }
        label.setIcon(createDefaultAvatar(size));
        return label;
    }

    public static ImageIcon createDefaultAvatar(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        applyQualityHints(g2);
        // Fond dégradé
        GradientPaint gp = new GradientPaint(0, 0, new Color(37, 99, 235), size, size, new Color(29, 78, 216));
        g2.setPaint(gp);
        g2.fillOval(0, 0, size - 1, size - 1);
        g2.setColor(new Color(255, 255, 255, 200));
        g2.setFont(new Font("Segoe UI", Font.BOLD, size / 3));
        FontMetrics fm = g2.getFontMetrics();
        String text = "?";
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, x, y);
        g2.dispose();
        return new ImageIcon(img);
    }
}
