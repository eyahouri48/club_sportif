package com.clubsportif.service;

import com.clubsportif.dao.ActiviteDAO;
import com.clubsportif.dao.InscriptionDAO;
import com.clubsportif.dao.MembreDAO;
import com.clubsportif.model.Activite;
import com.clubsportif.model.Inscription;
import com.clubsportif.model.Membre;
import com.clubsportif.model.StatutInscription;
import java.util.List;
import java.util.Optional;

/**
 * Logique métier pour la gestion des inscriptions.
 * Auto-génère les paiements à l'acceptation.
 */
public class InscriptionService {

    private final InscriptionDAO inscriptionDAO;
    private final ActiviteDAO activiteDAO;
    private final MembreDAO membreDAO;
    private NotificationService notificationService;
    private PaiementService paiementService;

    public InscriptionService(InscriptionDAO inscriptionDAO,
                               ActiviteDAO activiteDAO,
                               MembreDAO membreDAO) {
        this.inscriptionDAO = inscriptionDAO;
        this.activiteDAO = activiteDAO;
        this.membreDAO = membreDAO;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setPaiementService(PaiementService paiementService) {
        this.paiementService = paiementService;
    }

    public void inscrire(String membreId, String activiteId) {
        membreDAO.findById(membreId)
                .orElseThrow(() -> new IllegalArgumentException("Membre introuvable."));
        Activite activite = activiteDAO.findById(activiteId)
                .orElseThrow(() -> new IllegalArgumentException("Activité introuvable."));

        if (inscriptionDAO.existeInscription(membreId, activiteId))
            throw new IllegalArgumentException("Vous êtes déjà inscrit à cette activité.");

        int inscritsAcceptes = inscriptionDAO.countParticipantsAcceptes(activiteId);
        if (inscritsAcceptes >= activite.getCapaciteMax())
            throw new IllegalArgumentException("Cette activité est complète. Aucune place disponible.");

        Inscription inscription = new Inscription(membreId, activiteId);
        inscriptionDAO.save(inscription);

        if (notificationService != null) {
            String nomMembre = getNomMembre(membreId);
            notificationService.notifierNouvelleInscription(nomMembre, activite.getNom());
        }
    }

    public void validerInscription(String inscriptionId) {
        Inscription inscription = inscriptionDAO.findById(inscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscription introuvable."));

        if (inscription.getStatut() != StatutInscription.EN_ATTENTE)
            throw new IllegalArgumentException("Seule une inscription en attente peut être validée.");

        Activite activite = activiteDAO.findById(inscription.getActiviteId())
                .orElseThrow(() -> new IllegalArgumentException("Activité introuvable."));

        int inscritsAcceptes = inscriptionDAO.countParticipantsAcceptes(inscription.getActiviteId());
        if (inscritsAcceptes >= activite.getCapaciteMax())
            throw new IllegalArgumentException("Impossible de valider : l'activité est complète.");

        inscription.setStatut(StatutInscription.ACCEPTEE);
        inscriptionDAO.update(inscription);

        // Auto-générer le paiement pour cette activité
        if (paiementService != null) {
            paiementService.genererPaiementPourInscription(inscription.getMembreId(), activite);
        }

        if (notificationService != null) {
            notificationService.notifierAcceptation(inscription.getMembreId(), activite.getNom());
            int total = inscriptionDAO.countParticipantsAcceptes(inscription.getActiviteId());
            if (total >= activite.getCapaciteMax())
                notificationService.notifierActiviteComplete(activite.getNom());
        }
    }

    public void refuserInscription(String inscriptionId) {
        Inscription inscription = inscriptionDAO.findById(inscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscription introuvable."));
        if (inscription.getStatut() != StatutInscription.EN_ATTENTE)
            throw new IllegalArgumentException("Seule une inscription en attente peut être refusée.");

        String activiteNom = getNomActivite(inscription.getActiviteId());
        inscription.setStatut(StatutInscription.REFUSEE);
        inscriptionDAO.update(inscription);

        if (notificationService != null)
            notificationService.notifierRefus(inscription.getMembreId(), activiteNom);
    }

    public void annulerInscription(String inscriptionId) {
        inscriptionDAO.delete(inscriptionId);
    }

    public List<Inscription> listerToutesInscriptions() { return inscriptionDAO.findAll(); }
    public List<Inscription> getInscriptionsMembre(String membreId) { return inscriptionDAO.findByMembreId(membreId); }
    public List<Inscription> getInscriptionsActivite(String activiteId) { return inscriptionDAO.findByActiviteId(activiteId); }
    public int getNombreParticipants(String activiteId) { return inscriptionDAO.countParticipantsAcceptes(activiteId); }

    public String getNomMembre(String membreId) {
        return membreDAO.findById(membreId).map(Membre::getNomComplet).orElse("Inconnu");
    }

    public String getNomActivite(String activiteId) {
        return activiteDAO.findById(activiteId).map(Activite::getNom).orElse("Inconnue");
    }

    public Optional<Inscription> trouverParId(String id) { return inscriptionDAO.findById(id); }
}
