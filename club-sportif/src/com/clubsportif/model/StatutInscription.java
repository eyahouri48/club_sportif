package com.clubsportif.model;

import java.io.Serializable;

public enum StatutInscription implements Serializable {
    EN_ATTENTE("En attente"),
    ACCEPTEE("Acceptée"),
    REFUSEE("Refusée");

    private final String libelle;

    StatutInscription(String libelle) {
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
