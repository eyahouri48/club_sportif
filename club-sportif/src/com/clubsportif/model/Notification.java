package com.clubsportif.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Représente une notification pour un utilisateur.
 */
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Type {
        INSCRIPTION_ACCEPTEE,
        INSCRIPTION_REFUSEE,
        ACTIVITE_COMPLETE,
        NOUVELLE_INSCRIPTION, // pour l'admin
        PAIEMENT_EFFECTUE     // pour l'admin (spec §5)
    }

    private String id;
    private String destinataireId; // membreId du destinataire (ou "ADMIN")
    private String message;
    private Type type;
    private LocalDateTime dateCreation;
    private boolean lue;

    public Notification() {
        this.id = UUID.randomUUID().toString();
        this.dateCreation = LocalDateTime.now();
        this.lue = false;
    }

    public Notification(String destinataireId, String message, Type type) {
        this();
        this.destinataireId = destinataireId;
        this.message = message;
        this.type = type;
    }

    public String getId() { return id; }
    public String getDestinataire() { return destinataireId; }
    public void setDestinataire(String destinataireId) { this.destinataireId = destinataireId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public boolean isLue() { return lue; }
    public void setLue(boolean lue) { this.lue = lue; }
}
