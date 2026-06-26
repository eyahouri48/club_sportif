package com.clubsportif.dao.impl;

import com.clubsportif.dao.ActiviteDAO;
import com.clubsportif.model.Activite;
import java.util.List;
import java.util.stream.Collectors;

public class ActiviteDAOImpl extends AbstractFileDAO<Activite> implements ActiviteDAO {

    public ActiviteDAOImpl() { super("activites.dat"); }

    @Override protected String getId(Activite a) { return a.getId(); }

    @Override
    public List<Activite> findByNom(String nom) {
        return findAll().stream()
                .filter(a -> a.getNom().toLowerCase().contains(nom.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Activite> findByJour(String jour) {
        return findAll().stream()
                .filter(a -> a.getJour().equalsIgnoreCase(jour))
                .collect(Collectors.toList());
    }
}
