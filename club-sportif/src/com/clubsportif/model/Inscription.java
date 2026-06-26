package com.clubsportif.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Inscription implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String membreId;
    private String activiteId;
    private StatutInscription statut;
    private LocalDateTime dateInscription;

    public Inscription() {
        this.id = UUID.randomUUID().toString();
        this.statut = StatutInscription.EN_ATTENTE;
        this.dateInscription = LocalDateTime.now();
    }

    public Inscription(String membreId, String activiteId) {
        this();
        this.membreId = membreId;
        this.activiteId = activiteId;
    }

    // --- Getters & Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMembreId() { return membreId; }
    public void setMembreId(String membreId) { this.membreId = membreId; }

    public String getActiviteId() { return activiteId; }
    public void setActiviteId(String activiteId) { this.activiteId = activiteId; }

    public StatutInscription getStatut() { return statut; }
    public void setStatut(StatutInscription statut) { this.statut = statut; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inscription that = (Inscription) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Inscription{membreId=" + membreId + ", activiteId=" + activiteId +
               ", statut=" + statut + "}";
    }
}
