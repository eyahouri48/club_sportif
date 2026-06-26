package com.clubsportif.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Membre implements Serializable {

    private static final long serialVersionUID = 3L;

    private String id;
    private String login;
    private String motDePasse;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String adresse;
    private String telephone;
    private String email;
    private double poids;
    private double taille; // en cm — pour le calcul BMI
    private Role role;
    private boolean premierAcces;
    private LocalDate dateInscription;
    private byte[] photo; // photo de profil en binaire

    // Fiche sanitaire (Bonus 4)
    private List<String> conditionsMedicales; // ex: "Hypertension", "Diabète", ...
    private String noteSanitaire; // note libre du médecin/admin

    public Membre() {
        this.id = UUID.randomUUID().toString();
        this.role = Role.MEMBRE;
        this.premierAcces = true;
        this.dateInscription = LocalDate.now();
        this.conditionsMedicales = new ArrayList<>();
    }

    public Membre(String login, String motDePasse, String nom, String prenom,
                  LocalDate dateNaissance, String adresse, String telephone,
                  String email, double poids) {
        this();
        this.login = login;
        this.motDePasse = motDePasse;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.poids = poids;
    }

    // --- BMI Calcul ---

    /**
     * Calcule l'IMC (BMI) du membre.
     * Retourne -1 si la taille n'est pas renseignée.
     */
    public double calculerBMI() {
        if (taille <= 0) return -1;
        double tailleM = taille / 100.0;
        return poids / (tailleM * tailleM);
    }

    /**
     * Retourne l'interprétation textuelle du BMI.
     */
    public String getInterprétationBMI() {
        double bmi = calculerBMI();
        if (bmi < 0) return "Taille non renseignée";
        if (bmi < 18.5) return "Sous-poids";
        if (bmi < 25.0) return "Poids normal";
        if (bmi < 30.0) return "Surpoids";
        return "Obésité";
    }

    // --- Getters & Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getPoids() { return poids; }
    public void setPoids(double poids) { this.poids = poids; }

    public double getTaille() { return taille; }
    public void setTaille(double taille) { this.taille = taille; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isPremierAcces() { return premierAcces; }
    public void setPremierAcces(boolean premierAcces) { this.premierAcces = premierAcces; }

    public LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDate dateInscription) { this.dateInscription = dateInscription; }

    public byte[] getPhoto() { return photo; }
    public void setPhoto(byte[] photo) { this.photo = photo; }

    public List<String> getConditionsMedicales() {
        if (conditionsMedicales == null) conditionsMedicales = new ArrayList<>();
        return conditionsMedicales;
    }
    public void setConditionsMedicales(List<String> conditionsMedicales) {
        this.conditionsMedicales = conditionsMedicales;
    }

    public String getNoteSanitaire() { return noteSanitaire; }
    public void setNoteSanitaire(String noteSanitaire) { this.noteSanitaire = noteSanitaire; }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Membre membre = (Membre) o;
        return Objects.equals(id, membre.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return getNomComplet() + " (" + login + ")";
    }
}
