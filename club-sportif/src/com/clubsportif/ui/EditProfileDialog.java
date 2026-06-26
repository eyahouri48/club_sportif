package com.clubsportif.ui;

import com.clubsportif.model.Membre;
import com.clubsportif.service.MembreService;
import com.clubsportif.ui.components.*;
import com.clubsportif.util.Theme;
import com.clubsportif.util.Validator;

import javax.swing.*;
import java.awt.*;

/**
 * Dialogue permettant à un membre de modifier SES informations personnelles
 * non sensibles (spec §6) : email, téléphone, adresse.
 *
 * <p>Les champs sensibles (login, rôle, statut, fiche sanitaire, mot de passe) ne
 * sont pas exposés ici. Le contrôle d'accès final est appliqué côté service via
 * {@link MembreService#modifierProfilParMembre}.
 */
public class EditProfileDialog extends JDialog {

    private final MembreService membreService;
    private final String demandeurId;
    private final Membre membre;

    private StyledTextField emailField;
    private StyledTextField telephoneField;
    private StyledTextField adresseField;

    private boolean saved = false;

    public EditProfileDialog(JFrame parent, MembreService membreService,
                             String demandeurId, Membre membre) {
        super(parent, "Modifier mon profil", true);
        this.membreService = membreService;
        this.demandeurId = demandeurId;
        this.membre = membre;

        setSize(460, 380);
        setMinimumSize(new Dimension(420, 320));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Theme.BG_MAIN);
        initComponents();
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout(0, 14));
        main.setBackground(Theme.BG_MAIN);
        main.setBorder(BorderFactory.createEmptyBorder(20, 24, 18, 24));

        JPanel head = new JPanel();
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setOpaque(false);
        JLabel title = new JLabel("Mes informations de contact");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Seules vos coordonnées sont modifiables ici.");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_SECONDARY);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        head.add(title);
        head.add(Box.createVerticalStrut(2));
        head.add(sub);
        main.add(head, BorderLayout.NORTH);

        emailField     = new StyledTextField("ex: ahmed@email.com");
        telephoneField = new StyledTextField("8 chiffres, ex: 12345678");
        adresseField   = new StyledTextField("ex: Tunis, Tunisie");

        InputFilters.applyDigitFilter(telephoneField, 8);

        emailField.setText(membre.getEmail());
        telephoneField.setText(membre.getTelephone());
        adresseField.setText(membre.getAdresse());

        FormBuilder builder = new FormBuilder();
        builder.addField("Email *", emailField)
               .addField("Téléphone *", telephoneField)
               .addField("Adresse", adresseField);
        JPanel form = builder.build();
        form.setOpaque(false);
        main.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        StyledButton cancel = new StyledButton("Annuler", StyledButton.Style.OUTLINE);
        cancel.addActionListener(e -> dispose());
        StyledButton save = new StyledButton("Enregistrer", StyledButton.Style.SUCCESS);
        save.addActionListener(e -> sauvegarder());
        actions.add(cancel);
        actions.add(save);
        main.add(actions, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void sauvegarder() {
        String email = emailField.getText().trim();
        String tel   = telephoneField.getText().trim();
        // Pré-contrôle client (le service revalide de toute façon).
        if (!Validator.isValidEmail(email)) {
            DialogUtils.erreur(this, "L'email n'est pas valide (format: nom@domaine.ext).");
            return;
        }
        if (!Validator.isValidPhone(tel)) {
            DialogUtils.erreur(this, "Le téléphone doit contenir exactement 8 chiffres.");
            return;
        }
        try {
            membreService.modifierProfilParMembre(
                demandeurId, membre.getId(), email, tel, adresseField.getText(), null);
            saved = true;
            DialogUtils.succes(this, "Profil mis à jour avec succès.");
            dispose();
        } catch (IllegalArgumentException ex) {
            DialogUtils.erreur(this, ex.getMessage());
        }
    }

    public boolean isSaved() { return saved; }
}
