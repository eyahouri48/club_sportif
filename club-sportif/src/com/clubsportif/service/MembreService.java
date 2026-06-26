package com.clubsportif.service;

import com.clubsportif.dao.InscriptionDAO;
import com.clubsportif.dao.MembreDAO;
import com.clubsportif.model.Inscription;
import com.clubsportif.model.Membre;
import com.clubsportif.model.Role;
import com.clubsportif.util.Validator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Logique métier pour la gestion des membres.
 */
public class MembreService {

    private final MembreDAO membreDAO;
    private InscriptionDAO inscriptionDAO; // optionnel (pour cascade delete)

    public MembreService(MembreDAO membreDAO) {
        this.membreDAO = membreDAO;
    }

    public void setInscriptionDAO(InscriptionDAO inscriptionDAO) {
        this.inscriptionDAO = inscriptionDAO;
    }

    public void creerMembre(Membre membre) {
        validerMembre(membre, true);
        membre.setRole(Role.MEMBRE);
        membre.setPremierAcces(true);
        membreDAO.save(membre);
    }

    public void modifierMembre(Membre membre) {
        validerMembre(membre, false);
        membreDAO.update(membre);
    }

    /**
     * Mise à jour par le membre lui-même de ses informations personnelles NON sensibles
     * (spec §6) : email, téléphone, adresse, photo.
     *
     * <p>Contrôle d'accès : seul le titulaire du compte peut appeler cette méthode pour
     * son propre id. Les champs sensibles/administratifs (login, rôle, statut, fiche
     * sanitaire validée, mot de passe, abonnement) sont volontairement IGNORÉS ici et
     * restent sous le contrôle de l'admin.
     *
     * @param demandeurId id du membre qui effectue la demande (doit être le titulaire)
     */
    public void modifierProfilParMembre(String demandeurId, String cibleId,
                                        String email, String telephone, String adresse,
                                        byte[] photo) {
        if (demandeurId == null || !demandeurId.equals(cibleId)) {
            throw new IllegalArgumentException(
                "Accès refusé : vous ne pouvez modifier que votre propre profil.");
        }
        Membre membre = membreDAO.findById(cibleId)
                .orElseThrow(() -> new IllegalArgumentException("Membre introuvable."));

        // Validation des champs autorisés uniquement.
        String em = email == null ? "" : email.trim();
        String tel = telephone == null ? "" : telephone.trim();
        if (!Validator.isValidEmail(em))
            throw new IllegalArgumentException("L'email n'est pas valide (format: nom@domaine.ext).");
        if (!Validator.isValidPhone(tel))
            throw new IllegalArgumentException("Le téléphone doit contenir exactement 8 chiffres.");

        // Unicité email (hors compte courant).
        for (Membre m : membreDAO.findAll()) {
            if (m.getEmail() != null && m.getEmail().equalsIgnoreCase(em)
                    && !m.getId().equals(cibleId)) {
                throw new IllegalArgumentException("Cet email est déjà utilisé par un autre compte.");
            }
        }

        // Application des seuls champs non sensibles.
        membre.setEmail(em);
        membre.setTelephone(tel);
        membre.setAdresse(adresse == null ? membre.getAdresse() : adresse.trim());
        if (photo != null) membre.setPhoto(photo);

        membreDAO.update(membre);
    }

    public void supprimerMembre(String membreId) {
        Optional<Membre> opt = membreDAO.findById(membreId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Membre introuvable.");
        }
        if (opt.get().getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Impossible de supprimer un administrateur.");
        }

        // Supprimer ses inscriptions en cascade
        if (inscriptionDAO != null) {
            List<Inscription> inscriptions = inscriptionDAO.findByMembreId(membreId);
            for (Inscription i : inscriptions) {
                try { inscriptionDAO.delete(i.getId()); } catch (Exception ignored) {}
            }
        }

        membreDAO.delete(membreId);
    }

