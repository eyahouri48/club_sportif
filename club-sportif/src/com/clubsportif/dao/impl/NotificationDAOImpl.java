package com.clubsportif.dao.impl;

import com.clubsportif.dao.NotificationDAO;
import com.clubsportif.model.Notification;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationDAOImpl extends AbstractFileDAO<Notification> implements NotificationDAO {

    public NotificationDAOImpl() { super("notifications.dat"); }

    @Override protected String getId(Notification n) { return n.getId(); }

    @Override
    public List<Notification> findByDestinataire(String destinataireId) {
        return findAll().stream()
                .filter(n -> destinataireId.equals(n.getDestinataire()))
                .sorted((a, b) -> b.getDateCreation().compareTo(a.getDateCreation()))
                .collect(Collectors.toList());
    }

    @Override
    public long countNonLues(String destinataireId) {
        return findAll().stream()
                .filter(n -> destinataireId.equals(n.getDestinataire()) && !n.isLue())
                .count();
    }
}
