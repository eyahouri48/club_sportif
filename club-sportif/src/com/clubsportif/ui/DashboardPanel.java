package com.clubsportif.ui;

import com.clubsportif.model.*;
import com.clubsportif.service.*;
import com.clubsportif.ui.components.*;
import com.clubsportif.util.IconFactory;
import com.clubsportif.util.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardPanel extends JPanel {

    private final ActiviteService activiteService;
    private final InscriptionService inscriptionService;
    private final MembreService membreService;

    private JLabel totalMembresVal, totalActivitesVal, inscriptionsEnAttenteVal, activitesCompletesVal;
    private JPanel recentPanel;
    private PopularChartPanel chartPanel;

    public DashboardPanel(ActiviteService activiteService,
                          InscriptionService inscriptionService,
                          MembreService membreService) {
        this.activiteService = activiteService;
        this.inscriptionService = inscriptionService;
        this.membreService = membreService;

        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_MAIN);
        add(buildHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(24, 24, 24, 24));

        // KPI cards
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiRow.setOpaque(false);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        kpiRow.setAlignmentX(LEFT_ALIGNMENT);

        totalMembresVal          = new JLabel("--");
        totalActivitesVal        = new JLabel("--");
        inscriptionsEnAttenteVal = new JLabel("--");
        activitesCompletesVal    = new JLabel("--");

        kpiRow.add(createKpiCard("members", "Membres actifs", totalMembresVal,
            Theme.PRIMARY, Theme.PRIMARY_LIGHT));
        kpiRow.add(createKpiCard("activity", "Activites", totalActivitesVal,
            Theme.ACCENT, Theme.ACCENT_LIGHT));
        kpiRow.add(createKpiCard("clock", "En attente", inscriptionsEnAttenteVal,
            Theme.WARNING, Theme.WARNING_LIGHT));
        kpiRow.add(createKpiCard("warning", "Completes", activitesCompletesVal,
            Theme.DANGER, Theme.DANGER_LIGHT));

        content.add(kpiRow);
        content.add(Box.createVerticalStrut(24));

        // Two columns
        JPanel twoCol = new JPanel(new GridLayout(1, 2, 20, 0));
        twoCol.setOpaque(false);
        twoCol.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        twoCol.setAlignmentX(LEFT_ALIGNMENT);

        // Chart
        CardPanel chartCard = new CardPanel();
        chartCard.setLayout(new BorderLayout(0, 12));
        JLabel chartTitle = new JLabel("  Activites populaires");
        chartTitle.setFont(Theme.FONT_SUBTITLE);
        chartTitle.setForeground(Theme.TEXT_PRIMARY);
        chartTitle.setIcon(IconFactory.create("medal", 18, Theme.PRIMARY));
        chartCard.add(chartTitle, BorderLayout.NORTH);
        chartPanel = new PopularChartPanel();
        chartCard.add(chartPanel, BorderLayout.CENTER);
        twoCol.add(chartCard);

        // Recent inscriptions
        CardPanel recentCard = new CardPanel();
        recentCard.setLayout(new BorderLayout(0, 12));
        JLabel recentTitle = new JLabel("  Inscriptions recentes");
        recentTitle.setFont(Theme.FONT_SUBTITLE);
        recentTitle.setForeground(Theme.TEXT_PRIMARY);
        recentTitle.setIcon(IconFactory.create("inscription", 18, Theme.PRIMARY));
        recentCard.add(recentTitle, BorderLayout.NORTH);
        recentPanel = new JPanel();
        recentPanel.setLayout(new BoxLayout(recentPanel, BoxLayout.Y_AXIS));
        recentPanel.setOpaque(false);
        JScrollPane recentScroll = new JScrollPane(recentPanel);
        recentScroll.setBorder(null);
        recentScroll.getViewport().setOpaque(false);
        recentCard.add(recentScroll, BorderLayout.CENTER);
        twoCol.add(recentCard);

        content.add(twoCol);
        content.add(Box.createVerticalGlue());

        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setBorder(null);
        mainScroll.getViewport().setBackground(Theme.BG_MAIN);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0)) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Theme.BORDER);
                g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(18, 24, 16, 24));

        JPanel leftStack = new JPanel();
        leftStack.setLayout(new BoxLayout(leftStack, BoxLayout.Y_AXIS));
        leftStack.setOpaque(false);

        JLabel title = new JLabel("Tableau de bord");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH));
        JLabel dateLbl = new JLabel(dateStr.substring(0,1).toUpperCase() + dateStr.substring(1));
        dateLbl.setFont(Theme.FONT_SMALL);
        dateLbl.setForeground(Theme.TEXT_SECONDARY);
        dateLbl.setIcon(IconFactory.create("calendar", 14, Theme.TEXT_MUTED));

        leftStack.add(title);
        leftStack.add(Box.createVerticalStrut(3));
        leftStack.add(dateLbl);

        header.add(leftStack, BorderLayout.WEST);

        StyledButton refreshBtn = new StyledButton("Actualiser", StyledButton.Style.OUTLINE);
        refreshBtn.addActionListener(e -> chargerDonnees());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(refreshBtn);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel createKpiCard(String iconName, String label, JLabel valueLabel,
                                  Color color, Color bgColor) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,8));
                g2.fillRoundRect(2, 3, getWidth()-2, getHeight()-2, Theme.CARD_RADIUS, Theme.CARD_RADIUS);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-3, getHeight()-3, Theme.CARD_RADIUS, Theme.CARD_RADIUS);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth()-3, 4, 2, 2);
                g2.setColor(bgColor);
                g2.fillRoundRect(getWidth()-60, 20, 40, 40, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setLayout(null);
        card.setOpaque(false);

        // Icon
        JLabel iconLbl = new JLabel(IconFactory.create(iconName, 22, color));
        iconLbl.setBounds(card.getWidth()-55, 25, 30, 30);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(color);
        valueLabel.setBounds(16, 22, 120, 40);

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(Theme.FONT_SMALL);
        lblLabel.setForeground(Theme.TEXT_SECONDARY);
        lblLabel.setBounds(16, 60, 180, 18);

        card.add(valueLabel);
        card.add(lblLabel);
        return card;
    }

    public void chargerDonnees() {
        new SwingWorker<Void, Void>() {
            int nbMembres, nbActivites, nbEnAttente, nbCompletes;
            List<String> topNames = new ArrayList<>();
            List<Integer> topCounts = new ArrayList<>();
            List<String[]> recentInscriptions = new ArrayList<>();

            @Override protected Void doInBackground() {
                List<Membre> membres = membreService.listerMembres();
                List<Activite> activites = activiteService.listerActivites();
                List<Inscription> inscriptions = inscriptionService.listerToutesInscriptions();

                nbMembres   = membres.size();
                nbActivites = activites.size();
                nbEnAttente = (int) inscriptions.stream()
                    .filter(i -> i.getStatut() == StatutInscription.EN_ATTENTE).count();
                nbCompletes = (int) activites.stream()
                    .filter(a -> activiteService.estComplete(a.getId())).count();

                activites.stream()
                    .map(a -> new Object[]{a.getNom(), inscriptionService.getNombreParticipants(a.getId())})
                    .filter(arr -> (int)arr[1] > 0)
                    .sorted((a, b) -> (int)b[1] - (int)a[1])
                    .limit(5)
                    .forEach(arr -> { topNames.add((String)arr[0]); topCounts.add((int)arr[1]); });

                inscriptions.stream()
                    .sorted((a, b) -> b.getDateInscription().compareTo(a.getDateInscription()))
                    .limit(8)
                    .forEach(i -> recentInscriptions.add(new String[]{
                        inscriptionService.getNomMembre(i.getMembreId()),
                        inscriptionService.getNomActivite(i.getActiviteId()),
                        i.getStatut().getLibelle()
                    }));
                return null;
            }

            @Override protected void done() {
                totalMembresVal.setText(String.valueOf(nbMembres));
                totalActivitesVal.setText(String.valueOf(nbActivites));
                inscriptionsEnAttenteVal.setText(String.valueOf(nbEnAttente));
                activitesCompletesVal.setText(String.valueOf(nbCompletes));
                chartPanel.setData(topNames, topCounts);

                recentPanel.removeAll();
                if (recentInscriptions.isEmpty()) {
                    JLabel empty = new JLabel("Aucune inscription recente");
                    empty.setFont(Theme.FONT_BODY); empty.setForeground(Theme.TEXT_SECONDARY);
                    recentPanel.add(empty);
                } else {
                    for (String[] row : recentInscriptions) {
                        recentPanel.add(createRecentRow(row[0], row[1], row[2]));
                        recentPanel.add(Box.createVerticalStrut(4));
                    }
                }
                recentPanel.revalidate();
                recentPanel.repaint();
            }
        }.execute();
    }

    private JPanel createRecentRow(String membre, String activite, String statut) {
        JPanel row = new JPanel(new BorderLayout(8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,6,6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        row.setBorder(new EmptyBorder(4, 8, 4, 8));

        JLabel infoLbl = new JLabel(membre + "  >  " + activite);
        infoLbl.setFont(Theme.FONT_SMALL);
        infoLbl.setForeground(Theme.TEXT_PRIMARY);

        Color statusColor = switch(statut) {
            case "Acceptée" -> Theme.SUCCESS;
            case "Refusée"  -> Theme.DANGER;
            default         -> Theme.WARNING;
        };

        JLabel statusLbl = new JLabel(statut);
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusLbl.setForeground(statusColor);

        row.add(infoLbl, BorderLayout.CENTER);
        row.add(statusLbl, BorderLayout.EAST);
        return row;
    }

    static class PopularChartPanel extends JPanel {
        private List<String> labels = new ArrayList<>();
        private List<Integer> values = new ArrayList<>();
        private static final Color[] COLORS = {
            new Color(8,145,178), new Color(5,150,105), new Color(217,119,6),
            new Color(220,38,38), new Color(14,165,233)
        };

        PopularChartPanel() { setOpaque(false); setPreferredSize(new Dimension(300, 200)); }
        void setData(List<String> l, List<Integer> v) { labels = l; values = v; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (labels.isEmpty()) {
                g.setColor(Theme.TEXT_SECONDARY); g.setFont(Theme.FONT_BODY);
                g.drawString("Aucune donnee", 20, getHeight()/2); return;
            }
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int maxVal = values.stream().mapToInt(Integer::intValue).max().orElse(1);
            int n = labels.size();
            int barH = getHeight() - 56;
            int barW = getWidth() - 40;
            int bw = barW / n - 10;
            int sx = 20;

            for (int i = 0; i < n; i++) {
                int h = (int)((double)values.get(i) / maxVal * barH);
                int x = sx + i * (barW / n);
                int y = barH - h + 10;
                GradientPaint gp = new GradientPaint(x, y, COLORS[i%COLORS.length],
                    x, y+h, COLORS[i%COLORS.length].darker());
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, bw, h, 4, 4);
                g2.setColor(Theme.TEXT_PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String v = String.valueOf(values.get(i));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(v, x + (bw - fm.stringWidth(v))/2, y-5);
                g2.setColor(Theme.TEXT_SECONDARY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String lbl = labels.get(i).length() > 9 ? labels.get(i).substring(0,8)+"..." : labels.get(i);
                fm = g2.getFontMetrics();
                g2.drawString(lbl, x + (bw - fm.stringWidth(lbl))/2, getHeight()-8);
            }
        }
    }
}