    public List<Membre> listerMembres() {
        return membreDAO.findAll().stream()
                .filter(m -> m.getRole() == Role.MEMBRE)
                .collect(Collectors.toList());
    }

    public List<Membre> listerTous() {
        return membreDAO.findAll();
    }

    public Optional<Membre> trouverParId(String id) {
        return membreDAO.findById(id);
    }

    public Optional<Membre> trouverParLogin(String login) {
        return membreDAO.findByLogin(login);
    }

    public void changerMotDePasse(String membreId, String ancienMdp, String nouveauMdp) {
        Membre membre = membreDAO.findById(membreId)
                .orElseThrow(() -> new IllegalArgumentException("Membre introuvable."));

        if (!membre.getMotDePasse().equals(ancienMdp)) {
            throw new IllegalArgumentException("Ancien mot de passe incorrect.");
        }
        if (!Validator.isValidMotDePasse(nouveauMdp)) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit contenir au moins 4 caractères.");
        }
        if (ancienMdp.equals(nouveauMdp)) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit être différent de l'ancien.");
        }

        membre.setMotDePasse(nouveauMdp);
        membre.setPremierAcces(false);
        membreDAO.update(membre);
    }

    private void validerMembre(Membre membre, boolean isCreation) {
        if (Validator.isNullOrEmpty(membre.getNom())) {
            throw new IllegalArgumentException("Le nom est obligatoire.");
        }
        if (!Validator.isValidNom(membre.getNom())) {
            throw new IllegalArgumentException(
                "Le nom ne doit contenir que des lettres, espaces, tirets ou apostrophes.");
        }
        if (Validator.isNullOrEmpty(membre.getPrenom())) {
            throw new IllegalArgumentException("Le prénom est obligatoire.");
        }
        if (!Validator.isValidNom(membre.getPrenom())) {
            throw new IllegalArgumentException(
                "Le prénom ne doit contenir que des lettres, espaces, tirets ou apostrophes.");
        }
        if (!Validator.isValidLogin(membre.getLogin())) {
            throw new IllegalArgumentException("Le login doit contenir au moins 3 caractères.");
        }
        if (isCreation && !Validator.isValidMotDePasse(membre.getMotDePasse())) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 4 caractères.");
        }
        if (!Validator.isValidEmail(membre.getEmail())) {
            throw new IllegalArgumentException("L'email n'est pas valide (format: nom@domaine.ext).");
        }
        if (!Validator.isValidPhone(membre.getTelephone())) {
            throw new IllegalArgumentException("Le téléphone doit contenir exactement 8 chiffres.");
        }
        if (!Validator.isValidDateNaissance(membre.getDateNaissance())) {
            throw new IllegalArgumentException("La date de naissance n'est pas valide.");
        }

        // Bornes de plausibilité fiche sanitaire (spec §2) — uniquement si renseigné.
        if (membre.getTaille() > 0 && !Validator.isValidTaille(membre.getTaille())) {
            throw new IllegalArgumentException(
                "La taille doit être comprise entre " + (int) Validator.TAILLE_MIN
                + " et " + (int) Validator.TAILLE_MAX + " cm.");
        }
        if (membre.getPoids() > 0 && !Validator.isValidPoids(membre.getPoids())) {
            throw new IllegalArgumentException(
                "Le poids doit être compris entre " + (int) Validator.POIDS_MIN
                + " et " + (int) Validator.POIDS_MAX + " kg.");
        }

        // Unicité du login (création uniquement)
        if (isCreation && membreDAO.loginExiste(membre.getLogin())) {
            throw new IllegalArgumentException("Ce login est déjà utilisé.");
        }

        // Unicité de l'email (sauf pour le membre lui-même)
        List<Membre> existants = membreDAO.findAll();
        for (Membre m : existants) {
            if (m.getEmail().equalsIgnoreCase(membre.getEmail())
                    && !m.getId().equals(membre.getId())) {
                throw new IllegalArgumentException("Cet email est déjà utilisé par un autre compte.");
            }
        }
    }
}
