package com.clubsportif.ui;

import com.clubsportif.model.*;
import com.clubsportif.service.*;
import com.clubsportif.ui.components.StyledButton;
import com.clubsportif.util.Theme;

import javax.swing.*;
import java.awt.BasicStroke;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PaiementAdminPanel extends JPanel {

    private final PaiementService paiementService;
    private final MembreService   membreService;
    private final ActiviteService activiteService;

    private JPanel    listPanel;
    private JLabel    totalLabel;
    private JComboBox<String> filtreCombo;

    public PaiementAdminPanel(PaiementService paiementService,
                              MembreService membreService,
                              ActiviteService activiteService) {
        this.paiementService = paiementService;
        this.membreService   = membreService;
        this.activiteService = activiteService;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);
        add(buildHeader(), BorderLayout.NORTH);
        buildList();
        charger();
    }

    // ─── Header ───────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout(12, 0));
        h.setBackground(Color.WHITE);
        h.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
            new EmptyBorder(20, 24, 16, 24)));

        // Titre
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        JLabel title = new JLabel("Paiements");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Suivi des cotisations du mois courant");
        sub.setFont(Theme.FONT_BODY);
        sub.setForeground(Theme.TEXT_SECONDARY);
        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(sub);

        // Droite : total + filtre + bouton
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        totalLabel = new JLabel("Total encaissé : 0 DT");
        totalLabel.setFont(Theme.FONT_BODY_BOLD);
        totalLabel.setForeground(Theme.SUCCESS);

        filtreCombo = new JComboBox<>(new String[]{"Tous", "Payé", "Non payé"});
        filtreCombo.setFont(Theme.FONT_BODY);
        filtreCombo.addActionListener(e -> charger());

        StyledButton genBtn = new StyledButton("Generer ce mois", StyledButton.Style.OUTLINE);
        genBtn.setToolTipText("Créer les lignes de paiement du mois pour tous les membres");
        genBtn.addActionListener(e -> {
            paiementService.genererPaiementsMoisCourant(membreService.listerMembres());
            charger();
            JOptionPane.showMessageDialog(this,
                "Paiements générés avec succès !", "Succès",
                JOptionPane.INFORMATION_MESSAGE);
        });

        right.add(totalLabel);
        right.add(filtreCombo);
        right.add(genBtn);

        h.add(left,  BorderLayout.WEST);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    // ─── Zone liste ────────────────────────────────────────────────────
    private void buildList() {
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setBorder(new EmptyBorder(20, 24, 24, 24));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_MAIN);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scroll, BorderLayout.CENTER);
    }

    // ─── Chargement ────────────────────────────────────────────────────
    public void charger() {
        listPanel.removeAll();

        String filtre = (String) filtreCombo.getSelectedItem();
        List<Paiement> liste = paiementService.getPaiementsMoisCourant();
        if ("Payé".equals(filtre))
            liste = liste.stream().filter(Paiement::isPaye).collect(Collectors.toList());
        else if ("Non payé".equals(filtre))
            liste = liste.stream().filter(p -> !p.isPaye()).collect(Collectors.toList());

        double total = paiementService.totalEncaisseMoisCourant();
        totalLabel.setText(String.format("Total encaissé : %.0f DT", total));

        if (liste.isEmpty()) {
            JLabel empty = new JLabel(
                "Aucun paiement. Cliquez « Générer ce mois » pour créer les lignes.");
            empty.setFont(Theme.FONT_BODY);
            empty.setForeground(Theme.TEXT_MUTED);
            empty.setBorder(new EmptyBorder(20, 0, 0, 0));
            listPanel.add(empty);
        } else {
            listPanel.add(buildEntete());
            listPanel.add(Box.createVerticalStrut(6));
            for (Paiement p : liste) {
                listPanel.add(buildLigne(p));
                listPanel.add(Box.createVerticalStrut(5));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    // ─── Ligne en-tête colonnes ─────────────────────────────────────────
    private JPanel buildEntete() {
        JPanel row = new JPanel(new GridLayout(1, 6, 8, 0));
        row.setBackground(new Color(248, 250, 252));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            new EmptyBorder(8, 12, 8, 12)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        for (String col : new String[]{"Membre", "Type", "Activité", "Mois", "Montant", "Statut"}) {
            JLabel l = new JLabel(col);
            l.setFont(Theme.FONT_SMALL_BOLD);
            l.setForeground(Theme.TEXT_SECONDARY);
            row.add(l);
        }
        return row;
    }

    // ─── Ligne paiement ────────────────────────────────────────────────
    private JPanel buildLigne(Paiement p) {
        JPanel row = new JPanel(new GridLayout(1, 6, 8, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            new EmptyBorder(10, 12, 10, 12)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        // Membre
        String nom = membreService.listerMembres().stream()
            .filter(m -> m.getId().equals(p.getMembreId()))
            .findFirst().map(Membre::getNomComplet).orElse("—");
        row.add(cell(nom, Theme.TEXT_PRIMARY, Font.BOLD));

        // Type
        String type = p.getType() == Paiement.Type.INSCRIPTION_ANNUELLE
            ? "Inscription" : "Mensuelle";
        row.add(cell(type, Theme.TEXT_SECONDARY, Font.PLAIN));

        // Activité
        String act = p.getActiviteId() == null ? "—"
            : activiteService.trouverParId(p.getActiviteId())
                .map(Activite::getNom).orElse("—");
        row.add(cell(act, Theme.TEXT_SECONDARY, Font.PLAIN));

        // Mois
        String moisStr = Month.of(p.getMois())
            .getDisplayName(TextStyle.FULL, Locale.FRENCH) + " " + p.getAnnee();
        row.add(cell(moisStr, Theme.TEXT_SECONDARY, Font.PLAIN));

        // Montant
        row.add(cell(String.format("%.0f DT", p.getMontant()), Theme.TEXT_PRIMARY, Font.BOLD));

        // Statut + bouton toggle
        JPanel statutCell = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        statutCell.setOpaque(false);
        JLabel badge = buildBadge(p.isPaye());
        if (p.isPaye() && p.getMethode() != null) {
            badge.setToolTipText("Réglé via " + p.getMethode().getLibelle());
        }
        statutCell.add(badge);
        statutCell.add(buildToggle(p));
        row.add(statutCell);

        return row;
    }

    private JLabel cell(String text, Color color, int style) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", style, 12));
        l.setForeground(color);
        return l;
    }

    private JLabel buildBadge(boolean paye) {
        String txt = paye ? "  Paye  " : "  Non paye  ";
        JLabel badge = new JLabel(txt) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(paye ? Theme.SUCCESS_LIGHT : Theme.DANGER_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(paye ? Theme.SUCCESS : Theme.DANGER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setFont(Theme.FONT_SMALL_BOLD);
        badge.setForeground(paye ? Theme.SUCCESS : Theme.DANGER);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        return badge;
    }

    private JButton buildToggle(Paiement p) {
        Color c = p.isPaye() ? Theme.DANGER : Theme.SUCCESS;
        String txt = p.isPaye() ? "Annuler" : "Marquer payé";
        JButton btn = new JButton(txt);
        btn.setFont(Theme.FONT_SMALL);
        btn.setForeground(c);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(c));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            if (p.isPaye()) paiementService.marquerNonPaye(p.getId());
            else            paiementService.marquerPaye(p.getId());
            charger();
        });
        return btn;
    }
}
