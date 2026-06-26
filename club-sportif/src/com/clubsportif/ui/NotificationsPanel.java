package com.clubsportif.ui;

import com.clubsportif.model.Notification;
import com.clubsportif.service.NotificationService;
import com.clubsportif.ui.components.CardPanel;
import com.clubsportif.ui.components.StyledButton;
import com.clubsportif.util.IconFactory;
import com.clubsportif.util.Theme;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationsPanel extends JPanel {

    private final NotificationService notificationService;
    private final String destinataireId;
    private JPanel listPanel;
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public NotificationsPanel(NotificationService notificationService, String destinataireId) {
        this.notificationService = notificationService;
        this.destinataireId = destinataireId;
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("  Notifications");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setIcon(IconFactory.create("notification", 24, Theme.PRIMARY));

        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerButtons.setOpaque(false);
        StyledButton marquerTout = new StyledButton("Tout marquer lu", StyledButton.Style.OUTLINE);
        marquerTout.addActionListener(e -> {
            notificationService.marquerToutesCommeLues(destinataireId);
            charger();
        });
        headerButtons.add(marquerTout);
        header.add(title, BorderLayout.WEST);
        header.add(headerButtons, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_MAIN);
        add(scroll, BorderLayout.CENTER);

        SwingUtilities.invokeLater(this::charger);
    }

    public void charger() {
        listPanel.removeAll();
        List<Notification> notifications = notificationService.getNotificationsUtilisateur(destinataireId);

        if (notifications.isEmpty()) {
            JLabel vide = new JLabel("Aucune notification pour le moment.");
            vide.setFont(Theme.FONT_BODY);
            vide.setForeground(Theme.TEXT_SECONDARY);
            vide.setAlignmentX(LEFT_ALIGNMENT);
            listPanel.add(Box.createVerticalStrut(20));
            listPanel.add(vide);
        } else {
            for (Notification n : notifications) {
                listPanel.add(buildNotifCard(n));
                listPanel.add(Box.createVerticalStrut(8));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel buildNotifCard(Notification n) {
        boolean lue = n.isLue();
        Color accent = switch(n.getType()) {
            case INSCRIPTION_ACCEPTEE -> Theme.SUCCESS;
            case INSCRIPTION_REFUSEE  -> Theme.DANGER;
            case ACTIVITE_COMPLETE    -> Theme.WARNING;
            case NOUVELLE_INSCRIPTION -> Theme.ACCENT;
            case PAIEMENT_EFFECTUE    -> Theme.SUCCESS;
        };

        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(lue ? Color.WHITE : new Color(240, 249, 255));
                g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.setColor(lue ? Theme.BORDER : accent);
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                if (!lue) {
                    g2.setColor(accent);
                    g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JPanel textZone = new JPanel();
        textZone.setLayout(new BoxLayout(textZone, BoxLayout.Y_AXIS));
        textZone.setOpaque(false);

        JLabel msgLbl = new JLabel(n.getMessage());
        msgLbl.setFont(lue ? Theme.FONT_BODY : Theme.FONT_BODY_BOLD);
        msgLbl.setForeground(Theme.TEXT_PRIMARY);

        JLabel dateLbl = new JLabel(n.getDateCreation().format(DT_FMT));
        dateLbl.setFont(Theme.FONT_CAPTION);
        dateLbl.setForeground(Theme.TEXT_MUTED);

        textZone.add(msgLbl);
        textZone.add(Box.createVerticalStrut(4));
        textZone.add(dateLbl);

        card.add(new JLabel(IconFactory.create("notification", 20, accent)), BorderLayout.WEST);
        card.add(textZone, BorderLayout.CENTER);

        if (!lue) {
            StyledButton markBtn = new StyledButton("Lu", StyledButton.Style.GHOST);
            markBtn.setPreferredSize(new Dimension(50, 30));
            markBtn.addActionListener(e -> {
                notificationService.marquerCommeLue(n.getId());
                charger();
            });
            card.add(markBtn, BorderLayout.EAST);
        }

        return card;
    }
}
