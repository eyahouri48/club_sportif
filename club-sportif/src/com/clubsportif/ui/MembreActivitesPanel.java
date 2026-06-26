package com.clubsportif.ui;

import com.clubsportif.model.Activite;
import com.clubsportif.service.ActiviteService;
import com.clubsportif.service.InscriptionService;
import com.clubsportif.ui.components.*;
import com.clubsportif.util.IconFactory;
import com.clubsportif.util.Session;
import com.clubsportif.util.Theme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

public class MembreActivitesPanel extends JPanel {

    private final ActiviteService activiteService;
    private final InscriptionService inscriptionService;
    private StyledTextField searchField;
    private JComboBox<String> jourFilter;
    private JCheckBox dispoFilter;
    private ScrollableWrapPanel cardsContainer;
    private JLabel countLabel;
    private List<Activite> activites;
    private List<Activite> activitesFiltrees;
    private ActivityCard selectedCard;

    private static final String[] JOURS = {
        "Tous les jours","Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi","Dimanche"
    };

    public MembreActivitesPanel(ActiviteService activiteService, InscriptionService inscriptionService) {
        this.activiteService   = activiteService;
        this.inscriptionService = inscriptionService;
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_MAIN);
        buildHeader();
        buildCards();
        buildFooter();
        SwingUtilities.invokeLater(this::chargerDonnees);
    }

    private void buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(20, 24, 16, 24)));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel title = new JLabel("  Activites Disponibles");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.PRIMARY);
        title.setIcon(IconFactory.create("activity", 24, Theme.PRIMARY));

        countLabel = new JLabel("Chargement...");
        countLabel.setFont(Theme.FONT_SMALL);
        countLabel.setForeground(Theme.TEXT_SECONDARY);

        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(countLabel, BorderLayout.EAST);
        header.add(titleRow);
        header.add(Box.createVerticalStrut(14));

        JPanel filtresRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filtresRow.setOpaque(false);

        JLabel jourLbl = new JLabel("Jour :");
        jourLbl.setFont(Theme.FONT_SMALL_BOLD);
        jourLbl.setForeground(Theme.TEXT_SECONDARY);

        jourFilter = new JComboBox<>(JOURS);
        jourFilter.setFont(Theme.FONT_BODY);
        jourFilter.setPreferredSize(new Dimension(160, 38));
        jourFilter.addActionListener(e -> appliquerFiltres());

        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 30));
        sep.setForeground(Theme.BORDER);

        dispoFilter = new JCheckBox("Places disponibles seulement");
        dispoFilter.setOpaque(false);
        dispoFilter.setFont(Theme.FONT_BODY);
        dispoFilter.setForeground(Theme.TEXT_PRIMARY);
        dispoFilter.addActionListener(e -> appliquerFiltres());

        JSeparator sep2 = new JSeparator(JSeparator.VERTICAL);
        sep2.setPreferredSize(new Dimension(1, 30));
        sep2.setForeground(Theme.BORDER);

        searchField = new StyledTextField("Rechercher une activite...");
        searchField.setPreferredSize(new Dimension(240, 38));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { appliquerFiltres(); }
            public void removeUpdate(DocumentEvent e)  { appliquerFiltres(); }
            public void changedUpdate(DocumentEvent e) { appliquerFiltres(); }
        });

        filtresRow.add(jourLbl);
        filtresRow.add(jourFilter);
        filtresRow.add(sep);
        filtresRow.add(dispoFilter);
        filtresRow.add(sep2);
        filtresRow.add(searchField);
        header.add(filtresRow);
        add(header, BorderLayout.NORTH);
    }

    private void buildCards() {
        cardsContainer = new ScrollableWrapPanel(new WrapLayout(FlowLayout.LEFT, 20, 20));
        cardsContainer.setBackground(Theme.BG_MAIN);
        cardsContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scroll = new JScrollPane(cardsContainer);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_MAIN);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(28);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        add(scroll, BorderLayout.CENTER);
    }

    private void buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(12, 24, 12, 24)));

        JLabel hint = new JLabel("  Cliquez pour selectionner, puis S'inscrire");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_MUTED);
        hint.setIcon(IconFactory.create("info", 14, Theme.TEXT_MUTED));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        StyledButton refreshBtn = new StyledButton("Actualiser", StyledButton.Style.OUTLINE);
        refreshBtn.addActionListener(e -> chargerDonnees());
        StyledButton inscrireBtn = new StyledButton("S'inscrire", StyledButton.Style.PRIMARY);
        inscrireBtn.addActionListener(e -> sInscrire());
        // Spec §3 : les boutons doivent rester visibles en permanence (pas au survol).
        refreshBtn.setVisible(true);
        inscrireBtn.setVisible(true);
        refreshBtn.setOpaque(true);
        inscrireBtn.setOpaque(true);
        btnPanel.add(refreshBtn);
        btnPanel.add(inscrireBtn);

        footer.add(hint, BorderLayout.WEST);
        footer.add(btnPanel, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    public void chargerDonnees() {
        activites = activiteService.listerActivites();
        appliquerFiltres();
    }

    private void appliquerFiltres() {
        int jourIndex = jourFilter.getSelectedIndex();
        boolean dispo = dispoFilter.isSelected();
        String search = searchField.getText().toLowerCase().trim();

        activitesFiltrees = activites.stream()
            .filter(a -> {
                if (jourIndex > 0 && !a.getJour().equalsIgnoreCase(JOURS[jourIndex])) return false;
                if (dispo && activiteService.estComplete(a.getId())) return false;
                if (!search.isEmpty())
                    return a.getNom().toLowerCase().contains(search)
                        || (a.getDescription() != null && a.getDescription().toLowerCase().contains(search))
                        || a.getJour().toLowerCase().contains(search);
                return true;
            })
            .collect(Collectors.toList());
        rebuildCards();
    }

    private void rebuildCards() {
        cardsContainer.removeAll();
        selectedCard = null;
        int total = activitesFiltrees.size();
        countLabel.setText(total + " activite" + (total > 1 ? "s" : "") + " trouvee" + (total > 1 ? "s" : ""));

        if (activitesFiltrees.isEmpty()) {
            JPanel empty = new JPanel(new GridBagLayout());
            empty.setOpaque(false);
            JLabel lbl = new JLabel("Aucune activite ne correspond aux filtres");
            lbl.setFont(Theme.FONT_BODY);
            lbl.setForeground(Theme.TEXT_SECONDARY);
            empty.add(lbl);
            cardsContainer.add(empty);
        } else {
            for (Activite a : activitesFiltrees) {
                int places = activiteService.getPlacesRestantes(a.getId());
                ActivityCard card = new ActivityCard(a, places);
                card.addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        if (selectedCard != null) selectedCard.setSelected(false);
                        card.setSelected(true);
                        selectedCard = card;
                        if (e.getClickCount() == 2) sInscrire();
                    }
                });
                cardsContainer.add(card);
            }
        }
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private void sInscrire() {
        if (selectedCard == null) {
            DialogUtils.erreur(this, "Veuillez selectionner une activite.");
            return;
        }
        Activite activite = selectedCard.getActivite();
        String membreId   = Session.getInstance().getMembreConnecte().getId();
        if (DialogUtils.confirmer(this,
                "Voulez-vous vous inscrire a \"" + activite.getNom() + "\" ?\n"
                + "Jour : " + activite.getJour() + "\n"
                + "Horaires : " + activite.getHoraires(),
                "Confirmer l'inscription")) {
            try {
                inscriptionService.inscrire(membreId, activite.getId());
                chargerDonnees();
                DialogUtils.succes(this,
                    "Inscription envoyee avec succes !\n\nStatut : En attente de validation par l'administrateur.");
            } catch (IllegalArgumentException ex) {
                DialogUtils.erreur(this, ex.getMessage());
            }
        }
    }

    // WrapLayout for card grid
    static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override public Dimension preferredLayoutSize(Container t) { return layoutSize(t, true); }
        @Override public Dimension minimumLayoutSize(Container t) {
            Dimension d = layoutSize(t, false); d.width -= (getHgap() + 1); return d;
        }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int maxW = target.getSize().width;
                if (maxW == 0) maxW = Integer.MAX_VALUE;
                Insets ins = target.getInsets();
                maxW -= ins.left + ins.right + getHgap() * 2;
                Dimension dim = new Dimension(0, 0);
                int rowW = 0, rowH = 0;
                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component m = target.getComponent(i);
                    if (!m.isVisible()) continue;
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    if (rowW + d.width > maxW) { addRow(dim, rowW, rowH); rowW = 0; rowH = 0; }
                    if (rowW != 0) rowW += getHgap();
                    rowW += d.width;
                    rowH = Math.max(rowH, d.height);
                }
                addRow(dim, rowW, rowH);
                dim.width  += ins.left + ins.right + getHgap() * 2;
                dim.height += ins.top + ins.bottom + getVgap() * 2;
                return dim;
            }
        }
        private void addRow(Dimension dim, int rowW, int rowH) {
            dim.width = Math.max(dim.width, rowW);
            if (dim.height > 0) dim.height += getVgap();
            dim.height += rowH;
        }
    }
}
