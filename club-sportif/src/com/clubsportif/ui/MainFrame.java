package com.clubsportif.ui;

import com.clubsportif.util.IconFactory;
import com.clubsportif.util.Session;
import com.clubsportif.util.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fenêtre principale — Sidebar turquoise "Pour La Forme".
 * Icônes vectorielles custom, logo intégré, animations fluides.
 */
public class MainFrame extends JFrame {

    private final JPanel     contentPanel;
    private final CardLayout cardLayout;
    private       JPanel     sidebarPanel;
    private final Map<String, SidebarItem> menuItems = new LinkedHashMap<>();
    private String  currentPanel = "";
    private Runnable onLogout;
    private boolean sidebarExpanded = true;
    private int     sidebarCurrentW  = Theme.SIDEBAR_WIDTH;
    private Timer   sidebarAnimTimer;

    public MainFrame() {
        setTitle("Pour La Forme — Sport & Bien-être");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1320, 800);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_MAIN);
        sidebarPanel = buildSidebar();
        add(sidebarPanel, BorderLayout.WEST);
        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Theme.BG_MAIN);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(Theme.sidebarGradient(getHeight()));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 6));
                g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(Theme.SIDEBAR_WIDTH, 0));

        // Logo zone
        JPanel logoZone = new JPanel(new BorderLayout());
        logoZone.setOpaque(false);
        logoZone.setBorder(new EmptyBorder(16, 14, 12, 14));
        logoZone.setMaximumSize(new Dimension(Theme.SIDEBAR_WIDTH, 68));

        JPanel logoLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logoLeft.setOpaque(false);

        // Logo supprimé (#2) — branding purement typographique
        JPanel brandText = new JPanel();
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.setOpaque(false);
        JLabel brandName = new JLabel("Pour La Forme");
        brandName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        brandName.setForeground(Color.WHITE);
        JLabel brandSub = new JLabel("Sport & Bien-être");
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        brandSub.setForeground(Theme.SIDEBAR_ACTIVE);
        brandText.add(brandName);
        brandText.add(brandSub);

        logoLeft.add(brandText);
        logoZone.add(logoLeft, BorderLayout.CENTER);

        JButton toggleBtn = makeToggleBtn();
        toggleBtn.addActionListener(e -> toggleSidebar(sidebar, toggleBtn));
        logoZone.add(toggleBtn, BorderLayout.EAST);

        sidebar.add(logoZone);
        sidebar.add(makeSep());
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(buildUserZone());
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(makeSep());
        sidebar.add(Box.createVerticalStrut(10));

        JLabel sectionLbl = new JLabel("NAVIGATION");
        sectionLbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        sectionLbl.setForeground(new Color(71, 85, 105));
        sectionLbl.setBorder(new EmptyBorder(0, 18, 6, 0));
        sectionLbl.setMaximumSize(new Dimension(Theme.SIDEBAR_WIDTH, 20));
        sectionLbl.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(sectionLbl);
        return sidebar;
    }

    private JButton makeToggleBtn() {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,12));
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setIcon(IconFactory.create("filter", 14, new Color(100, 116, 139)));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(28, 28));
        return btn;
    }

    private JPanel buildUserZone() {
        JPanel zone = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        zone.setOpaque(false);
        zone.setMaximumSize(new Dimension(Theme.SIDEBAR_WIDTH, 60));

        String nom = Session.getInstance().getNomUtilisateur();
        String[] parts = nom.split(" ");
        String initiales = (parts.length >= 2)
            ? String.valueOf(parts[0].charAt(0)).toUpperCase() + String.valueOf(parts[1].charAt(0)).toUpperCase()
            : nom.substring(0, Math.min(2, nom.length())).toUpperCase();

        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(Theme.logoGradient(0, 0, getWidth(), getHeight()));
                g2.fillOval(0, 0, getWidth()-1, getHeight()-1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setOpaque(false);
        avatar.setLayout(new GridBagLayout());
        avatar.setPreferredSize(new Dimension(38, 38));
        JLabel initLbl = new JLabel(initiales);
        initLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        initLbl.setForeground(Color.WHITE);
        avatar.add(initLbl);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        JLabel nameLbl = new JLabel(nom.length() > 18 ? nom.substring(0,16)+"..." : nom);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLbl.setForeground(Color.WHITE);
        String role = Session.getInstance().isAdmin() ? "Administrateur" : "Membre";
        JLabel roleLbl = new JLabel(role);
        roleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        roleLbl.setForeground(Theme.SIDEBAR_ACTIVE);
        info.add(nameLbl);
        info.add(roleLbl);
        zone.add(avatar);
        zone.add(info);
        return zone;
    }

    private JSeparator makeSep() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 12));
        sep.setBackground(new Color(255, 255, 255, 6));
        sep.setMaximumSize(new Dimension(Theme.SIDEBAR_WIDTH, 1));
        return sep;
    }

    private void toggleSidebar(JPanel sidebar, JButton btn) {
        int targetW = sidebarExpanded ? Theme.SIDEBAR_COLLAPSED : Theme.SIDEBAR_WIDTH;
        sidebarExpanded = !sidebarExpanded;
        if (sidebarAnimTimer != null && sidebarAnimTimer.isRunning()) sidebarAnimTimer.stop();
        sidebarAnimTimer = new Timer(12, null);
        sidebarAnimTimer.addActionListener(e -> {
            int diff = targetW - sidebarCurrentW;
            if (Math.abs(diff) <= 4) {
                sidebarCurrentW = targetW;
                sidebarAnimTimer.stop();
            } else {
                sidebarCurrentW += diff / 4;
            }
            sidebar.setPreferredSize(new Dimension(sidebarCurrentW, 0));
            for (SidebarItem item : menuItems.values())
                item.setTextVisible(sidebarCurrentW > 120);
            revalidate();
        });
        sidebarAnimTimer.start();
    }

    public void addMenuItem(String name, String iconName, JPanel panel) {
        contentPanel.add(panel, name);
        SidebarItem item = new SidebarItem(iconName, name);
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { navigateTo(name); }
        });
        menuItems.put(name, item);
        sidebarPanel.add(item);
    }

    public void addLogoutButton(Runnable onLogout) {
        this.onLogout = onLogout;
        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(makeSep());
        sidebarPanel.add(Box.createVerticalStrut(4));
        SidebarItem logoutItem = new SidebarItem("logout", "Déconnexion");
        logoutItem.setDanger(true);
        logoutItem.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (onLogout != null) onLogout.run(); }
        });
        sidebarPanel.add(logoutItem);
        sidebarPanel.add(Box.createVerticalStrut(12));
    }

    public void navigateTo(String panelName) {
        for (Map.Entry<String, SidebarItem> entry : menuItems.entrySet())
            entry.getValue().setActive(entry.getKey().equals(panelName));
        cardLayout.show(contentPanel, panelName);
        currentPanel = panelName;
    }

    public JPanel getContentPanel() { return contentPanel; }

    // ── SidebarItem with vector icons ─────────────────────────────
    private static class SidebarItem extends JPanel {
        private boolean active = false, hovering = false, danger = false, textVisible = true;
        private final JLabel iconLbl, textLbl;
        private final String iconName;
        private float activeAlpha = 0f;
        private Timer activeTimer;

        SidebarItem(String iconName, String text) {
            this.iconName = iconName;
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.LEFT, 14, 0));
            setMaximumSize(new Dimension(Theme.SIDEBAR_WIDTH, 44));
            setPreferredSize(new Dimension(Theme.SIDEBAR_WIDTH, 44));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            iconLbl = new JLabel(IconFactory.sidebar(iconName, 20));
            textLbl = new JLabel(text);
            textLbl.setFont(Theme.FONT_SIDEBAR);
            textLbl.setForeground(Theme.SIDEBAR_TEXT);
            add(iconLbl);
            add(textLbl);

            MouseAdapter hover = new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hovering = false; repaint(); }
            };
            addMouseListener(hover);
            MouseAdapter forward = new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    SidebarItem.this.dispatchEvent(SwingUtilities.convertMouseEvent((Component)e.getSource(), e, SidebarItem.this));
                }
                @Override public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hovering = false; repaint(); }
            };
            iconLbl.addMouseListener(forward);
            textLbl.addMouseListener(forward);
        }

        void setActive(boolean a) {
            this.active = a;
            iconLbl.setIcon(a ? IconFactory.sidebarActive(iconName, 20) : IconFactory.sidebar(iconName, 20));
            textLbl.setFont(a ? Theme.FONT_SIDEBAR_ACT : Theme.FONT_SIDEBAR);
            textLbl.setForeground(a ? Color.WHITE : Theme.SIDEBAR_TEXT);

            if (activeTimer != null) activeTimer.stop();
            activeAlpha = a ? 0f : 1f;
            float target = a ? 1f : 0f;
            activeTimer = new Timer(16, null);
            activeTimer.addActionListener(e -> {
                activeAlpha += (target - activeAlpha) * 0.15f;
                if (Math.abs(activeAlpha - target) < 0.01f) { activeAlpha = target; activeTimer.stop(); }
                repaint();
            });
            activeTimer.start();
        }

        void setDanger(boolean d) {
            this.danger = d;
            iconLbl.setIcon(IconFactory.create("logout", 20, new Color(252, 165, 165)));
            textLbl.setForeground(new Color(252, 165, 165));
        }

        void setTextVisible(boolean v) { textVisible = v; textLbl.setVisible(v); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                int alpha = (int)(40 * activeAlpha);
                g2.setColor(new Color(8, 145, 178, alpha)); // PRIMARY with alpha
                g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 8, 8);
            } else if (hovering) {
                g2.setColor(new Color(255, 255, 255, danger ? 8 : 12));
                g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 8, 8);
            }
            if (active && activeAlpha > 0.05f) {
                g2.setColor(new Color(34, 211, 238, (int)(255 * activeAlpha))); // SIDEBAR_ACTIVE
                g2.fillRoundRect(2, 8, 3, getHeight()-16, 3, 3);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
