package com.clubsportif.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Paiement implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Type   { INSCRIPTION_ANNUELLE, COTISATION_MENSUELLE }
    public enum Statut { PAYE, NON_PAYE }

    /** Méthodes de paiement proposées (spec §5). */
    public enum Methode {
        VISA("Visa"),
        MASTERCARD("Mastercard"),
        E_DINAR("e-Dinar");

        private final String libelle;
        Methode(String libelle) { this.libelle = libelle; }
        public String getLibelle() { return libelle; }
        @Override public String toString() { return libelle; }
    }

    private String     id;
    private String     membreId;
    private String     activiteId;   // null si INSCRIPTION_ANNUELLE
    private Type       type;
    private Statut     statut;
    private double     montant;
    private int        mois;         // 1-12
    private int        annee;
    private LocalDate  datePaiement; // null si NON_PAYE
    private Methode    methode;      // null tant que non payé (spec §5)

    public Paiement() {
        this.id     = UUID.randomUUID().toString();
        this.statut = Statut.NON_PAYE;
        this.annee  = LocalDate.now().getYear();
        this.mois   = LocalDate.now().getMonthValue();
    }

    public Paiement(String membreId, String activiteId, Type type,
                    double montant, int mois, int annee) {
        this();
        this.membreId   = membreId;
        this.activiteId = activiteId;
        this.type       = type;
        this.montant    = montant;
        this.mois       = mois;
        this.annee      = annee;
    }

    public void marquerPaye()    { statut = Statut.PAYE;     datePaiement = LocalDate.now(); }

    /** Marque payé en enregistrant la méthode choisie (spec §5). */
    public void marquerPaye(Methode methode) {
        this.statut       = Statut.PAYE;
        this.datePaiement = LocalDate.now();
        this.methode      = methode;
    }

    public void marquerNonPaye() { statut = Statut.NON_PAYE; datePaiement = null; methode = null; }
    public boolean isPaye()      { return statut == Statut.PAYE; }

    public String    getId()                        { return id; }
    public void      setId(String id)               { this.id = id; }
    public String    getMembreId()                  { return membreId; }
    public void      setMembreId(String v)          { this.membreId = v; }
    public String    getActiviteId()                { return activiteId; }
    public void      setActiviteId(String v)        { this.activiteId = v; }
    public Type      getType()                      { return type; }
    public void      setType(Type v)                { this.type = v; }
    public Statut    getStatut()                    { return statut; }
    public void      setStatut(Statut v)            { this.statut = v; }
    public double    getMontant()                   { return montant; }
    public void      setMontant(double v)           { this.montant = v; }
    public int       getMois()                      { return mois; }
    public void      setMois(int v)                 { this.mois = v; }
    public int       getAnnee()                     { return annee; }
    public void      setAnnee(int v)                { this.annee = v; }
    public LocalDate getDatePaiement()              { return datePaiement; }
    public void      setDatePaiement(LocalDate v)   { this.datePaiement = v; }
    public Methode   getMethode()                   { return methode; }
    public void      setMethode(Methode v)          { this.methode = v; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Paiement)) return false;
        return Objects.equals(id, ((Paiement)o).id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}
