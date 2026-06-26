package com.clubsportif.dao.impl;

import com.clubsportif.dao.PaiementDAO;
import com.clubsportif.model.Paiement;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PaiementDAOImpl extends AbstractFileDAO<Paiement> implements PaiementDAO {

    public PaiementDAOImpl() { super("paiements.dat"); }

    @Override protected String getId(Paiement p) { return p.getId(); }

    @Override
    public List<Paiement> findByMembreId(String membreId) {
        return findAll().stream()
            .filter(p -> membreId.equals(p.getMembreId()))
            .collect(Collectors.toList());
    }

    @Override
    public List<Paiement> findByMoisAnnee(int mois, int annee) {
        return findAll().stream()
            .filter(p -> p.getMois() == mois && p.getAnnee() == annee)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existePaiement(String membreId, String activiteId, int mois, int annee) {
        return findAll().stream().anyMatch(p ->
            membreId.equals(p.getMembreId()) &&
            Objects.equals(activiteId, p.getActiviteId()) &&
            p.getMois() == mois && p.getAnnee() == annee
        );
    }
}
