package com.clubsportif.dao;

import com.clubsportif.model.Membre;
import java.util.Optional;

/**
 * Contrat d'accès aux données pour les Membres.
 * Dev A implémente cette interface.
 */
public interface MembreDAO extends GenericDAO<Membre> {

    /**
     * Recherche un membre par son login.
     */
    Optional<Membre> findByLogin(String login);

    /**
     * Vérifie si un login existe déjà.
     */
    boolean loginExiste(String login);

    /**
     * Vérifie si un email existe déjà.
     */
    boolean emailExiste(String email);
}
