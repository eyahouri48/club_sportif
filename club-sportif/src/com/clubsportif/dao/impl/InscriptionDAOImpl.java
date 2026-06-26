package com.clubsportif.dao.impl;

import com.clubsportif.dao.InscriptionDAO;
import com.clubsportif.model.Inscription;
import com.clubsportif.model.StatutInscription;
import java.util.List;
import java.util.stream.Collectors;

public class InscriptionDAOImpl extends AbstractFileDAO<Inscription> implements InscriptionDAO {

    public InscriptionDAOImpl() { super("inscriptions.dat"); }

    @Override protected String getId(Inscription i) { return i.getId(); }

    @Override
    public List<Inscription> findByMembreId(String membreId) {
        return findAll().stream().filter(i -> i.getMembreId().equals(membreId)).collect(Collectors.toList());
    }

    @Override
    public List<Inscription> findByActiviteId(String activiteId) {
        return findAll().stream().filter(i -> i.getActiviteId().equals(activiteId)).collect(Collectors.toList());
    }

    @Override
    public int countParticipantsAcceptes(String activiteId) {
        return (int) findAll().stream()
                .filter(i -> i.getActiviteId().equals(activiteId) && i.getStatut() == StatutInscription.ACCEPTEE)
                .count();
    }

    @Override
    public boolean existeInscription(String membreId, String activiteId) {
        return findAll().stream()
                .anyMatch(i -> i.getMembreId().equals(membreId) && i.getActiviteId().equals(activiteId));
    }

    @Override
    public List<Inscription> findByStatut(StatutInscription statut) {
        return findAll().stream().filter(i -> i.getStatut() == statut).collect(Collectors.toList());
    }
}
