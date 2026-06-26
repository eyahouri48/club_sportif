package com.clubsportif.dao.impl;

import com.clubsportif.dao.MembreDAO;
import com.clubsportif.model.Membre;
import java.util.Optional;

public class MembreDAOImpl extends AbstractFileDAO<Membre> implements MembreDAO {

    public MembreDAOImpl() { super("membres.dat"); }

    @Override protected String getId(Membre m) { return m.getId(); }

    @Override
    public Optional<Membre> findByLogin(String login) {
        return findAll().stream().filter(m -> m.getLogin().equalsIgnoreCase(login)).findFirst();
    }

    @Override
    public boolean loginExiste(String login) {
        return findAll().stream().anyMatch(m -> m.getLogin().equalsIgnoreCase(login));
    }

    @Override
    public boolean emailExiste(String email) {
        return findAll().stream().anyMatch(m -> email.equalsIgnoreCase(m.getEmail()));
    }
}
