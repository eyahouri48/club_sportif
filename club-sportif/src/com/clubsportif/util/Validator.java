package com.clubsportif.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Utilitaire de validation des données saisies par l'utilisateur.
 * Fichier partagé — les deux devs l'utilisent pour valider les formulaires.
 */
public final class Validator {

    private Validator() {}

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Téléphone : exactement 8 chiffres (spec §1).
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{8}$");

    // Nom/prénom : lettres (avec accents), espaces, tirets et apostrophes uniquement (spec §1).
    private static final Pattern NOM_PATTERN =
            Pattern.compile("^[\\p{L} '\\-]+$");

    // Bornes de plausibilité de la fiche sanitaire (spec §2).
    public static final double TAILLE_MIN = 50.0;   // cm
    public static final double TAILLE_MAX = 250.0;  // cm
    public static final double POIDS_MIN  = 20.0;   // kg
    public static final double POIDS_MAX  = 300.0;  // kg

    // HH:mm sur 24h, plage 00:00–23:59
    private static final Pattern HEURE_PATTERN =
            Pattern.compile("^([01][0-9]|2[0-3]):[0-5][0-9]$");

    // "HH:mm - HH:mm" (espaces autour du tiret tolérés)
    private static final Pattern PLAGE_PATTERN =
            Pattern.compile("^\\s*([01][0-9]|2[0-3]):[0-5][0-9]\\s*-\\s*([01][0-9]|2[0-3]):[0-5][0-9]\\s*$");

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return !isNullOrEmpty(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Téléphone valide = exactement 8 chiffres, chiffres uniquement (spec §1).
     * Les espaces en bordure sont tolérés mais aucun autre caractère n'est nettoyé :
     * un format à 8 chiffres stricts est exigé.
     */
    public static boolean isValidPhone(String phone) {
        if (isNullOrEmpty(phone)) return false;
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Nom / prénom : lettres (accents inclus), espaces, tirets et apostrophes.
     * Tout chiffre ou caractère spécial non autorisé est rejeté (spec §1).
     */
    public static boolean isValidNom(String nom) {
        return !isNullOrEmpty(nom) && NOM_PATTERN.matcher(nom.trim()).matches();
    }

    /** Poids en kg dans une plage de plausibilité (spec §2). */
    public static boolean isValidPoids(String poids) {
        try {
            return isValidPoids(Double.parseDouble(poids.trim().replace(",", ".")));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPoids(double poids) {
        return poids >= POIDS_MIN && poids <= POIDS_MAX;
    }

    /** Taille en cm dans une plage de plausibilité (spec §2). */
    public static boolean isValidTaille(String taille) {
        try {
            return isValidTaille(Double.parseDouble(taille.trim().replace(",", ".")));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidTaille(double taille) {
        return taille >= TAILLE_MIN && taille <= TAILLE_MAX;
    }

    /** Prix mensuel : strictement positif (spec §4). */
    public static boolean isValidPrix(double prix) {
        return prix > 0;
    }

    // ── Validation des champs de paiement fictifs (spec §5) ──────────────

    /** Numéro de carte : exactement 16 chiffres une fois les espaces retirés. */
    public static boolean isValidCardNumber(String raw) {
        if (isNullOrEmpty(raw)) return false;
        String digits = raw.replaceAll("\\s", "");
        return digits.matches("^[0-9]{16}$");
    }

    /** CVV : exactement 3 chiffres. */
    public static boolean isValidCVV(String cvv) {
        return !isNullOrEmpty(cvv) && cvv.trim().matches("^[0-9]{3}$");
    }

    /**
     * Date d'expiration au format MM/AA, mois 01–12, et non expirée
     * (le mois d'expiration courant reste valide).
     */
    public static boolean isValidExpiry(String exp) {
        if (isNullOrEmpty(exp)) return false;
        String e = exp.trim();
        if (!e.matches("^(0[1-9]|1[0-2])/[0-9]{2}$")) return false;
        int mois  = Integer.parseInt(e.substring(0, 2));
        int annee = 2000 + Integer.parseInt(e.substring(3, 5));
        LocalDate now = LocalDate.now();
        // Dernier jour du mois d'expiration.
        LocalDate finExp = LocalDate.of(annee, mois, 1).plusMonths(1).minusDays(1);
        return !finExp.isBefore(now);
    }

    /** Nom du titulaire : mêmes règles que nom/prénom (lettres, espaces, tirets, apostrophes). */
    public static boolean isValidCardHolder(String name) {
        return isValidNom(name);
    }

    public static boolean isValidCapacite(String capacite) {
        try {
            int c = Integer.parseInt(capacite.trim());
            return c > 0 && c <= 500;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidDateNaissance(LocalDate date) {
        if (date == null) return false;
        LocalDate today = LocalDate.now();
        return date.isBefore(today) && date.isAfter(today.minusYears(120));
    }

    /** Valide une heure unique au format HH:mm (00:00–23:59). */
    public static boolean isValidHeure(String heure) {
        return !isNullOrEmpty(heure) && HEURE_PATTERN.matcher(heure.trim()).matches();
    }

    /**
     * Valide une plage "HH:mm - HH:mm" et vérifie que la fin est strictement
     * postérieure au début.
     */
    public static boolean isValidPlageHoraire(String plage) {
        if (isNullOrEmpty(plage) || !PLAGE_PATTERN.matcher(plage).matches()) return false;
        String[] parts = plage.split("-");
        String debut = parts[0].trim();
        String fin   = parts[1].trim();
        return toMinutes(debut) < toMinutes(fin);
    }

    private static int toMinutes(String hhmm) {
        String[] p = hhmm.split(":");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
    }

    public static boolean isValidLogin(String login) {
        return !isNullOrEmpty(login) && login.trim().length() >= 3;
    }

    public static boolean isValidMotDePasse(String mdp) {
        return !isNullOrEmpty(mdp) && mdp.length() >= 4;
    }
}
