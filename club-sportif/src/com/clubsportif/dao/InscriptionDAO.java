package com.clubsportif.dao;

import com.clubsportif.model.Inscription;
import com.clubsportif.model.StatutInscription;
import java.util.List;

/**
 * Contrat d'accès aux données pour les Inscriptions.
 * Dev B implémente cette interface.
 */
public interface InscriptionDAO extends GenericDAO<Inscription> {

    /**
     * Trouve toutes les inscriptions d'un membre.
     */
    List<Inscription> findByMembreId(String membreId);

    /**
     * Trouve toutes les inscriptions pour une activité.
     */
    List<Inscription> findByActiviteId(String activiteId);

    /**
     * Compte le nombre de participants acceptés pour une activité.
     */
    int countParticipantsAcceptes(String activiteId);

    /**
     * Vérifie si un membre est déjà inscrit à une activité.
     */
    boolean existeInscription(String membreId, String activiteId);

    /**
     * Trouve les inscriptions par statut.
     */
    List<Inscription> findByStatut(StatutInscription statut);
}
