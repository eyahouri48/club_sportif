package com.clubsportif.ui;

import com.clubsportif.model.Activite;
import com.clubsportif.model.Inscription;
import com.clubsportif.model.StatutInscription;
import com.clubsportif.service.ActiviteService;
import com.clubsportif.service.InscriptionService;
import com.clubsportif.service.MembreService;
import com.clubsportif.ui.components.StyledButton;
import com.clubsportif.util.IconFactory;
import com.clubsportif.util.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ClassementPanel extends JPanel {

    private final ActiviteService activiteService;
    private final InscriptionService inscriptionService;
    private final MembreService membreService;
    private JPanel planningGrid, topActivitesPanel, tauxPanel, topMembresPanel;
    private JLabel lastUpdateLabel;

    private static final String[] JOURS_ORDRE = {
        "Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi","Dimanche"
    };
    private static final Color[] JOUR_COLORS = {
        new Color(8,145,178), new Color(5,150,105), new Color(217,119,6),
        new Color(139,92,246), new Color(220,38,38), new Color(20,184,166), new Color(14,165,233)
    };

    public ClassementPanel(MembreService membreService, InscriptionService inscriptionService) {
        this(null, inscriptionService, membreService);
    }

    public ClassementPanel(ActiviteService activiteService,
                           InscriptionService inscriptionService,
                           MembreService membreService) {
        this.activiteService   = activiteService;
        this.inscriptionService = inscriptionService;
        this.membreService     = membreService;
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_MAIN);
        buildHeader();
        buildContent();
    }

    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0, Theme.BORDER),
            BorderFactory.createEmptyBorder(20,24,16,24)));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("  Statistiques & Planning");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.PRIMARY);
        title.setIcon(IconFactory.create("stats", 24, Theme.PRIMARY));

        lastUpdateLabel = new JLabel("Chargement des donnees...");
        lastUpdateLabel.setFont(Theme.FONT_SMALL);
        lastUpdateLabel.setForeground(Theme.TEXT_SECONDARY);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(3));
        titleBlock.add(lastUpdateLabel);

        StyledButton refreshBtn = new StyledButton("Actualiser", StyledButton.Style.OUTLINE);
        refreshBtn.addActionListener(e -> chargerDonnees());

        header.add(titleBlock, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    private void buildContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.BG_MAIN);
        content.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        content.add(makeSectionTitle("Planning de la semaine", "calendar"));
        content.add(Box.createVerticalStrut(10));
        planningGrid = new JPanel(new GridLayout(1, 7, 10, 0));
        planningGrid.setOpaque(false);
        planningGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        planningGrid.setAlignmentX(LEFT_ALIGNMENT);
        content.add(planningGrid);
        content.add(Box.createVerticalStrut(24));

        JPanel twoCol = new JPanel(new GridLayout(1, 2, 16, 0));
        twoCol.setOpaque(false);
        twoCol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        twoCol.setAlignmentX(LEFT_ALIGNMENT);

        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);
        leftCol.add(makeSectionTitle("Top 5 activites populaires", "medal"));
        leftCol.add(Box.createVerticalStrut(8));
        topActivitesPanel = new JPanel();
        topActivitesPanel.setLayout(new BoxLayout(topActivitesPanel, BoxLayout.Y_AXIS));
        topActivitesPanel.setOpaque(false);
        topActivitesPanel.setAlignmentX(LEFT_ALIGNMENT);
        leftCol.add(topActivitesPanel);

        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);
        rightCol.add(makeSectionTitle("Membres les plus actifs", "members"));
        rightCol.add(Box.createVerticalStrut(8));
        topMembresPanel = new JPanel();
        topMembresPanel.setLayout(new BoxLayout(topMembresPanel, BoxLayout.Y_AXIS));
        topMembresPanel.setOpaque(false);
        topMembresPanel.setAlignmentX(LEFT_ALIGNMENT);
        rightCol.add(topMembresPanel);

        twoCol.add(leftCol);
        twoCol.add(rightCol);
        content.add(twoCol);
        content.add(Box.createVerticalStrut(24));

        content.add(makeSectionTitle("Taux de remplissage par activite", "stats"));
        content.add(Box.createVerticalStrut(10));
        tauxPanel = new JPanel();
        tauxPanel.setLayout(new BoxLayout(tauxPanel, BoxLayout.Y_AXIS));
        tauxPanel.setOpaque(false);
        tauxPanel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(tauxPanel);
        content.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_MAIN);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8,0));
        add(scroll, BorderLayout.CENTER);

        SwingUtilities.invokeLater(this::chargerDonnees);
    }

    private JLabel makeSectionTitle(String text, String iconName) {
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(Theme.TEXT_PRIMARY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setIcon(IconFactory.create(iconName, 18, Theme.PRIMARY));
        return lbl;
    }

    public void chargerDonnees() {
        if (activiteService == null) { lastUpdateLabel.setText("Service non disponible"); return; }

        new SwingWorker<Object[], Void>() {
            List<Activite> activites;
            List<Inscription> inscriptions;
            Map<String, Long> nbParActivite;
            Map<String, Long> nbParMembre;

            @Override protected Object[] doInBackground() {
                activites    = activiteService.listerActivites();
                inscriptions = inscriptionService.listerToutesInscriptions();
                nbParActivite = inscriptions.stream()
                    .filter(i -> i.getStatut() == StatutInscription.ACCEPTEE)
                    .collect(Collectors.groupingBy(Inscription::getActiviteId, Collectors.counting()));
                nbParMembre = inscriptions.stream()
                    .filter(i -> i.getStatut() == StatutInscription.ACCEPTEE)
                    .collect(Collectors.groupingBy(Inscription::getMembreId, Collectors.counting()));
                return null;
            }

            @Override protected void done() {
                updatePlanning();
                updateTopActivites();
                updateTopMembres();
                updateTauxRemplissage();
                lastUpdateLabel.setText("Mis a jour : " +
                    new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()));
            }

            private void updatePlanning() {
                planningGrid.removeAll();
                Map<String, List<Activite>> parJour = new LinkedHashMap<>();
                for (String j : JOURS_ORDRE) parJour.put(j, new ArrayList<>());
                for (Activite a : activites) {
                    String jour = capitalise(a.getJour());
                    parJour.computeIfAbsent(jour, k -> new ArrayList<>()).add(a);
                }
                int idx = 0;
                for (String jour : JOURS_ORDRE) {
                    List<Activite> list = parJour.getOrDefault(jour, Collections.emptyList());
                    planningGrid.add(buildJourCard(jour, list, JOUR_COLORS[idx % JOUR_COLORS.length]));
                    idx++;
                }
                planningGrid.revalidate(); planningGrid.repaint();
            }

            private JPanel buildJourCard(String jour, List<Activite> list, Color color) {
                JPanel card = new JPanel() {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(Color.WHITE);
                        g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                        g2.setColor(color);
                        g2.fillRoundRect(0,0,getWidth()-1,30,12,12);
                        g2.fillRect(0,18,getWidth()-1,12);
                        g2.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),60));
                        g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                        // Jour label
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                        FontMetrics fm = g2.getFontMetrics();
                        String j = jour.substring(0,3).toUpperCase();
                        g2.drawString(j, (getWidth()-fm.stringWidth(j))/2, 20);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                card.setOpaque(false);
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                card.setBorder(BorderFactory.createEmptyBorder(36, 8, 10, 8));

                if (list.isEmpty()) {
                    JLabel none = new JLabel("Aucune", SwingConstants.CENTER);
                    none.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                    none.setForeground(Theme.TEXT_MUTED);
                    none.setAlignmentX(CENTER_ALIGNMENT);
                    card.add(Box.createVerticalGlue());
                    card.add(none);
                    card.add(Box.createVerticalGlue());
                } else {
                    for (Activite a : list) {
                        JLabel act = new JLabel(truncate(a.getNom(), 12));
                        act.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                        act.setForeground(Theme.TEXT_PRIMARY);
                        act.setAlignmentX(LEFT_ALIGNMENT);
                        card.add(act);
                        JLabel time = new JLabel("  " + a.getHoraires());
                        time.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        time.setForeground(color.darker());
                        time.setAlignmentX(LEFT_ALIGNMENT);
                        card.add(time);
                        card.add(Box.createVerticalStrut(4));
                    }
                }
                return card;
            }

            private void updateTopActivites() {
                topActivitesPanel.removeAll();
                List<Activite> sorted = activites.stream()
                    .sorted((a, b) -> Long.compare(
                        nbParActivite.getOrDefault(b.getId(), 0L),
                        nbParActivite.getOrDefault(a.getId(), 0L)))
                    .limit(5).collect(Collectors.toList());
                long maxV = sorted.stream().mapToLong(a -> nbParActivite.getOrDefault(a.getId(), 0L)).max().orElse(1);
                String[] ranks = {"1er","2e","3e","4e","5e"};
                int rank = 0;
                for (Activite a : sorted) {
                    long nb = nbParActivite.getOrDefault(a.getId(), 0L);
                    topActivitesPanel.add(buildRankRow(ranks[rank++], a.getNom(), nb + " inscrit(s)", nb, maxV, Theme.PRIMARY));
                    topActivitesPanel.add(Box.createVerticalStrut(6));
                }
                if (sorted.isEmpty()) {
                    JLabel none = new JLabel("Aucune donnee");
                    none.setFont(Theme.FONT_SMALL); none.setForeground(Theme.TEXT_MUTED);
                    topActivitesPanel.add(none);
                }
                topActivitesPanel.revalidate(); topActivitesPanel.repaint();
            }

            private void updateTopMembres() {
                topMembresPanel.removeAll();
                var membres = membreService.listerMembres();
                var sorted = membres.stream()
                    .sorted((a,b) -> Long.compare(
                        nbParMembre.getOrDefault(b.getId(), 0L),
                        nbParMembre.getOrDefault(a.getId(), 0L)))
                    .limit(5).collect(Collectors.toList());
                long maxV = sorted.stream().mapToLong(m -> nbParMembre.getOrDefault(m.getId(), 0L)).max().orElse(1);
                String[] ranks = {"1er","2e","3e","4e","5e"};
                int rank = 0;
                for (var m : sorted) {
                    long nb = nbParMembre.getOrDefault(m.getId(), 0L);
                    topMembresPanel.add(buildRankRow(ranks[rank++], m.getNomComplet(), nb + " activite(s)", nb, maxV, Theme.ACCENT));
                    topMembresPanel.add(Box.createVerticalStrut(6));
                }
                if (sorted.isEmpty()) {
                    JLabel none = new JLabel("Aucune donnee");
                    none.setFont(Theme.FONT_SMALL); none.setForeground(Theme.TEXT_MUTED);
                    topMembresPanel.add(none);
                }
                topMembresPanel.revalidate(); topMembresPanel.repaint();
            }

            private void updateTauxRemplissage() {
                tauxPanel.removeAll();
                var sorted = activites.stream()
                    .filter(a -> a.getCapaciteMax() > 0)
                    .sorted((a,b) -> {
                        double ra = (double) nbParActivite.getOrDefault(a.getId(),0L) / a.getCapaciteMax();
                        double rb = (double) nbParActivite.getOrDefault(b.getId(),0L) / b.getCapaciteMax();
                        return Double.compare(rb, ra);
                    }).limit(8).collect(Collectors.toList());
                for (Activite a : sorted) {
                    long nb = nbParActivite.getOrDefault(a.getId(), 0L);
                    int cap = a.getCapaciteMax();
                    double ratio = (double) nb / cap;
                    Color barColor = ratio >= 0.9 ? Theme.DANGER : ratio >= 0.6 ? Theme.WARNING : Theme.SUCCESS;
                    tauxPanel.add(buildTauxRow(a.getNom(), nb, cap, ratio, barColor));
                    tauxPanel.add(Box.createVerticalStrut(8));
                }
                if (sorted.isEmpty()) {
                    JLabel none = new JLabel("Aucune activite avec capacite definie");
                    none.setFont(Theme.FONT_SMALL); none.setForeground(Theme.TEXT_MUTED);
                    tauxPanel.add(none);
                }
                tauxPanel.revalidate(); tauxPanel.repaint();
            }
        }.execute();
    }

    private JPanel buildRankRow(String rank, String name, String detail, long value, long max, Color color) {
        JPanel row = new JPanel(new BorderLayout(8,0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel rankLbl = new JLabel(rank);
        rankLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rankLbl.setForeground(color);
        rankLbl.setPreferredSize(new Dimension(32,28));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        JLabel nameLbl = new JLabel(truncate(name, 28));
        nameLbl.setFont(Theme.FONT_SMALL_BOLD);
        nameLbl.setForeground(Theme.TEXT_PRIMARY);
        JLabel detLbl = new JLabel(detail);
        detLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        detLbl.setForeground(Theme.TEXT_SECONDARY);
        info.add(nameLbl);
        info.add(detLbl);

        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(226,232,240));
                g2.fillRoundRect(0, getHeight()/2-3, getWidth(), 6, 6,6);
                if (max > 0 && value > 0) {
                    int w = (int)((double)value/max * getWidth());
                    g2.setPaint(new GradientPaint(0,0,color,w,0,color.brighter()));
                    g2.fillRoundRect(0, getHeight()/2-3, w, 6, 6,6);
                }
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(80, 28));
        row.add(rankLbl, BorderLayout.WEST);
        row.add(info, BorderLayout.CENTER);
        row.add(bar, BorderLayout.EAST);
        return row;
    }

    private JPanel buildTauxRow(String name, long nb, int cap, double ratio, Color barColor) {
        JPanel row = new JPanel(new BorderLayout(10,0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.setAlignmentX(LEFT_ALIGNMENT);
        JLabel nameLbl = new JLabel(truncate(name, 22));
        nameLbl.setFont(Theme.FONT_SMALL_BOLD);
        nameLbl.setForeground(Theme.TEXT_PRIMARY);
        nameLbl.setPreferredSize(new Dimension(160, 20));
        String pct = String.format("%.0f%%  (%d/%d)", ratio*100, nb, cap);
        JLabel pctLbl = new JLabel(pct);
        pctLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        pctLbl.setForeground(barColor);
        pctLbl.setPreferredSize(new Dimension(80, 20));
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(226,232,240));
                g2.fillRoundRect(0, getHeight()/2-4, getWidth(), 8, 8,8);
                if (ratio > 0) {
                    int w = (int)(ratio * getWidth());
                    g2.setColor(barColor);
                    g2.fillRoundRect(0, getHeight()/2-4, w, 8, 8,8);
                }
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        row.add(nameLbl, BorderLayout.WEST);
        row.add(bar, BorderLayout.CENTER);
        row.add(pctLbl, BorderLayout.EAST);
        return row;
    }

    private static String capitalise(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
    private static String truncate(String s, int max) {
        if (s == null || s.length() <= max) return s == null ? "" : s;
        return s.substring(0, max-1) + "...";
    }
}
