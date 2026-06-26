package com.clubsportif.ui;

import com.clubsportif.model.*;
import com.clubsportif.service.ActiviteService;
import com.clubsportif.service.PaiementService;
import com.clubsportif.ui.components.*;
import com.clubsportif.util.Session;
import com.clubsportif.util.Theme;

import javax.swing.*;
import java.awt.BasicStroke;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class PaiementMembrePanel extends JPanel {

    private final PaiementService paiementService;
    private final ActiviteService activiteService;

    public PaiementMembrePanel(PaiementService paiementService,
                               ActiviteService activiteService) {
        this.paiementService = paiementService;
        this.activiteService = activiteService;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContenu(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(Color.WHITE);
        h.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
            new EmptyBorder(20, 24, 16, 24)));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel title = new JLabel("Mes Paiements");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Vos cotisations et inscriptions");
        sub.setFont(Theme.FONT_BODY);
        sub.setForeground(Theme.TEXT_SECONDARY);

        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(sub);
        h.add(left, BorderLayout.WEST);
        return h;
    }

    private JScrollPane buildContenu() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(24, 24, 24, 24));

        String membreId = Session.getInstance().getMembreConnecte() != null
            ? Session.getInstance().getMembreConnecte().getId() : "";

        List<Paiement> paiements = paiementService.getPaiementsParMembre(membreId);

        if (paiements.isEmpty()) {
            JLabel msg = new JLabel("Aucun paiement enregistré pour l'instant.");
            msg.setFont(Theme.FONT_BODY);
            msg.setForeground(Theme.TEXT_MUTED);
            content.add(msg);
        } else {
            // Section inscription annuelle
            content.add(sectionTitle("Inscription annuelle"));
            content.add(Box.createVerticalStrut(10));
            boolean foundAnn = false;
            for (Paiement p : paiements) {
                if (p.getType() == Paiement.Type.INSCRIPTION_ANNUELLE) {
                    content.add(buildCarte(p)); content.add(Box.createVerticalStrut(8));
                    foundAnn = true;
                }
            }
            if (!foundAnn) content.add(videMsg("Aucune inscription annuelle."));

            content.add(Box.createVerticalStrut(24));

            // Section cotisations mensuelles
            content.add(sectionTitle("Cotisations mensuelles"));
            content.add(Box.createVerticalStrut(10));
            boolean foundMens = false;
            for (Paiement p : paiements) {
                if (p.getType() == Paiement.Type.COTISATION_MENSUELLE) {
                    content.add(buildCarte(p)); content.add(Box.createVerticalStrut(8));
                    foundMens = true;
                }
            }
            if (!foundMens) content.add(videMsg("Aucune cotisation mensuelle."));
        }

        content.add(Box.createVerticalGlue());
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_MAIN);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scroll;
    }

    private JPanel buildCarte(Paiement p) {
        boolean paye   = p.isPaye();
        Color   accent = paye ? Theme.SUCCESS : Theme.DANGER;
        Color   bgLight = paye ? Theme.SUCCESS_LIGHT : Theme.DANGER_LIGHT;

        JPanel card = new JPanel(new BorderLayout(16, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        // Spec §5 (correction d'affichage) : hauteur suffisante pour que le bloc
        // statut/bouton ne déborde pas du flux. La largeur s'étire, la hauteur est bornée.
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        card.setPreferredSize(new Dimension(560, 84));
        card.setAlignmentX(LEFT_ALIGNMENT);

        // Infos gauche
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        String label = p.getType() == Paiement.Type.INSCRIPTION_ANNUELLE
            ? "Inscription annuelle " + p.getAnnee()
            : activiteService.trouverParId(p.getActiviteId())
                .map(Activite::getNom).orElse("Activité");
        JLabel nom = new JLabel(label);
        nom.setFont(Theme.FONT_BODY_BOLD);
        nom.setForeground(Theme.TEXT_PRIMARY);

        String periode = p.getType() == Paiement.Type.INSCRIPTION_ANNUELLE ? ""
            : Month.of(p.getMois()).getDisplayName(TextStyle.FULL, Locale.FRENCH)
              + " " + p.getAnnee();
        JLabel periodeL = new JLabel(periode);
        periodeL.setFont(Theme.FONT_SMALL);
        periodeL.setForeground(Theme.TEXT_MUTED);

        info.add(nom);
        if (!periode.isEmpty()) { info.add(Box.createVerticalStrut(2)); info.add(periodeL); }

        // Droite : montant + badge + action
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JLabel montant = new JLabel(String.format("%.0f DT", p.getMontant()));
        montant.setFont(new Font("Segoe UI", Font.BOLD, 16));
        montant.setForeground(Theme.TEXT_PRIMARY);

        String badgeTxt = paye ? "  Payé  " : "  En attente  ";
        JLabel badge = new JLabel(badgeTxt) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgLight);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setFont(Theme.FONT_SMALL_BOLD);
        badge.setForeground(accent);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));

        right.add(montant);
        right.add(badge);

        // Action de paiement (spec §5)
        if (paye) {
            // Indicateur « Paiement effectué » + méthode utilisée
            String methode = p.getMethode() != null ? " (" + p.getMethode().getLibelle() + ")" : "";
            JLabel done = new JLabel("Paiement effectué" + methode);
            done.setFont(Theme.FONT_SMALL_BOLD);
            done.setForeground(Theme.SUCCESS);
            right.add(done);
        } else {
            StyledButton payerBtn = new StyledButton("Procéder au paiement", StyledButton.Style.PRIMARY);
            payerBtn.setFont(Theme.FONT_SMALL_BOLD);
            payerBtn.addActionListener(e -> ouvrirPaiement(p));
            right.add(payerBtn);
        }

        card.add(info,  BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private void ouvrirPaiement(Paiement p) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        PaiementDialog dlg = new PaiementDialog(parent, paiementService, p);
        dlg.setVisible(true);
        if (dlg.isPaye()) {
            rafraichir();
            DialogUtils.succes(this, "Votre paiement a bien été enregistré.");
        }
    }

    /** Reconstruit la zone centrale après un paiement. */
    private void rafraichir() {
        removeAll();
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContenu(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SUBTITLE);
        l.setForeground(Theme.TEXT_PRIMARY);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel videMsg(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BODY);
        l.setForeground(Theme.TEXT_MUTED);
        l.setBorder(new EmptyBorder(4, 4, 4, 4));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }
}
