package com.clubsportif.ui.components;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

/**
 * Filtres de saisie réutilisables côté client (équivalent UI des contraintes
 * de validation). Empêchent la frappe de caractères non autorisés et appliquent
 * une longueur maximale là où c'est pertinent.
 *
 * <p>Ces filtres NE remplacent PAS la validation côté service : ils améliorent
 * l'expérience de saisie, mais toute donnée reste re-validée à la sauvegarde.
 */
public final class InputFilters {

    private InputFilters() {}

    /** Lettres (accents inclus), espaces, tirets et apostrophes uniquement. */
    public static void applyNameFilter(JTextComponent field) {
        setFilter(field, new RegexFilter("[\\p{L} '\\-]*", Integer.MAX_VALUE));
    }

    /** Chiffres uniquement, longueur maximale {@code maxLen}. */
    public static void applyDigitFilter(JTextComponent field, int maxLen) {
        setFilter(field, new RegexFilter("[0-9]*", maxLen));
    }

    private static void setFilter(JTextComponent field, DocumentFilter filter) {
        Document doc = field.getDocument();
        if (doc instanceof AbstractDocument ad) {
            ad.setDocumentFilter(filter);
        }
    }

    /**
     * Filtre générique : n'accepte une insertion/remplacement que si le texte
     * résultant correspond entièrement au motif autorisé et respecte la longueur max.
     */
    private static final class RegexFilter extends DocumentFilter {
        private final String allowedPattern;
        private final int maxLen;

        RegexFilter(String allowedPattern, int maxLen) {
            this.allowedPattern = allowedPattern;
            this.maxLen = maxLen;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                throws BadLocationException {
            if (text == null) return;
            String result = build(fb, offset, 0, text);
            if (accepts(result)) super.insertString(fb, offset, text, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr)
                throws BadLocationException {
            if (text == null) text = "";
            String result = build(fb, offset, length, text);
            if (accepts(result)) super.replace(fb, offset, length, text, attr);
        }

        private String build(FilterBypass fb, int offset, int length, String inserted)
                throws BadLocationException {
            Document doc = fb.getDocument();
            String current = doc.getText(0, doc.getLength());
            return current.substring(0, offset) + inserted + current.substring(offset + length);
        }

        private boolean accepts(String candidate) {
            return candidate.length() <= maxLen && candidate.matches(allowedPattern);
        }
    }
}
