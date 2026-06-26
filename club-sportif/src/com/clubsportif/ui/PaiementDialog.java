package com.clubsportif.ui;

import com.clubsportif.model.Paiement;
import com.clubsportif.service.PaiementService;
import com.clubsportif.ui.components.*;
import com.clubsportif.util.Theme;
import com.clubsportif.util.Validator;

import javax.swing.*;
import java.awt.*;

/**
 * Dialogue de paiement (spec §5).
 *
 * <p>Flux en deux étapes :
 * <ol>
 *   <li>Choix de la méthode (Visa, Mastercard, e-Dinar) — logos dessinés, aucun asset externe.</li>
 *   <li>Formulaire de carte fictif avec contrôle de saisie strict par champ.</li>
 * </ol>
 *
 * <p>Les données saisies sont fictives : l'objectif est de simuler le flux, pas
 * de traiter un vrai paiement. À la validation, {@link PaiementService#payer}
 * enregistre la méthode, passe le statut à PAYE et notifie l'admin.
 */
public class PaiementDialog extends JDialog {

    private final PaiementService paiementService;
    private final Paiement paiement;

    private boolean paye = false;
    private Paiement.Methode methodeChoisie;

    private final CardLayout cards = new CardLayout();
    private final JPanel cardsHost = new JPanel(cards);

    // Champs carte
    private StyledTextField numeroField;
    private StyledTextField expiryField;
    private StyledTextField cvvField;
    private StyledTextField titulaireField;
    private JLabel formTitleLabel;

    public PaiementDialog(JFrame parent, PaiementService paiementService, Paiement paiement) {
        super(parent, "Paiement", true);
        this.paiementService = paiementService;
        this.paiement = paiement;

        setSize(460, 560);
        setMinimumSize(new Dimension(420, 480));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Theme.BG_MAIN);

