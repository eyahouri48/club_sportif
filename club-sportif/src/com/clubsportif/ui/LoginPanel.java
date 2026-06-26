package com.clubsportif.ui;

import com.clubsportif.ui.components.*;
import com.clubsportif.util.Theme;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;

/**
 * Ecran de connexion plein ecran — image team a gauche, formulaire a droite.
 * Sans logo image — branding typographique uniquement.
 */
public class LoginPanel extends JPanel {

    private final StyledTextField     loginField;
    private final StyledPasswordField passField;
    private BiConsumer<String, String> onLogin;
    private BufferedImage teamImage;

    public LoginPanel() {
        setLayout(new GridLayout(1, 2));
        loginField = new StyledTextField("Votre identifiant");
        passField  = new StyledPasswordField("Votre mot de passe");

        try {
            teamImage = ImageIO.read(getClass().getResourceAsStream("team.png"));
        } catch (Exception ignored) {}

        // ── Panneau gauche : image team ──────────────────────────
        JPanel leftPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setColor(Theme.PRIMARY_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                if (teamImage != null) {
                    double imgR = (double) teamImage.getWidth() / teamImage.getHeight();
                    double panR = (double) getWidth() / getHeight();
                    int dw, dh, dx, dy;
                    if (imgR > panR) { dh=getHeight(); dw=(int)(dh*imgR); dx=(getWidth()-dw)/2; dy=0; }
                    else             { dw=getWidth();  dh=(int)(dw/imgR); dx=0; dy=(getHeight()-dh)/2; }
                    g2.drawImage(teamImage, dx, dy, dw, dh, null);
                    // Léger voile bas pour l'équilibre visuel (le branding est déjà dans l'image)
                    g2.setPaint(new GradientPaint(0,getHeight()-160,new Color(15,25,50,0),
                                                  0,getHeight(),new Color(15,25,50,90)));
                    g2.fillRect(0,0,getWidth(),getHeight());
                }
                g2.dispose();
            }
        };

        // ── Panneau droit : formulaire sur fond beige clair ──────
        JPanel rightPanel = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Theme.BEIGE_LIGHT);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 50));
        form.setMaximumSize(new Dimension(420, 500));

        // Typographic branding
        JLabel brandLine = new JLabel("POUR LA FORME");
        brandLine.setFont(new Font("Segoe UI", Font.BOLD, 13));
        brandLine.setForeground(Theme.PRIMARY);
        brandLine.setAlignmentX(LEFT_ALIGNMENT);
        form.add(brandLine);
        form.add(Box.createVerticalStrut(20));

        JLabel welcome = new JLabel("Bienvenue");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 30));
        welcome.setForeground(Theme.PRIMARY);
        welcome.setAlignmentX(LEFT_ALIGNMENT);
        form.add(welcome);
        form.add(Box.createVerticalStrut(6));

        JLabel subtitle = new JLabel("Connectez-vous a votre espace membre");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        form.add(subtitle);
        form.add(Box.createVerticalStrut(40));

        // Separator line beige
        JPanel sepLine = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Theme.BEIGE_DARK);
                g.fillRect(0,0,getWidth(),2);
            }
        };
        sepLine.setOpaque(false);
        sepLine.setPreferredSize(new Dimension(0,2));
        sepLine.setMaximumSize(new Dimension(400,2));
        sepLine.setAlignmentX(LEFT_ALIGNMENT);
        form.add(sepLine);
        form.add(Box.createVerticalStrut(30));

        // Fields
        form.add(fieldLabel("Identifiant"));
        form.add(Box.createVerticalStrut(6));
        loginField.setMaximumSize(new Dimension(400, Theme.FIELD_HEIGHT));
        loginField.setAlignmentX(LEFT_ALIGNMENT);
        form.add(loginField);
        form.add(Box.createVerticalStrut(20));

        form.add(fieldLabel("Mot de passe"));
        form.add(Box.createVerticalStrut(6));
        passField.setMaximumSize(new Dimension(400, Theme.FIELD_HEIGHT));
        passField.setAlignmentX(LEFT_ALIGNMENT);
        form.add(passField);
        form.add(Box.createVerticalStrut(32));

        StyledButton loginBtn = new StyledButton("Se connecter", StyledButton.Style.PRIMARY);
        loginBtn.setMaximumSize(new Dimension(400, 48));
        loginBtn.setAlignmentX(LEFT_ALIGNMENT);
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(20));

        rightPanel.add(form);

        // Texte responsive : le titre et la ligne de marque se redimensionnent
        // proportionnellement à la largeur du panneau droit (#2).
        rightPanel.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int w = rightPanel.getWidth();
                int welcomeSize = Math.max(22, Math.min(40, w / 14));
                int brandSize   = Math.max(11, Math.min(16, w / 36));
                welcome.setFont(new Font("Segoe UI", Font.BOLD, welcomeSize));
                brandLine.setFont(new Font("Segoe UI", Font.BOLD, brandSize));
                form.revalidate();
            }
        });

        add(leftPanel);
        add(rightPanel);

        loginBtn.addActionListener(e -> doLogin());
        loginField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) passField.requestFocus();
            }
        });
        passField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        });
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL_BOLD);
        l.setForeground(Theme.TEXT_SECONDARY);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    public void setOnLogin(BiConsumer<String, String> h) { this.onLogin = h; }

    public void afficherErreur(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erreur de connexion", JOptionPane.ERROR_MESSAGE);
        passField.setText(""); passField.requestFocus();
    }

    private void doLogin() {
        String login = loginField.getText().trim();
        String pass  = new String(passField.getPassword());
        if (login.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.",
                "Champs requis", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (onLogin != null) onLogin.accept(login, pass);
    }
}
