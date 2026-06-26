package com.clubsportif.dao;

import com.clubsportif.model.Paiement;
import java.util.List;

public interface PaiementDAO extends GenericDAO<Paiement> {
    List<Paiement> findByMembreId(String membreId);
    List<Paiement> findByMoisAnnee(int mois, int annee);
    boolean existePaiement(String membreId, String activiteId, int mois, int annee);
}
