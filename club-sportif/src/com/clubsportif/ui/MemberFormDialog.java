package com.clubsportif.ui;

import com.clubsportif.model.Membre;
import com.clubsportif.service.MembreService;
import com.clubsportif.ui.components.*;
import com.clubsportif.util.PhotoUtils;
import com.clubsportif.util.Theme;
import com.clubsportif.util.Validator;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Dialogue modal pour ajouter ou modifier un membre.
 * FIX v8 : Aperçu photo HAUTE QUALITÉ (80px), design amélioré, champs bien alignés.
 */
public class MemberFormDialog extends JDialog {

    private final MembreService membreService;
    private final Membre membreExistant;

    private StyledTextField loginField;
    private StyledPasswordField passwordField;
    private StyledTextField nomField;
    private StyledTextField prenomField;
    private StyledTextField dateNaissanceField;
    private StyledTextField adresseField;
    private StyledTextField telephoneField;
    private StyledTextField emailField;
    private StyledTextField poidsField;
    private StyledTextField tailleField;

    // Photo
    private JLabel photoPreview;
    private JLabel photoStatusLabel;
    private byte[] photoBytes;

    private boolean saved = false;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public MemberFormDialog(JFrame parent, MembreService membreService, Membre membreExistant) {
        super(parent, membreExistant == null ? "Ajouter un membre" : "Modifier un membre", true);
        this.membreService = membreService;
        this.membreExistant = membreExistant;

        setSize(560, 780);
        setLocationRelativeTo(parent);
        setResizable(true);
        setMinimumSize(new Dimension(480, 500));
        getContentPane().setBackground(Theme.BG_MAIN);

        initComponents();
        if (membreExistant != null) {
            remplirChamps();
        }
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(Theme.BG_MAIN);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));

        // Titre + sous-titre
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel(membreExistant == null ? "Nouveau membre" : "Modifier le membre");
        titleLabel.setFont(Theme.FONT_SUBTITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        JLabel subLabel = new JLabel("Remplissez les informations du membre");
        subLabel.setFont(Theme.FONT_SMALL);
        subLabel.setForeground(Theme.TEXT_SECONDARY);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(subLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // Formulaire
        loginField = new StyledTextField("ex: ahmed.benali");
        passwordField = new StyledPasswordField("Min. 4 caractères");
        nomField = new StyledTextField("ex: Ben Ali");
        prenomField = new StyledTextField("ex: Ahmed");
        dateNaissanceField = new StyledTextField("jj/mm/aaaa");
        adresseField = new StyledTextField("ex: Tunis, Tunisie");
        telephoneField = new StyledTextField("8 chiffres, ex: 12345678");
        emailField = new StyledTextField("ex: ahmed@email.com");
        poidsField = new StyledTextField("20–300 kg, ex: 75.5");
        tailleField = new StyledTextField("50–250 cm, ex: 175");

        // ── Contrôle de saisie côté client (spec §1) ──────────────
        // Nom/Prénom : lettres, espaces, tirets, apostrophes uniquement.
        InputFilters.applyNameFilter(nomField);
        InputFilters.applyNameFilter(prenomField);
        // Téléphone : 8 chiffres maximum, chiffres uniquement.
        InputFilters.applyDigitFilter(telephoneField, 8);

        // ── Zone photo améliorée ──────────────────────────────────
        JPanel photoZone = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 255));
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setColor(Theme.BORDER);
                g2.setStroke(new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_ROUND,
                        java.awt.BasicStroke.JOIN_ROUND, 0, new float[]{4, 4}, 0));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        photoZone.setOpaque(false);
        photoZone.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 12));

        // ── Aperçu photo HAUTE QUALITÉ 80x80 ──────────────────────
        photoPreview = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                super.paintComponent(g);
                g2.setClip(null);
                g2.setColor(Theme.BORDER);
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.drawOval(0, 0, getWidth()-1, getHeight()-1);
                g2.dispose();
            }
        };
        photoPreview.setPreferredSize(new Dimension(80, 80));
        photoPreview.setMinimumSize(new Dimension(80, 80));
        photoPreview.setMaximumSize(new Dimension(80, 80));
        photoPreview.setHorizontalAlignment(SwingConstants.CENTER);
        photoPreview.setIcon(PhotoUtils.createAvatarLabel(null, 80).getIcon());

        JPanel photoInfo = new JPanel();
        photoInfo.setLayout(new BoxLayout(photoInfo, BoxLayout.Y_AXIS));
        photoInfo.setOpaque(false);

        JLabel photoTitle = new JLabel("Photo de profil");
        photoTitle.setFont(Theme.FONT_BODY_BOLD);
        photoTitle.setForeground(Theme.TEXT_PRIMARY);

        photoStatusLabel = new JLabel("Aucune photo sélectionnée");
        photoStatusLabel.setFont(Theme.FONT_SMALL);
        photoStatusLabel.setForeground(Theme.TEXT_SECONDARY);

        StyledButton choisirPhotoBtn = new StyledButton("Choisir une photo", StyledButton.Style.OUTLINE);
        choisirPhotoBtn.setFont(Theme.FONT_SMALL);
        choisirPhotoBtn.setPreferredSize(new Dimension(170, 34));
        choisirPhotoBtn.addActionListener(e -> {
            byte[] b = PhotoUtils.choisirPhoto(this);
            if (b != null) {
                photoBytes = b;
                // Aperçu haute qualité 80px
                photoPreview.setIcon(PhotoUtils.createAvatarLabel(b, 80).getIcon());
                photoStatusLabel.setText("Photo chargee avec succes");
                photoStatusLabel.setForeground(Theme.SUCCESS);
                photoPreview.repaint();
            }
        });

        photoInfo.add(photoTitle);
        photoInfo.add(Box.createVerticalStrut(4));
        photoInfo.add(photoStatusLabel);
        photoInfo.add(Box.createVerticalStrut(8));
        photoInfo.add(choisirPhotoBtn);

        photoZone.add(photoPreview);
        photoZone.add(photoInfo);

        FormBuilder builder = new FormBuilder();
        builder.addField("Photo", photoZone)
               .addSeparator()
               .addField("Login *", loginField)
               .addField("Mot de passe *", passwordField)
               .addSeparator()
               .addField("Nom *", nomField)
               .addField("Prénom *", prenomField)
               .addField("Date naissance *", dateNaissanceField)
               .addField("Adresse", adresseField)
               .addField("Téléphone *", telephoneField)
               .addField("Email *", emailField)
               .addField("Poids (kg)", poidsField)
               .addField("Taille (cm)", tailleField);

        JPanel formPanel = builder.build();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_MAIN);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setWheelScrollingEnabled(true);
        // FIX #3 : le formulaire doit suivre la largeur du viewport sinon une barre
        // horizontale apparaît et "bloque" le défilement vertical à l'ajout d'un membre.
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        formPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, formPanel.getPreferredSize().height));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        if (membreExistant != null) {
            loginField.setEditable(false);
            loginField.setBackground(new Color(241, 245, 249));
        }

        // ── Boutons (toujours visibles en bas) ─────────────────────
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
        loginField.setText(membreExistant.getLogin());
        nomField.setText(membreExistant.getNom());
        prenomField.setText(membreExistant.getPrenom());
        if (membreExistant.getDateNaissance() != null)
            dateNaissanceField.setText(membreExistant.getDateNaissance().format(DATE_FMT));
        adresseField.setText(membreExistant.getAdresse());
        telephoneField.setText(membreExistant.getTelephone());
        emailField.setText(membreExistant.getEmail());
        poidsField.setText(String.valueOf(membreExistant.getPoids()));
        if (membreExistant.getTaille() > 0)
            tailleField.setText(String.valueOf(membreExistant.getTaille()));

        if (membreExistant.getPhoto() != null && membreExistant.getPhoto().length > 0) {
            photoBytes = membreExistant.getPhoto();
            photoPreview.setIcon(PhotoUtils.createAvatarLabel(photoBytes, 80).getIcon());
            photoStatusLabel.setText("Photo existante");
            photoStatusLabel.setForeground(Theme.SUCCESS);
        }
    }

    private void sauvegarder() {
        try {
            Membre membre;
            if (membreExistant != null) {
                membre = membreExistant;
            } else {
                membre = new Membre();
                membre.setLogin(loginField.getText().trim());
                membre.setMotDePasse(new String(passwordField.getPassword()));
            }

            membre.setNom(nomField.getText().trim());
            membre.setPrenom(prenomField.getText().trim());
            membre.setAdresse(adresseField.getText().trim());
            membre.setTelephone(telephoneField.getText().trim());
            membre.setEmail(emailField.getText().trim());

            // ── Contrôles de saisie côté client (spec §1) ─────────────
            if (!Validator.isValidNom(membre.getNom()))
                throw new IllegalArgumentException(
                    "Le nom ne doit contenir que des lettres, espaces, tirets ou apostrophes.");
            if (!Validator.isValidNom(membre.getPrenom()))
                throw new IllegalArgumentException(
                    "Le prénom ne doit contenir que des lettres, espaces, tirets ou apostrophes.");
            if (!Validator.isValidPhone(membre.getTelephone()))
                throw new IllegalArgumentException("Le téléphone doit contenir exactement 8 chiffres.");

            try {
                LocalDate date = LocalDate.parse(dateNaissanceField.getText().trim(), DATE_FMT);
                membre.setDateNaissance(date);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Format de date invalide. Utilisez jj/mm/aaaa.");
            }

            String poidsStr = poidsField.getText().trim();
            if (!poidsStr.isEmpty()) {
                double poids;
                try { poids = Double.parseDouble(poidsStr.replace(",", ".")); }
                catch (NumberFormatException e) { throw new IllegalArgumentException("Le poids doit être un nombre valide."); }
                if (!Validator.isValidPoids(poids))
                    throw new IllegalArgumentException(
                        "Le poids doit être compris entre " + (int) Validator.POIDS_MIN
                        + " et " + (int) Validator.POIDS_MAX + " kg.");
                membre.setPoids(poids);
            }

            String tailleStr = tailleField.getText().trim();
            if (!tailleStr.isEmpty()) {
                double taille;
                try { taille = Double.parseDouble(tailleStr.replace(",", ".")); }
                catch (NumberFormatException e) { throw new IllegalArgumentException("La taille doit être un nombre valide."); }
                if (!Validator.isValidTaille(taille))
                    throw new IllegalArgumentException(
                        "La taille doit être comprise entre " + (int) Validator.TAILLE_MIN
                        + " et " + (int) Validator.TAILLE_MAX + " cm.");
                membre.setTaille(taille);
            }

            if (photoBytes != null) membre.setPhoto(photoBytes);

            if (membreExistant != null) {
                String newPass = new String(passwordField.getPassword());
                if (!newPass.isEmpty()) membre.setMotDePasse(newPass);
                membreService.modifierMembre(membre);
            } else {
                membreService.creerMembre(membre);
            }

            saved = true;
            DialogUtils.succes(this, membreExistant != null ?
                    "Membre modifié avec succès." : "Membre créé avec succès.");
            dispose();

        } catch (IllegalArgumentException ex) {
            DialogUtils.erreur(this, ex.getMessage());
        }
    }

    public boolean isSaved() { return saved; }
}
