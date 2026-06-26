package com.clubsportif.ui;

import com.clubsportif.model.Membre;
import com.clubsportif.service.MembreService;
import com.clubsportif.ui.components.*;
import com.clubsportif.util.PhotoUtils;
import com.clubsportif.util.Session;
import com.clubsportif.util.Theme;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Panel profil membre — avec photo, BMI et accès à la fiche sanitaire.
 * Bonus 4 + 5 intégrés.
 */
public class ProfilePanel extends JPanel {

    private final MembreService membreService;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ProfilePanel(MembreService membreService) {
        this.membreService = membreService;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        construireInterface();
    }

    public void construireInterface() {
        removeAll();

        Membre membre = Session.getInstance().getMembreConnecte();
        if (membre == null) return;
        membre = membreService.trouverParId(membre.getId()).orElse(membre);
        final Membre m = membre;

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // Titre
        JLabel title = new JLabel("Mon Profil");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(20));

        // === Carte profil : avatar + infos ===
        CardPanel profileCard = new CardPanel();
        profileCard.setLayout(new BorderLayout(20, 0));
        profileCard.setAlignmentX(LEFT_ALIGNMENT);
        profileCard.setMaximumSize(new Dimension(640, 200));

        // Avatar
        JPanel avatarPanel = new JPanel();
        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));
        avatarPanel.setOpaque(false);

        JLabel avatar = PhotoUtils.createAvatarLabel(m.getPhoto(), 80);
        avatar.setAlignmentX(CENTER_ALIGNMENT);

        StyledButton changePhotoBtn = new StyledButton("Photo", StyledButton.Style.OUTLINE);
        changePhotoBtn.setFont(Theme.FONT_SMALL);
        changePhotoBtn.setMaximumSize(new Dimension(90, 28));
        changePhotoBtn.setAlignmentX(CENTER_ALIGNMENT);
        changePhotoBtn.addActionListener(e -> changerPhoto(m));

        avatarPanel.add(Box.createVerticalGlue());
        avatarPanel.add(avatar);
        avatarPanel.add(Box.createVerticalStrut(8));
        avatarPanel.add(changePhotoBtn);
        avatarPanel.add(Box.createVerticalGlue());
        profileCard.add(avatarPanel, BorderLayout.WEST);

        // Infos
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        ajouterLigne(infoPanel, gbc, row++, "Nom complet", m.getNomComplet());
        ajouterLigne(infoPanel, gbc, row++, "Login", m.getLogin());
        ajouterLigne(infoPanel, gbc, row++, "Email", m.getEmail());
        ajouterLigne(infoPanel, gbc, row++, "Téléphone", m.getTelephone());
        ajouterLigne(infoPanel, gbc, row++, "Membre depuis",
                m.getDateInscription() != null ? m.getDateInscription().format(DATE_FMT) : "-");

        profileCard.add(infoPanel, BorderLayout.CENTER);
        content.add(profileCard);
        content.add(Box.createVerticalStrut(16));

        // === Carte BMI ===
        CardPanel bmiCard = new CardPanel();
        bmiCard.setLayout(new BorderLayout(12, 0));
        bmiCard.setAlignmentX(LEFT_ALIGNMENT);
        bmiCard.setMaximumSize(new Dimension(640, 90));

        JPanel bmiInfo = new JPanel();
        bmiInfo.setLayout(new BoxLayout(bmiInfo, BoxLayout.Y_AXIS));
        bmiInfo.setOpaque(false);

        JLabel bmiTitle = new JLabel("Indice de Masse Corporelle (BMI)");
        bmiTitle.setFont(Theme.FONT_SUBTITLE);
        bmiTitle.setForeground(Theme.TEXT_PRIMARY);

        double bmi = m.calculerBMI();
        String bmiText = bmi > 0 ? String.format("%.1f — %s", bmi, m.getInterprétationBMI()) : "Non calculé";
        JLabel bmiVal = new JLabel(bmiText);
        bmiVal.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD));
        bmiVal.setForeground(bmi > 0 ? getBMIColor(bmi) : Theme.TEXT_SECONDARY);

        String condStr = m.getConditionsMedicales().isEmpty()
                ? "Aucune condition renseignée"
                : String.join(", ", m.getConditionsMedicales());
        JLabel condLabel = new JLabel("Conditions : " + condStr);
        condLabel.setFont(Theme.FONT_SMALL);
        condLabel.setForeground(Theme.TEXT_SECONDARY);

        bmiInfo.add(bmiTitle);
        bmiInfo.add(Box.createVerticalStrut(4));
        bmiInfo.add(bmiVal);
        bmiInfo.add(Box.createVerticalStrut(2));
        bmiInfo.add(condLabel);
        bmiCard.add(bmiInfo, BorderLayout.CENTER);

        StyledButton ficheBtn = new StyledButton("Voir fiche", StyledButton.Style.OUTLINE);
        ficheBtn.addActionListener(e -> {
            Membre updated = membreService.trouverParId(m.getId()).orElse(m);
            FicheSanitaireDialog dlg = new FicheSanitaireDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this), membreService, updated);
            dlg.setVisible(true);
            construireInterface(); // Refresh
        });
        bmiCard.add(ficheBtn, BorderLayout.EAST);

        content.add(bmiCard);
        content.add(Box.createVerticalStrut(16));

        // === Boutons actions ===
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionsPanel.setOpaque(false);
        actionsPanel.setAlignmentX(LEFT_ALIGNMENT);

        StyledButton editProfileBtn = new StyledButton("Modifier mes infos", StyledButton.Style.PRIMARY);
        editProfileBtn.addActionListener(e -> {
            Membre courant = membreService.trouverParId(m.getId()).orElse(m);
            EditProfileDialog dlg = new EditProfileDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    membreService, Session.getInstance().getMembreConnecte().getId(), courant);
            dlg.setVisible(true);
            if (dlg.isSaved()) construireInterface();
        });
        actionsPanel.add(editProfileBtn);

        StyledButton changePassBtn = new StyledButton("🔒 Changer le mot de passe", StyledButton.Style.OUTLINE);
        changePassBtn.addActionListener(e -> {
            ChangePasswordDialog dialog = new ChangePasswordDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    membreService, m.getId(), false);
            dialog.setVisible(true);
        });
        actionsPanel.add(changePassBtn);
        content.add(actionsPanel);
        content.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_MAIN);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void changerPhoto(Membre m) {
        byte[] bytes = PhotoUtils.choisirPhoto(this);
        if (bytes == null) return;
        try {
            // Photo = donnée non sensible → passe par le contrôle d'accès membre (spec §6).
            String demandeurId = Session.getInstance().getMembreConnecte().getId();
            membreService.modifierProfilParMembre(
                demandeurId, m.getId(), m.getEmail(), m.getTelephone(), m.getAdresse(), bytes);
            construireInterface();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Color getBMIColor(double bmi) {
        if (bmi < 18.5) return Theme.WARNING;
        if (bmi < 25.0) return Theme.SUCCESS;
        if (bmi < 30.0) return Theme.WARNING;
        return Theme.DANGER;
    }

    private void ajouterLigne(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridy = row;
        gbc.gridx = 0; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD));
        lbl.setForeground(Theme.TEXT_SECONDARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        JLabel val = new JLabel(value != null && !value.isEmpty() ? value : "—");
        val.setFont(Theme.FONT_BODY);
        val.setForeground(Theme.TEXT_PRIMARY);
        panel.add(val, gbc);
    }
}
