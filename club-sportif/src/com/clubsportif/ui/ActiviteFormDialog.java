package com.clubsportif.ui;

import com.clubsportif.model.Activite;
import com.clubsportif.service.ActiviteService;
import com.clubsportif.ui.components.*;
import com.clubsportif.util.PhotoUtils;
import com.clubsportif.util.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * Dialogue modal pour ajouter ou modifier une activité.
 * Inclut le chargement de photo.
 */
public class ActiviteFormDialog extends JDialog {

    private final ActiviteService activiteService;
    private final Activite activiteExistante;

    private StyledTextField nomField;
    private JTextArea descriptionArea;
    private StyledTextField capaciteField;
    private StyledTextField horairesField;
    private StyledTextField prixField;
    private JComboBox<String> jourCombo;
    private JLabel photoLabel;
    private JLabel photoPreview;
    private byte[] photoBytes;

    private boolean saved = false;

    private static final String[] JOURS = {
            "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"
    };

    public ActiviteFormDialog(JFrame parent, ActiviteService activiteService,
                               Activite activiteExistante) {
        super(parent, activiteExistante == null ? "Ajouter une activité" : "Modifier une activité", true);
        this.activiteService = activiteService;
        this.activiteExistante = activiteExistante;

        setSize(520, 640);
        setLocationRelativeTo(parent);
        setResizable(true);
        setMinimumSize(new Dimension(450, 400));

        initComponents();
        if (activiteExistante != null) remplirChamps();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(Theme.BG_MAIN);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel titleLabel = new JLabel(activiteExistante == null ?
                "Nouvelle activité" : "Modifier l'activité");
        titleLabel.setFont(Theme.FONT_SUBTITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        nomField = new StyledTextField("ex: Musculation, Yoga, Natation...");
        prixField = new StyledTextField("> 0, ex: 80");
        capaciteField = new StyledTextField("ex: 20");
        horairesField = new StyledTextField("ex: 08:00 - 10:00");

        jourCombo = new JComboBox<>(JOURS);
        jourCombo.setFont(Theme.FONT_BODY);
        jourCombo.setPreferredSize(new Dimension(250, Theme.FIELD_HEIGHT));

        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setFont(Theme.FONT_BODY);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setPreferredSize(new Dimension(250, 80));

        // Photo
        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        photoPanel.setOpaque(false);

        photoPreview = new JLabel();
        photoPreview.setPreferredSize(new Dimension(50, 50));

        StyledButton choisirPhoto = new StyledButton("Image", StyledButton.Style.OUTLINE);
        choisirPhoto.setFont(Theme.FONT_SMALL);
        choisirPhoto.setPreferredSize(new Dimension(100, 30));
        choisirPhoto.addActionListener(e -> {
            byte[] b = PhotoUtils.choisirPhoto(this);
            if (b != null) {
                photoBytes = b;
                photoLabel.setText("Image chargee (" + (b.length / 1024) + " Ko)");
                photoLabel.setForeground(Theme.SUCCESS);
                ImageIcon icon = PhotoUtils.toImageIcon(b, 50, 50);
                if (icon != null) photoPreview.setIcon(icon);
            }
        });

        photoLabel = new JLabel("Aucune photo");
        photoLabel.setFont(Theme.FONT_SMALL);
        photoLabel.setForeground(Theme.TEXT_SECONDARY);

        photoPanel.add(photoPreview);
        photoPanel.add(choisirPhoto);
        photoPanel.add(photoLabel);

        FormBuilder builder = new FormBuilder();
        builder.addField("Photo", photoPanel)
               .addSeparator()
               .addField("Nom *", nomField)
               .addField("Description *", descScroll)
               .addField("Jour *", jourCombo)
               .addField("Horaires *", horairesField)
               .addField("Capacité max *", capaciteField)
               .addField("Prix mensuel (DT) *", prixField);

        JScrollPane scrollPane = new JScrollPane(builder.build());
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_MAIN);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)));

        StyledButton cancelBtn = new StyledButton("Annuler", StyledButton.Style.OUTLINE);
        cancelBtn.addActionListener(e -> dispose());

        StyledButton saveBtn = new StyledButton("Enregistrer", StyledButton.Style.SUCCESS);
        saveBtn.addActionListener(e -> sauvegarder());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void remplirChamps() {
        nomField.setText(activiteExistante.getNom());
        descriptionArea.setText(activiteExistante.getDescription());
        capaciteField.setText(String.valueOf(activiteExistante.getCapaciteMax()));
        horairesField.setText(activiteExistante.getHoraires());

        for (int i = 0; i < JOURS.length; i++) {
            if (JOURS[i].equalsIgnoreCase(activiteExistante.getJour())) {
                jourCombo.setSelectedIndex(i);
                break;
            }
        }

        prixField.setText(String.valueOf(activiteExistante.getPrix()));

        if (activiteExistante.getPhoto() != null) {
            photoBytes = activiteExistante.getPhoto();
            photoLabel.setText("Photo existante");
            photoLabel.setForeground(Theme.SUCCESS);
            ImageIcon icon = PhotoUtils.toImageIcon(photoBytes, 50, 50);
            if (icon != null) photoPreview.setIcon(icon);
        }
    }

    private void sauvegarder() {
        try {
            Activite activite = activiteExistante != null ? activiteExistante : new Activite();

            activite.setNom(nomField.getText().trim());
            activite.setDescription(descriptionArea.getText().trim());
            activite.setJour((String) jourCombo.getSelectedItem());
            activite.setHoraires(horairesField.getText().trim());
            if (photoBytes != null) activite.setPhoto(photoBytes);

            // Validation client de l'horaire avant tout appel service (#7)
            if (!com.clubsportif.util.Validator.isValidPlageHoraire(activite.getHoraires())) {
                throw new IllegalArgumentException(
                    "Horaire invalide. Format attendu : HH:mm - HH:mm (ex: 08:00 - 10:00).");
            }

            try {
                activite.setCapaciteMax(Integer.parseInt(capaciteField.getText().trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("La capacité doit être un nombre entier.");
            }
            try {
                double prix = Double.parseDouble(prixField.getText().trim().replace(",", "."));
                if (prix <= 0)
                    throw new IllegalArgumentException("Le prix mensuel doit être strictement supérieur à 0.");
                activite.setPrix(prix);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Le prix doit être un nombre (ex: 80).");
            }

            if (activiteExistante != null) {
                activiteService.modifierActivite(activite);
            } else {
                activiteService.creerActivite(activite);
            }

            saved = true;
            DialogUtils.succes(this, activiteExistante != null ?
                    "Activité modifiée avec succès." : "Activité créée avec succès.");
            dispose();

        } catch (IllegalArgumentException ex) {
            DialogUtils.erreur(this, ex.getMessage());
        }
    }

    public boolean isSaved() { return saved; }
}
