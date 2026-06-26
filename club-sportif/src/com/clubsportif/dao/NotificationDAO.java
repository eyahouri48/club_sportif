package com.clubsportif.dao;

import com.clubsportif.model.Notification;
import java.util.List;
import java.util.Optional;

public interface NotificationDAO extends GenericDAO<Notification> {
    List<Notification> findByDestinataire(String destinataireId);
    long countNonLues(String destinataireId);
}