        initComponents();
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout(0, 14));
        main.setBackground(Theme.BG_MAIN);
        main.setBorder(BorderFactory.createEmptyBorder(20, 24, 18, 24));

        // En-tête : récapitulatif du montant
        JPanel head = new JPanel();
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setOpaque(false);
        JLabel title = new JLabel("Régler : " + paiementService.libelle(paiement));
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel montant = new JLabel(String.format("Montant : %.0f DT", paiement.getMontant()));
        montant.setFont(Theme.FONT_BODY_BOLD);
        montant.setForeground(Theme.PRIMARY);
        montant.setAlignmentX(LEFT_ALIGNMENT);
        head.add(title);
        head.add(Box.createVerticalStrut(4));
        head.add(montant);
        main.add(head, BorderLayout.NORTH);

        cardsHost.setOpaque(false);
        cardsHost.add(buildMethodStep(), "method");
        cardsHost.add(buildCardStep(),   "card");
        main.add(cardsHost, BorderLayout.CENTER);

        setContentPane(main);
        cards.show(cardsHost, "method");
    }

    // ─── Étape 1 : choix de la méthode ──────────────────────────────────
    private JPanel buildMethodStep() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        JLabel lbl = new JLabel("Choisissez votre méthode de paiement :");
        lbl.setFont(Theme.FONT_BODY);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        panel.add(lbl, BorderLayout.NORTH);

        JPanel options = new JPanel();
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        options.setOpaque(false);

        for (Paiement.Methode m : Paiement.Methode.values()) {
            options.add(buildMethodRow(m));
            options.add(Box.createVerticalStrut(10));
        }
        panel.add(options, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        StyledButton annuler = new StyledButton("Annuler", StyledButton.Style.OUTLINE);
        annuler.addActionListener(e -> dispose());
        actions.add(annuler);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildMethodRow(Paiement.Methode m) {
        JPanel row = new JPanel(new BorderLayout(14, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        row.setAlignmentX(LEFT_ALIGNMENT);

        PaymentLogo logo = new PaymentLogo(m);
        row.add(logo, BorderLayout.WEST);

        JLabel name = new JLabel(m.getLibelle());
        name.setFont(Theme.FONT_BODY_BOLD);
        name.setForeground(Theme.TEXT_PRIMARY);
        row.add(name, BorderLayout.CENTER);

        StyledButton choisir = new StyledButton("Choisir", StyledButton.Style.PRIMARY);
        choisir.addActionListener(e -> {
            methodeChoisie = m;
            formTitleLabel.setText("Carte " + m.getLibelle() + " (données fictives)");
            cards.show(cardsHost, "card");
        });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(choisir);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    // ─── Étape 2 : formulaire de carte ──────────────────────────────────
    private JPanel buildCardStep() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        formTitleLabel = new JLabel("Carte (données fictives)");
        formTitleLabel.setFont(Theme.FONT_BODY_BOLD);
        formTitleLabel.setForeground(Theme.TEXT_PRIMARY);
        panel.add(formTitleLabel, BorderLayout.NORTH);

        numeroField    = new StyledTextField("16 chiffres, ex: 4242424242424242");
        expiryField    = new StyledTextField("MM/AA");
        cvvField       = new StyledTextField("3 chiffres");
        titulaireField = new StyledTextField("Nom du titulaire");

        // Filtres de saisie côté client
        InputFilters.applyDigitFilter(numeroField, 16);
        InputFilters.applyDigitFilter(cvvField, 3);
        InputFilters.applyNameFilter(titulaireField);

        FormBuilder builder = new FormBuilder();
        builder.addField("Numéro de carte *", numeroField)
               .addField("Expiration (MM/AA) *", expiryField)
               .addField("CVV *", cvvField)
               .addField("Titulaire *", titulaireField);
        JPanel form = builder.build();
        form.setOpaque(false);
        panel.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        StyledButton retour = new StyledButton("Retour", StyledButton.Style.OUTLINE);
        retour.addActionListener(e -> cards.show(cardsHost, "method"));
        StyledButton payer = new StyledButton("Payer", StyledButton.Style.SUCCESS);
        payer.addActionListener(e -> validerPaiement());
        actions.add(retour);
        actions.add(payer);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void validerPaiement() {
        // Contrôle de saisie strict, champ par champ, avec message explicite (spec §5).
        if (!Validator.isValidCardNumber(numeroField.getText())) {
            DialogUtils.erreur(this, "Numéro de carte invalide : 16 chiffres attendus.");
            return;
        }
        if (!Validator.isValidExpiry(expiryField.getText())) {
            DialogUtils.erreur(this, "Date d'expiration invalide : format MM/AA et carte non expirée.");
            return;
        }
        if (!Validator.isValidCVV(cvvField.getText())) {
            DialogUtils.erreur(this, "CVV invalide : 3 chiffres attendus.");
            return;
        }
        if (!Validator.isValidCardHolder(titulaireField.getText())) {
            DialogUtils.erreur(this, "Nom du titulaire invalide : lettres uniquement.");
            return;
        }

        try {
            paiementService.payer(paiement.getId(), methodeChoisie);
            paye = true;
            DialogUtils.succes(this,
                "Paiement effectué avec succès via " + methodeChoisie.getLibelle() + " !");
            dispose();
        } catch (IllegalArgumentException ex) {
            DialogUtils.erreur(this, ex.getMessage());
        }
    }

    public boolean isPaye() { return paye; }

    // ─── Logo de paiement dessiné (aucun asset externe) ─────────────────
    private static final class PaymentLogo extends JComponent {
        private final Paiement.Methode methode;

        PaymentLogo(Paiement.Methode methode) {
            this.methode = methode;
            setPreferredSize(new Dimension(56, 36));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            // Fond carte
            g2.setColor(new Color(245, 247, 250));
            g2.fillRoundRect(0, 0, w - 1, h - 1, 8, 8);
            g2.setColor(Theme.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

            switch (methode) {
                case VISA -> {
                    g2.setColor(new Color(26, 31, 113)); // bleu Visa
                    g2.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 16));
                    drawCentered(g2, "VISA", w, h);
                }
                case MASTERCARD -> {
                    // Deux cercles entrelacés
                    int r = h - 14, cy = h / 2 - r / 2;
                    g2.setColor(new Color(235, 0, 27));            // rouge
                    g2.fillOval(w / 2 - r + 4, cy, r, r);
                    g2.setColor(new Color(247, 158, 27, 220));     // jaune/orange
                    g2.fillOval(w / 2 - 4, cy, r, r);
                }
                case E_DINAR -> {
                    g2.setColor(new Color(0, 122, 94));            // vert e-Dinar
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    drawCentered(g2, "e-Dinar", w, h);
                }
            }
            g2.dispose();
        }

        private void drawCentered(Graphics2D g2, String txt, int w, int h) {
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(txt)) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(txt, tx, ty);
        }
    }
}
