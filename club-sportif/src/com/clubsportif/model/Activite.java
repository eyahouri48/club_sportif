package com.clubsportif.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Activite implements Serializable {

    private static final long serialVersionUID = 4L;

    private String id;
    private String nom;
    private String description;
    private int capaciteMax;
    private String horaires;
    private String jour;
    private byte[] photo;
    private double prix; // cotisation mensuelle en DT

    public Activite() {
        this.id = UUID.randomUUID().toString();
    }

    public Activite(String nom, String description, int capaciteMax,
                    String horaires, String jour) {
        this();
        this.nom = nom;
        this.description = description;
        this.capaciteMax = capaciteMax;
        this.horaires = horaires;
        this.jour = jour;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(int capaciteMax) { this.capaciteMax = capaciteMax; }

    public String getHoraires() { return horaires; }
    public void setHoraires(String horaires) { this.horaires = horaires; }

    public String getJour() { return jour; }
    public void setJour(String jour) { this.jour = jour; }

    public byte[] getPhoto() { return photo; }
    public void setPhoto(byte[] photo) { this.photo = photo; }

    public double getPrix()       { return prix; }
    public void setPrix(double v) { this.prix = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Activite activite = (Activite) o;
        return Objects.equals(id, activite.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return nom + " (" + jour + " - " + horaires + ")";
    }
}
