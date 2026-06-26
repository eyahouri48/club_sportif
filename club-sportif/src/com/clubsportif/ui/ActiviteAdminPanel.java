package com.clubsportif.ui;

import com.clubsportif.model.Activite;
import com.clubsportif.service.ActiviteService;
import com.clubsportif.service.InscriptionService;
import com.clubsportif.ui.components.*;
import com.clubsportif.util.IconFactory;
import com.clubsportif.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

public class ActiviteAdminPanel extends JPanel {

    private final ActiviteService    activiteService;
    private final InscriptionService inscriptionService;
    private final StyledTextField    searchField;
    private ScrollableWrapPanel cardsContainer;
    private JLabel countLabel;
    private List<Activite> activites;
    private ActivityCard selectedCard;

    private static final String[] JOURS_FILTER = {
        "Tous","Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi","Dimanche"
    };
    private JComboBox<String> jourFilter;

    public ActiviteAdminPanel(ActiviteService activiteService,
                               InscriptionService inscriptionService) {
        this.activiteService    = activiteService;
        this.inscriptionService = inscriptionService;
        searchField = new StyledTextField("Rechercher...");
        jourFilter  = new JComboBox<>(JOURS_FILTER);

        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_MAIN);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildGrid(),   BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
        SwingUtilities.invokeLater(this::chargerDonnees);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Theme.BORDER);
                g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            }
        };
        header.setOpaque(false);
        header.setLayout(new BorderLayout(0, 14));
        header.setBorder(new EmptyBorder(20, 24, 16, 24));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JPanel titleLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleLeft.setOpaque(false);
        JLabel icon = new JLabel(IconFactory.create("activity", 28, Theme.PRIMARY));
        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);
        JLabel title = new JLabel("Gestion des Activites");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        countLabel = new JLabel("Chargement...");
        countLabel.setFont(Theme.FONT_SMALL);
        countLabel.setForeground(Theme.TEXT_SECONDARY);
        titleStack.add(title);
        titleStack.add(countLabel);
        titleLeft.add(icon);
        titleLeft.add(titleStack);
        titleRow.add(titleLeft, BorderLayout.WEST);

        StyledButton addBtn = new StyledButton("+ Nouvelle activite", StyledButton.Style.PRIMARY);
        addBtn.addActionListener(e -> ajouterActivite());
        JPanel addWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        addWrap.setOpaque(false);
        addWrap.add(addBtn);
        titleRow.add(addWrap, BorderLayout.EAST);
        header.add(titleRow, BorderLayout.NORTH);

        JPanel filtres = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filtres.setOpaque(false);
        searchField.setPreferredSize(new Dimension(240, 38));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrer(); }
            public void removeUpdate(DocumentEvent e) { filtrer(); }
            public void changedUpdate(DocumentEvent e) { filtrer(); }
        });
        JLabel jourLbl = new JLabel("Jour :");
        jourLbl.setFont(Theme.FONT_SMALL_BOLD);
        jourLbl.setForeground(Theme.TEXT_SECONDARY);
        jourFilter.setFont(Theme.FONT_BODY);
        jourFilter.setPreferredSize(new Dimension(150, 38));
        jourFilter.addActionListener(e -> filtrer());
        filtres.add(searchField);
        filtres.add(jourLbl);
        filtres.add(jourFilter);
        header.add(filtres, BorderLayout.SOUTH);
        return header;
    }

    private JScrollPane buildGrid() {
        cardsContainer = new ScrollableWrapPanel(
            new MembreActivitesPanel.WrapLayout(FlowLayout.LEFT, 20, 20));
        cardsContainer.setBackground(Theme.BG_MAIN);
        cardsContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        JScrollPane scroll = new JScrollPane(cardsContainer);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_MAIN);
        // FIX #6 : BLIT_SCROLL_MODE laisse des "fantômes" de cartes non-opaques au
        // défilement. BACKINGSTORE force un repaint complet → plus de cartes collées.
        scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(28);
        return scroll;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
            new EmptyBorder(12, 24, 12, 24)));

        JLabel hint = new JLabel("  Selectionnez une carte, double-clic pour modifier");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_MUTED);
        hint.setIcon(IconFactory.create("info", 14, Theme.TEXT_MUTED));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        StyledButton refreshBtn = new StyledButton("Actualiser",  StyledButton.Style.OUTLINE);
        StyledButton editBtn    = new StyledButton("Modifier",    StyledButton.Style.OUTLINE);
        StyledButton deleteBtn  = new StyledButton("Supprimer",  StyledButton.Style.DANGER);
        refreshBtn.addActionListener(e -> chargerDonnees());
        editBtn.addActionListener(e -> modifierActivite());
        deleteBtn.addActionListener(e -> supprimerActivite());
        btnPanel.add(refreshBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        footer.add(hint, BorderLayout.WEST);
        footer.add(btnPanel, BorderLayout.EAST);
        return footer;
    }

    public void chargerDonnees() {
        activites = activiteService.listerActivites();
        filtrer();
    }

    private void filtrer() {
        if (activites == null) return;
        String q = searchField.getText().toLowerCase().trim();
        int jourIdx = jourFilter.getSelectedIndex();
        List<Activite> filtrees = activites.stream()
            .filter(a -> {
                if (jourIdx > 0 && !a.getJour().equalsIgnoreCase(JOURS_FILTER[jourIdx])) return false;
                if (!q.isEmpty())
                    return a.getNom().toLowerCase().contains(q)
                        || (a.getDescription() != null && a.getDescription().toLowerCase().contains(q));
                return true;
            }).collect(Collectors.toList());
        afficherCards(filtrees);
    }

    private void afficherCards(List<Activite> liste) {
        cardsContainer.removeAll();
        selectedCard = null;
        countLabel.setText(liste.size() + " activite" + (liste.size() > 1 ? "s" : ""));

        if (liste.isEmpty()) {
            JPanel empty = new JPanel(new GridBagLayout());
            empty.setOpaque(false);
            JLabel lbl = new JLabel("Aucune activite trouvee");
            lbl.setFont(Theme.FONT_BODY);
            lbl.setForeground(Theme.TEXT_SECONDARY);
            empty.add(lbl);
            cardsContainer.add(empty);
        } else {
            for (Activite a : liste) {
                int places = activiteService.getPlacesRestantes(a.getId());
                ActivityCard card = new ActivityCard(a, places);
                card.addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        if (selectedCard != null) selectedCard.setSelected(false);
                        card.setSelected(true);
                        selectedCard = card;
                        if (e.getClickCount() == 2) modifierActivite();
                    }
                });
                cardsContainer.add(card);
            }
        }
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private void ajouterActivite() {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        ActiviteFormDialog d = new ActiviteFormDialog(parent, activiteService, null);
        d.setVisible(true);
        if (d.isSaved()) chargerDonnees();
    }

    private void modifierActivite() {
        if (selectedCard == null) { DialogUtils.erreur(this, "Selectionnez une activite."); return; }
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        ActiviteFormDialog d = new ActiviteFormDialog(parent, activiteService, selectedCard.getActivite());
        d.setVisible(true);
        if (d.isSaved()) chargerDonnees();
    }

    private void supprimerActivite() {
        if (selectedCard == null) { DialogUtils.erreur(this, "Selectionnez une activite."); return; }
        Activite a = selectedCard.getActivite();
        if (DialogUtils.confirmer(this, "Supprimer \"" + a.getNom() + "\" ?", "Confirmer")) {
            try { activiteService.supprimerActivite(a.getId()); chargerDonnees(); }
            catch (Exception ex) { DialogUtils.erreur(this, ex.getMessage()); }
        }
    }
}
