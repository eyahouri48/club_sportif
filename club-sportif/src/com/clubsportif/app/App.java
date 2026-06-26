package com.clubsportif.app;

import com.clubsportif.dao.impl.*;
import com.clubsportif.dao.*;
import com.clubsportif.model.*;
import com.clubsportif.service.*;
import com.clubsportif.ui.*;
import com.clubsportif.util.*;

import javax.swing.*;
import java.awt.*;

public class App {

    private final MembreDAO       membreDAO;
    private final ActiviteDAO     activiteDAO;
    private final InscriptionDAO  inscriptionDAO;
    private final NotificationDAO notificationDAO;
    private final PaiementDAO     paiementDAO;

    private final MembreService       membreService;
    private final ActiviteService     activiteService;
    private final InscriptionService  inscriptionService;
    private final NotificationService notificationService;
    private final PaiementService     paiementService;

    private JFrame    loginFrame;
    private MainFrame mainFrame;

    public App() {
        membreDAO       = new MembreDAOImpl();
        activiteDAO     = new ActiviteDAOImpl();
        inscriptionDAO  = new InscriptionDAOImpl();
        notificationDAO = new NotificationDAOImpl();
        paiementDAO     = new PaiementDAOImpl();

        membreService      = new MembreService(membreDAO);
        membreService.setInscriptionDAO(inscriptionDAO);
        activiteService    = new ActiviteService(activiteDAO, inscriptionDAO);
        notificationService = new NotificationService(notificationDAO);
        inscriptionService = new InscriptionService(inscriptionDAO, activiteDAO, membreDAO);
        inscriptionService.setNotificationService(notificationService);
        paiementService = new PaiementService(paiementDAO, inscriptionService, activiteService);
        paiementService.setNotificationService(notificationService);
        paiementService.setMembreService(membreService);
        inscriptionService.setPaiementService(paiementService);

        new DataInitializer(membreDAO, activiteDAO).initialiser();
    }

    public void start() { showLogin(); }

    private void showLogin() {
        if (mainFrame != null) { mainFrame.dispose(); mainFrame = null; }

        loginFrame = new JFrame("Pour La Forme — Connexion");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        loginFrame.setMinimumSize(new Dimension(900, 600));

        LoginPanel loginPanel = new LoginPanel();
        loginPanel.setOnLogin((login, pass) -> handleLogin(loginPanel, login, pass));
        loginFrame.setContentPane(loginPanel);
        loginFrame.setVisible(true);
    }

    private void handleLogin(LoginPanel loginPanel, String login, String pass) {
        try {
            Membre membre = membreService.trouverParLogin(login)
                    .filter(m -> m.getMotDePasse().equals(pass))
                    .orElseThrow(() -> new IllegalArgumentException("Login ou mot de passe incorrect."));
            Session.getInstance().ouvrirSession(membre);
            loginFrame.dispose();

            if (membre.isPremierAcces() && membre.getRole() == Role.MEMBRE) {
                showMembreDashboard(membre);
                SwingUtilities.invokeLater(() -> {
                    ChangePasswordDialog cpd = new ChangePasswordDialog(
                        mainFrame, membreService, membre.getId(), true);
                    cpd.setVisible(true);
                });
            } else if (membre.getRole() == Role.ADMIN) {
                showAdminDashboard();
            } else {
                showMembreDashboard(membre);
            }
        } catch (IllegalArgumentException e) {
            loginPanel.afficherErreur(e.getMessage());
        }
    }

    private void showAdminDashboard() {
        mainFrame = new MainFrame();
        DashboardPanel dashboard = new DashboardPanel(activiteService, inscriptionService, membreService);

        mainFrame.addMenuItem("Tableau de bord", "dashboard",    dashboard);
        mainFrame.addMenuItem("Membres",         "members",      new MemberListPanel(membreService));
        mainFrame.addMenuItem("Activites",       "activity",     new ActiviteAdminPanel(activiteService, inscriptionService));
        mainFrame.addMenuItem("Inscriptions",    "inscription",  new InscriptionAdminPanel(inscriptionService));
        mainFrame.addMenuItem("Paiements",       "payment",      new PaiementAdminPanel(paiementService, membreService, activiteService));
        mainFrame.addMenuItem("Statistiques",    "stats",        new ClassementPanel(activiteService, inscriptionService, membreService));
        mainFrame.addMenuItem("Notifications",   "notification", new NotificationsPanel(notificationService, "ADMIN"));

        mainFrame.addLogoutButton(this::logout);
        mainFrame.navigateTo("Tableau de bord");
        dashboard.chargerDonnees();
        mainFrame.setVisible(true);
    }

    private void showMembreDashboard(Membre membre) {
        mainFrame = new MainFrame();
        mainFrame.addMenuItem("Activites",        "activity",     new MembreActivitesPanel(activiteService, inscriptionService));
        mainFrame.addMenuItem("Mes inscriptions", "inscription",  new MembreInscriptionsPanel(inscriptionService));
        mainFrame.addMenuItem("Mes paiements",    "payment",      new PaiementMembrePanel(paiementService, activiteService));
        mainFrame.addMenuItem("Notifications",    "notification", new NotificationsPanel(notificationService, membre.getId()));
        mainFrame.addMenuItem("Mon profil",       "profile",      new ProfilePanel(membreService));

        mainFrame.addLogoutButton(this::logout);
        mainFrame.navigateTo("Activites");
        mainFrame.setVisible(true);
    }

    private void logout() {
        Session.getInstance().fermerSession();
        if (mainFrame != null) { mainFrame.dispose(); mainFrame = null; }
        showLogin();
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> new App().start());
    }
}
