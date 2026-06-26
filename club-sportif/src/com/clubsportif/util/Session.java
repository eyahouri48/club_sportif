package com.clubsportif.util;

import com.clubsportif.model.Membre;
import com.clubsportif.model.Role;

/**
 * Singleton pour gérer la session de l'utilisateur connecté.
 * Accessible partout dans l'application via Session.getInstance().
 *
 * L'admin est un Membre avec Role.ADMIN — pas de cas spécial.
 */
public class Session {

    private static Session instance;
    private Membre membreConnecte;

    private Session() {}

    public static synchronized Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void ouvrirSession(Membre membre) {
        this.membreConnecte = membre;
    }

    public void fermerSession() {
        this.membreConnecte = null;
    }

    public Membre getMembreConnecte() {
        return membreConnecte;
    }

    public boolean isAdmin() {
        return membreConnecte != null && membreConnecte.getRole() == Role.ADMIN;
    }

    public boolean isConnecte() {
        return membreConnecte != null;
    }

    public String getNomUtilisateur() {
        return membreConnecte != null ? membreConnecte.getNomComplet() : "Inconnu";
    }
}
