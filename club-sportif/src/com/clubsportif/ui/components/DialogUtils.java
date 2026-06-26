package com.clubsportif.ui.components;

import com.clubsportif.util.Theme;
import javax.swing.*;
import java.awt.*;

public final class DialogUtils {

    private DialogUtils() {}

    public static void erreur(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public static void succes(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmer(Component parent, String message, String titre) {
        return JOptionPane.showConfirmDialog(parent, message, titre,
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
