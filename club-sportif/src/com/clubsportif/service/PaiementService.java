package com.clubsportif.service;

import com.clubsportif.dao.PaiementDAO;
import com.clubsportif.model.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service paiement — cotisations mensuelles auto-générées.
 * Simplifié : plus de génération manuelle, tout est automatique
 * à l'acceptation de l'inscription.
 */
public class PaiementService {

    public static final double FRAIS_INSCRIPTION_ANNUELLE = 50.0;

    private final PaiementDAO        paiementDAO;
    private final InscriptionService inscriptionService;
    private final ActiviteService    activiteService;
    private NotificationService      notificationService; // optionnel
    private MembreService            membreService;        // optionnel (nom membre)

    public PaiementService(PaiementDAO paiementDAO,
                           InscriptionService inscriptionService,
                           ActiviteService activiteService) {
        this.paiementDAO        = paiementDAO;
        this.inscriptionService = inscriptionService;
        this.activiteService    = activiteService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setMembreService(MembreService membreService) {
        this.membreService = membreService;
    }

    /**
     * Auto-génère le paiement mensuel quand une inscription est acceptée.
     */
    public void genererPaiementPourInscription(String membreId, Activite activite) {
        int mois  = LocalDate.now().getMonthValue();
        int annee = LocalDate.now().getYear();

        if (!paiementDAO.existePaiement(membreId, activite.getId(), mois, annee)) {
            paiementDAO.save(new Paiement(
                membreId, activite.getId(),
                Paiement.Type.COTISATION_MENSUELLE,
                activite.getPrix(), mois, annee
            ));
        }
    }

    /**
     * Génère les lignes de paiement du mois courant pour tous les membres.
     */
    public void genererPaiementsMoisCourant(List<Membre> membres) {
        int mois  = LocalDate.now().getMonthValue();
        int annee = LocalDate.now().getYear();

        for (Membre m : membres) {
            if (m.getRole() != Role.MEMBRE) continue;

            // Inscription annuelle (une fois par an)
            boolean annuelleExiste = paiementDAO.findAll().stream().anyMatch(p ->
                p.getMembreId().equals(m.getId()) &&
                p.getType() == Paiement.Type.INSCRIPTION_ANNUELLE &&
                p.getAnnee() == annee
            );
            if (!annuelleExiste) {
                paiementDAO.save(new Paiement(
                    m.getId(), null,
                    Paiement.Type.INSCRIPTION_ANNUELLE,
                    FRAIS_INSCRIPTION_ANNUELLE, 1, annee
                ));
            }

            // Cotisation mensuelle par activite acceptee
            inscriptionService.getInscriptionsMembre(m.getId()).stream()
                .filter(i -> i.getStatut() == StatutInscription.ACCEPTEE)
                .forEach(i -> {
                    if (!paiementDAO.existePaiement(m.getId(), i.getActiviteId(), mois, annee)) {
                        double prix = activiteService.trouverParId(i.getActiviteId())
                            .map(Activite::getPrix).orElse(0.0);
                        paiementDAO.save(new Paiement(
                            m.getId(), i.getActiviteId(),
                            Paiement.Type.COTISATION_MENSUELLE,
                            prix, mois, annee
                        ));
                    }
                });
        }
    }

    public void marquerPaye(String id) {
        paiementDAO.findById(id).ifPresent(p -> { p.marquerPaye(); paiementDAO.update(p); });
    }

    /**
     * Règlement initié par le membre (spec §5) : enregistre la méthode choisie,
     * passe le statut à PAYE et notifie l'admin. La validation des champs de carte
     * (fictifs) est faite côté UI ; ici on ne traite que la logique métier.
     *
     * @return le paiement mis à jour.
     */
    public Paiement payer(String paiementId, Paiement.Methode methode) {
        Paiement p = paiementDAO.findById(paiementId)
            .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable."));
        if (p.isPaye())
            throw new IllegalArgumentException("Ce paiement a déjà été réglé.");
        if (methode == null)
            throw new IllegalArgumentException("Veuillez choisir une méthode de paiement.");

        p.marquerPaye(methode);
        paiementDAO.update(p);

        if (notificationService != null) {
            String nomMembre = membreService != null
                ? membreService.trouverParId(p.getMembreId()).map(Membre::getNomComplet).orElse("Un membre")
                : "Un membre";
            notificationService.notifierPaiementEffectue(nomMembre, libelle(p), p.getMontant(),
                methode.getLibelle());
        }
        return p;
    }

    /** Libellé lisible d'un paiement (réutilisé UI + notifications). */
    public String libelle(Paiement p) {
        if (p.getType() == Paiement.Type.INSCRIPTION_ANNUELLE)
            return "Inscription annuelle " + p.getAnnee();
        return activiteService.trouverParId(p.getActiviteId())
            .map(Activite::getNom).orElse("Activité");
    }

    public void marquerNonPaye(String id) {
        paiementDAO.findById(id).ifPresent(p -> { p.marquerNonPaye(); paiementDAO.update(p); });
    }

    public List<Paiement> getPaiementsParMembre(String membreId) {
        return paiementDAO.findByMembreId(membreId);
    }

    /** Paiements non réglés du membre — éligibles à « Procéder au paiement » (spec §5). */
    public List<Paiement> getPaiementsEnAttente(String membreId) {
        return paiementDAO.findByMembreId(membreId).stream()
            .filter(p -> !p.isPaye())
            .collect(Collectors.toList());
    }

    public List<Paiement> getPaiementsMoisCourant() {
        return paiementDAO.findByMoisAnnee(
            LocalDate.now().getMonthValue(), LocalDate.now().getYear());
    }

    public List<Paiement> getTousPaiements() {
        return paiementDAO.findAll();
    }

    public double totalEncaisseMoisCourant() {
        return getPaiementsMoisCourant().stream()
            .filter(Paiement::isPaye).mapToDouble(Paiement::getMontant).sum();
    }
}
