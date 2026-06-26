package com.clubsportif.model;

import java.io.Serializable;

public enum Role implements Serializable {
    ADMIN("Administrateur"),
    MEMBRE("Membre");

    private final String libelle;

    Role(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
