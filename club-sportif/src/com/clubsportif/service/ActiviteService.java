package com.clubsportif.service;

import com.clubsportif.dao.ActiviteDAO;
import com.clubsportif.dao.InscriptionDAO;
import com.clubsportif.model.Activite;
import com.clubsportif.model.Inscription;
import com.clubsportif.util.Validator;
import java.util.List;
import java.util.Optional;

/**
 * Logique métier pour la gestion des activités.
 */
public class ActiviteService {

    private final ActiviteDAO activiteDAO;
    private final InscriptionDAO inscriptionDAO;

    public ActiviteService(ActiviteDAO activiteDAO, InscriptionDAO inscriptionDAO) {
        this.activiteDAO = activiteDAO;
        this.inscriptionDAO = inscriptionDAO;
    }

    public void creerActivite(Activite activite) {
        validerActivite(activite);
        verifierUnicite(activite);
        activiteDAO.save(activite);
    }

    public void modifierActivite(Activite activite) {
        validerActivite(activite);
        verifierUnicite(activite);
        activiteDAO.update(activite);
    }

    public void supprimerActivite(String activiteId) {
        List<Inscription> inscriptions = inscriptionDAO.findByActiviteId(activiteId);
        for (Inscription i : inscriptions) {
            try { inscriptionDAO.delete(i.getId()); } catch (Exception ignored) {}
        }
        activiteDAO.delete(activiteId);
    }

    public List<Activite> listerActivites() {
        return activiteDAO.findAll();
    }

    public Optional<Activite> trouverParId(String id) {
        return activiteDAO.findById(id);
    }

    public int getPlacesRestantes(String activiteId) {
        Activite activite = activiteDAO.findById(activiteId)
                .orElseThrow(() -> new IllegalArgumentException("Activité introuvable."));
        int inscritsAcceptes = inscriptionDAO.countParticipantsAcceptes(activiteId);
        return Math.max(0, activite.getCapaciteMax() - inscritsAcceptes);
    }

    public boolean estComplete(String activiteId) {
        return getPlacesRestantes(activiteId) == 0;
    }

    public List<Activite> getActivitesCompletes() {
        return activiteDAO.findAll().stream()
                .filter(a -> estComplete(a.getId()))
                .toList();
    }

    /**
     * Validation côté service (équivalent serveur).
     * Les champs sont validés dans l'ordre exact d'apparition dans le formulaire
     * (Nom → Description → Jour → Horaires → Capacité → Prix), et un seul message
     * est levé à la fois — celui du premier champ invalide.
     */
    private void validerActivite(Activite activite) {
        // 1. Nom
        if (Validator.isNullOrEmpty(activite.getNom()))
            throw new IllegalArgumentException("Le nom est obligatoire.");
        // 2. Description
        if (Validator.isNullOrEmpty(activite.getDescription()))
            throw new IllegalArgumentException("La description est obligatoire.");
        // 3. Jour
        if (Validator.isNullOrEmpty(activite.getJour()))
            throw new IllegalArgumentException("Le jour est obligatoire.");
        // 4. Horaires — format et plage horaire stricts
        if (Validator.isNullOrEmpty(activite.getHoraires()))
            throw new IllegalArgumentException("Les horaires sont obligatoires.");
        if (!Validator.isValidPlageHoraire(activite.getHoraires()))
            throw new IllegalArgumentException(
                "Horaire invalide. Format attendu : HH:mm - HH:mm (ex: 08:00 - 10:00), "
                + "avec une heure de fin postérieure à l'heure de début.");
        // 5. Capacité
        if (activite.getCapaciteMax() <= 0)
            throw new IllegalArgumentException("La capacité doit être un entier supérieur à 0.");
        if (activite.getCapaciteMax() > 500)
            throw new IllegalArgumentException("La capacité maximale ne peut dépasser 500.");
        // 6. Prix mensuel — strictement positif (spec §4)
        if (!Validator.isValidPrix(activite.getPrix()))
            throw new IllegalArgumentException("Le prix mensuel doit être strictement supérieur à 0.");
    }

    /**
     * Contrôle d'unicité côté service : nom en doublon et créneau (jour + horaires)
     * déjà occupé. L'activité en cours de modification est exclue de la comparaison.
     */
    private void verifierUnicite(Activite activite) {
        String nom      = activite.getNom() == null ? "" : activite.getNom().trim();
        String jour     = activite.getJour() == null ? "" : activite.getJour().trim();
        String horaires = activite.getHoraires() == null ? "" : activite.getHoraires().trim();

        for (Activite existante : activiteDAO.findAll()) {
            if (existante.getId().equals(activite.getId())) continue; // exclure soi-même

            if (existante.getNom() != null && existante.getNom().trim().equalsIgnoreCase(nom))
                throw new IllegalArgumentException(
                    "Une activité nommée \"" + nom + "\" existe déjà.");

            boolean memeJour     = existante.getJour() != null
                    && existante.getJour().trim().equalsIgnoreCase(jour);
            boolean memeHoraires = existante.getHoraires() != null
                    && existante.getHoraires().trim().equalsIgnoreCase(horaires);
            if (memeJour && memeHoraires)
                throw new IllegalArgumentException(
                    "Le créneau " + jour + " " + horaires + " est déjà occupé par \""
                    + existante.getNom() + "\".");
        }
    }
}
