package com.clubsportif.util;

import com.clubsportif.dao.ActiviteDAO;
import com.clubsportif.dao.MembreDAO;
import com.clubsportif.model.Activite;
import com.clubsportif.model.Membre;
import com.clubsportif.model.Role;

import java.time.LocalDate;

/**
 * Initialise les données par défaut au premier lancement.
 */
public class DataInitializer {

    private final MembreDAO membreDAO;
    private ActiviteDAO activiteDAO;

    public DataInitializer(MembreDAO membreDAO) {
        this.membreDAO = membreDAO;
    }

    public DataInitializer(MembreDAO membreDAO, ActiviteDAO activiteDAO) {
        this.membreDAO = membreDAO;
        this.activiteDAO = activiteDAO;
    }

    public void initialiser() {
        creerAdminParDefaut();
        if (activiteDAO != null) {
            creerActivitesParDefaut();
        }
    }

    private void creerAdminParDefaut() {
        boolean adminExiste = membreDAO.findAll().stream()
                .anyMatch(m -> m.getRole() == Role.ADMIN);

        if (!adminExiste) {
            Membre admin = new Membre(
                    "admin", "admin",
                    "Lassoued", "Amine",
                    LocalDate.of(1990, 1, 1),
                    "Tunis, Tunisie", "00000000",
                    "amine.lassoued@pourlaforme.tn", 0
            );
            admin.setRole(Role.ADMIN);
            admin.setPremierAcces(false);
            membreDAO.save(admin);
            System.out.println("[Init] Compte admin créé : login=admin / mdp=admin");
        }

        boolean membreExiste = membreDAO.findAll().stream()
                .anyMatch(m -> m.getRole() == Role.MEMBRE);
        if (!membreExiste) {
            Membre membre = new Membre(
                    "ahmed", "1234",
                    "Ben Salem", "Ahmed",
                    LocalDate.of(1998, 5, 15),
                    "Sfax, Tunisie", "98765432",
                    "ahmed@email.tn", 78.5
            );
            membre.setTaille(175);
            membre.setPremierAcces(false);
            membreDAO.save(membre);
            System.out.println("[Init] Membre démo créé : login=ahmed / mdp=1234");
        }
    }

    private void creerActivitesParDefaut() {
        if (!activiteDAO.findAll().isEmpty()) return;

        activiteDAO.save(new Activite("Musculation",
                "Renforcement musculaire avec équipements modernes et coaching personnalisé",
                20, "08:00 - 10:00", "Lundi"));

        activiteDAO.save(new Activite("CrossFit",
                "Entraînement fonctionnel haute intensité — WOD quotidien",
                15, "07:00 - 08:30", "Mardi"));

        activiteDAO.save(new Activite("Yoga",
                "Séances de Hatha Yoga pour souplesse, respiration et relaxation",
                18, "10:00 - 11:30", "Mardi"));

        activiteDAO.save(new Activite("Natation",
                "Séances de natation en piscine semi-olympique — tous niveaux",
                25, "07:00 - 09:00", "Mercredi"));

        activiteDAO.save(new Activite("Boxe",
                "Boxe anglaise et kickboxing — technique, sparring et conditionnement",
                16, "18:00 - 19:30", "Mercredi"));

        activiteDAO.save(new Activite("Football",
                "Entraînement collectif sur terrain synthétique — matchs et tactique",
                22, "17:00 - 19:00", "Jeudi"));

        activiteDAO.save(new Activite("Basketball",
                "Sessions de basketball — dribble, shoot et matchs 5v5",
                20, "16:00 - 18:00", "Vendredi"));

        activiteDAO.save(new Activite("Zumba",
                "Danse fitness dynamique sur des rythmes latinos — fun et cardio",
                30, "18:00 - 19:00", "Vendredi"));

        activiteDAO.save(new Activite("Pilates",
                "Renforcement du core et posture avec exercices au sol",
                14, "09:00 - 10:00", "Samedi"));

        activiteDAO.save(new Activite("Arts Martiaux",
                "Judo et Karaté — discipline, self-défense et compétition",
                18, "10:00 - 12:00", "Samedi"));

        activiteDAO.save(new Activite("Running",
                "Course à pied en groupe — endurance, fractionné et trail",
                30, "07:00 - 08:30", "Dimanche"));

        activiteDAO.save(new Activite("Tennis",
                "Courts de tennis couverts — cours individuels et doubles",
                8, "09:00 - 11:00", "Dimanche"));

        System.out.println("[Init] Activités par défaut créées (" + activiteDAO.findAll().size() + " activités)");
    }
}
