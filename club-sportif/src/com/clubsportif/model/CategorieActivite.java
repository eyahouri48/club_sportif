package com.clubsportif.model;

/**
 * Catégorie unique d'activité — simplifiée.
 */
public enum CategorieActivite {
    SPORT("Sport", "🏃");

    private final String libelle;
    private final String emoji;

    CategorieActivite(String libelle, String emoji) {
        this.libelle = libelle;
        this.emoji = emoji;
    }

    public String getLibelle() { return libelle; }
    public String getEmoji() { return emoji; }

    @Override
    public String toString() { return libelle; }
}
