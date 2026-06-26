package com.clubsportif.service;

import com.clubsportif.dao.NotificationDAO;
import com.clubsportif.model.Notification;
import java.util.List;

public class NotificationService {

    private final NotificationDAO notificationDAO;

    public NotificationService(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    public void envoyerNotification(String destinataireId, String message, Notification.Type type) {
        notificationDAO.save(new Notification(destinataireId, message, type));
    }

    public void notifierNouvelleInscription(String nomMembre, String nomActivite) {
        envoyerNotification("ADMIN",
                "Nouvelle inscription : " + nomMembre + "  > " + nomActivite,
                Notification.Type.NOUVELLE_INSCRIPTION);
    }

    public void notifierAcceptation(String membreId, String nomActivite) {
        envoyerNotification(membreId,
                "Votre inscription à \"" + nomActivite + "\" a ete acceptee",
                Notification.Type.INSCRIPTION_ACCEPTEE);
    }

    public void notifierRefus(String membreId, String nomActivite) {
        envoyerNotification(membreId,
                "Votre inscription à \"" + nomActivite + "\" a ete refusee",
                Notification.Type.INSCRIPTION_REFUSEE);
    }

    public void notifierActiviteComplete(String nomActivite) {
        envoyerNotification("ADMIN",
                "L'activité \"" + nomActivite + "\" est maintenant complete",
                Notification.Type.ACTIVITE_COMPLETE);
    }

    /** Notifie l'admin qu'un membre a effectué un paiement (spec §5). */
    public void notifierPaiementEffectue(String nomMembre, String libelle, double montant, String methode) {
        envoyerNotification("ADMIN",
                "Paiement reçu : " + nomMembre + " a réglé \"" + libelle + "\" ("
                + String.format("%.0f DT", montant) + ") via " + methode,
                Notification.Type.PAIEMENT_EFFECTUE);
    }

    public List<Notification> getNotificationsUtilisateur(String destinataireId) {
        return notificationDAO.findByDestinataire(destinataireId);
    }

    public long getNombreNonLues(String destinataireId) {
        return notificationDAO.countNonLues(destinataireId);
    }

    public void marquerCommeLue(String notificationId) {
        notificationDAO.findById(notificationId).ifPresent(n -> {
            n.setLue(true);
            notificationDAO.update(n);
        });
    }

    public void marquerToutesCommeLues(String destinataireId) {
        for (Notification n : notificationDAO.findByDestinataire(destinataireId)) {
            if (!n.isLue()) { n.setLue(true); notificationDAO.update(n); }
        }
    }
}
