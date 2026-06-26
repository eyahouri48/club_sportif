package com.clubsportif.dao;

import com.clubsportif.model.Activite;
import java.util.List;

/**
 * Contrat d'accès aux données pour les Activités.
 * Dev B implémente cette interface.
 */
public interface ActiviteDAO extends GenericDAO<Activite> {

    /**
     * Recherche les activités par nom (recherche partielle).
     */
    List<Activite> findByNom(String nom);

    /**
     * Recherche les activités par jour de la semaine.
     */
    List<Activite> findByJour(String jour);
}
