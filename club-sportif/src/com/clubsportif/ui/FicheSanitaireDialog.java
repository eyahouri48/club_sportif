package com.clubsportif.ui;

import com.clubsportif.model.Membre;
import com.clubsportif.service.MembreService;
import com.clubsportif.ui.components.*;
import com.clubsportif.util.PhotoUtils;
import com.clubsportif.util.Theme;
import com.clubsportif.util.Validator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Dialogue de fiche sanitaire : BMI + conditions médicales.
 * Bonus 4 : Calcul BMI et fiche sanitaire.
 */
public class FicheSanitaireDialog extends JDialog {

    private static final List<String> CONDITIONS = Arrays.asList(
            "Hypertension", "Diabète", "Arthrose", "Asthme",
            "Problèmes cardiaques", "Allergie", "Hernie discale", "Autre"
    );

    private final MembreService membreService;
    private final Membre membre;

    private StyledTextField tailleField;
    private StyledTextField poidsField;
    private final List<JCheckBox> checkboxes = new ArrayList<>();
    private JTextArea noteArea;
    private JLabel bmiResultLabel;
    private JLabel bmiInterpLabel;

    public FicheSanitaireDialog(JFrame parent, MembreService membreService, Membre membre) {
        super(parent, "Fiche Sanitaire — " + membre.getNomComplet(), true);
        this.membreService = membreService;
        this.membre = membre;

        setSize(540, 680);
        setLocationRelativeTo(parent);
        setResizable(true);
        getContentPane().setBackground(Theme.BG_MAIN);

        initComponents();
        chargerDonnees();
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setBackground(Theme.BG_MAIN);
        main.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        // Titre
        JLabel title = new JLabel("Fiche Sanitaire");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        main.add(title, BorderLayout.NORTH);

        // Contenu scrollable
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // === Photo et identité du membre ===
        JPanel identitePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        identitePanel.setOpaque(false);
        identitePanel.setAlignmentX(LEFT_ALIGNMENT);
        identitePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel avatarLabel = PhotoUtils.createAvatarLabel(membre.getPhoto(), 56);
        identitePanel.add(avatarLabel);

        JPanel nomPanel = new JPanel();
        nomPanel.setOpaque(false);
        nomPanel.setLayout(new BoxLayout(nomPanel, BoxLayout.Y_AXIS));
        JLabel nomLabel = new JLabel(membre.getNomComplet());
        nomLabel.setFont(Theme.FONT_SUBTITLE);
        nomLabel.setForeground(Theme.TEXT_PRIMARY);
        JLabel loginLabel = new JLabel("@" + membre.getLogin());
        loginLabel.setFont(Theme.FONT_SMALL);
        loginLabel.setForeground(Theme.TEXT_SECONDARY);
        nomPanel.add(nomLabel);
        nomPanel.add(loginLabel);
        identitePanel.add(nomPanel);
        content.add(identitePanel);
        content.add(Box.createVerticalStrut(16));

        // === Section BMI ===
        content.add(sectionLabel("Calcul IMC (BMI)"));
        content.add(Box.createVerticalStrut(8));

        JPanel mesuresPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        mesuresPanel.setOpaque(false);
        mesuresPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        mesuresPanel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel poidsWrap = new JPanel(new BorderLayout(0, 4));
        poidsWrap.setOpaque(false);
        poidsWrap.add(smallLabel("Poids (kg)"), BorderLayout.NORTH);
        poidsField = new StyledTextField("ex: 75.5");
        poidsField.setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.FIELD_HEIGHT));
        poidsWrap.add(poidsField, BorderLayout.CENTER);

        JPanel tailleWrap = new JPanel(new BorderLayout(0, 4));
        tailleWrap.setOpaque(false);
        tailleWrap.add(smallLabel("Taille (cm)"), BorderLayout.NORTH);
        tailleField = new StyledTextField("ex: 175");
        tailleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.FIELD_HEIGHT));
        tailleWrap.add(tailleField, BorderLayout.CENTER);

        mesuresPanel.add(poidsWrap);
        mesuresPanel.add(tailleWrap);
        content.add(mesuresPanel);
        content.add(Box.createVerticalStrut(10));

        // Bouton calculer BMI
        StyledButton calcBtn = new StyledButton("Calculer le BMI", StyledButton.Style.OUTLINE);
        calcBtn.setAlignmentX(LEFT_ALIGNMENT);
        calcBtn.addActionListener(e -> calculerBMI());
        content.add(calcBtn);
        content.add(Box.createVerticalStrut(10));

        // Résultat BMI
        CardPanel bmiCard = new CardPanel();
        bmiCard.setLayout(new BoxLayout(bmiCard, BoxLayout.Y_AXIS));
        bmiCard.setAlignmentX(LEFT_ALIGNMENT);
        bmiCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        bmiResultLabel = new JLabel("BMI : —");
        bmiResultLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        bmiResultLabel.setForeground(Theme.PRIMARY);

        bmiInterpLabel = new JLabel("Renseignez le poids et la taille pour calculer");
        bmiInterpLabel.setFont(Theme.FONT_SMALL);
        bmiInterpLabel.setForeground(Theme.TEXT_SECONDARY);

        bmiCard.add(bmiResultLabel);
        bmiCard.add(Box.createVerticalStrut(4));
        bmiCard.add(bmiInterpLabel);
        content.add(bmiCard);
        content.add(Box.createVerticalStrut(20));

        // === Section Conditions médicales ===
        content.add(sectionLabel("Conditions medicales"));
        content.add(Box.createVerticalStrut(8));

        JPanel checkPanel = new JPanel(new GridLayout(0, 2, 8, 6));
        checkPanel.setOpaque(false);
        checkPanel.setAlignmentX(LEFT_ALIGNMENT);
        checkPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        for (String cond : CONDITIONS) {
            JCheckBox cb = new JCheckBox(cond);
            cb.setOpaque(false);
            cb.setFont(Theme.FONT_BODY);
            cb.setForeground(Theme.TEXT_PRIMARY);
            checkboxes.add(cb);
            checkPanel.add(cb);
        }
        content.add(checkPanel);
        content.add(Box.createVerticalStrut(16));

        // Note médicale
        content.add(sectionLabel("Note du medecin / Admin"));
        content.add(Box.createVerticalStrut(6));
        noteArea = new JTextArea(4, 30);
        noteArea.setFont(Theme.FONT_BODY);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        JScrollPane noteScroll = new JScrollPane(noteArea);
        noteScroll.setAlignmentX(LEFT_ALIGNMENT);
        noteScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        content.add(noteScroll);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_MAIN);
        main.add(scroll, BorderLayout.CENTER);

        // Boutons bas
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        StyledButton annuler = new StyledButton("Annuler", StyledButton.Style.OUTLINE);
        annuler.addActionListener(e -> dispose());

        StyledButton sauvegarder = new StyledButton("Enregistrer");
        sauvegarder.addActionListener(e -> sauvegarder());

        buttons.add(annuler);
        buttons.add(sauvegarder);
        main.add(buttons, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void chargerDonnees() {
        if (membre.getPoids() > 0) poidsField.setText(String.valueOf(membre.getPoids()));
        if (membre.getTaille() > 0) tailleField.setText(String.valueOf(membre.getTaille()));
        if (membre.getNoteSanitaire() != null) noteArea.setText(membre.getNoteSanitaire());

        List<String> conditions = membre.getConditionsMedicales();
        for (JCheckBox cb : checkboxes) {
            cb.setSelected(conditions.contains(cb.getText()));
        }

        if (membre.getTaille() > 0 && membre.getPoids() > 0) {
            calculerBMI();
        }
    }

    private void calculerBMI() {
        try {
            double poids = Double.parseDouble(poidsField.getText().trim().replace(",", "."));
            double taille = Double.parseDouble(tailleField.getText().trim().replace(",", "."));
            if (poids <= 0 || taille <= 0) throw new NumberFormatException();

            double tailleM = taille / 100.0;
            double bmi = poids / (tailleM * tailleM);

            bmiResultLabel.setText(String.format("BMI : %.1f", bmi));

            Color color;
            String interp;
            if (bmi < 18.5) { interp = "Sous-poids"; color = Theme.WARNING; }
            else if (bmi < 25.0) { interp = "Poids normal"; color = Theme.SUCCESS; }
            else if (bmi < 30.0) { interp = "Surpoids"; color = Theme.WARNING; }
            else { interp = "Obesite"; color = Theme.DANGER; }

            bmiResultLabel.setForeground(color);
            bmiInterpLabel.setText(interp);
            bmiInterpLabel.setForeground(color);

        } catch (NumberFormatException e) {
            DialogUtils.erreur(this, "Veuillez entrer des valeurs numériques valides pour le poids et la taille.");
        }
    }

    private void sauvegarder() {
        try {
            double poids, taille;
            try {
                poids = Double.parseDouble(poidsField.getText().trim().replace(",", "."));
                taille = Double.parseDouble(tailleField.getText().trim().replace(",", "."));
            } catch (NumberFormatException e) {
                DialogUtils.erreur(this, "Poids et taille doivent être des nombres valides.");
                return;
            }

            // Bornes de plausibilité (spec §2)
            if (!Validator.isValidTaille(taille)) {
                DialogUtils.erreur(this, "La taille doit être comprise entre "
                    + (int) Validator.TAILLE_MIN + " et " + (int) Validator.TAILLE_MAX + " cm.");
                return;
            }
            if (!Validator.isValidPoids(poids)) {
                DialogUtils.erreur(this, "Le poids doit être compris entre "
                    + (int) Validator.POIDS_MIN + " et " + (int) Validator.POIDS_MAX + " kg.");
                return;
            }

            membre.setPoids(poids);
            membre.setTaille(taille);
            membre.setNoteSanitaire(noteArea.getText().trim());

            List<String> selected = new ArrayList<>();
            for (JCheckBox cb : checkboxes) {
                if (cb.isSelected()) selected.add(cb.getText());
            }
            membre.setConditionsMedicales(selected);

            membreService.modifierMembre(membre);
            DialogUtils.succes(this, "Fiche sanitaire enregistrée avec succès !");
            dispose();

        } catch (IllegalArgumentException e) {
            DialogUtils.erreur(this, e.getMessage());
        }
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_SUBTITLE);
        lbl.setForeground(Theme.TEXT_PRIMARY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JLabel smallLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD));
        lbl.setForeground(Theme.TEXT_SECONDARY);
        return lbl;
    }
}
