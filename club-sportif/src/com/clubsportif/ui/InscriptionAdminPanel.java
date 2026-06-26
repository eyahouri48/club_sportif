package com.clubsportif.ui;

import com.clubsportif.model.Inscription;
import com.clubsportif.model.StatutInscription;
import com.clubsportif.service.InscriptionService;
import com.clubsportif.ui.components.*;
import com.clubsportif.util.Theme;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel de gestion des inscriptions (vue admin).
 * Permet de consulter, valider, refuser et supprimer des inscriptions.
 * Responsable : Dev B
 */
public class InscriptionAdminPanel extends JPanel {

    private final InscriptionService inscriptionService;
    private final DataTable dataTable;
    private final StyledTextField searchField;
    private List<Inscription> inscriptions;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] COLUMNS = {
            "Membre", "Activité", "Date inscription", "Statut"
    };

    public InscriptionAdminPanel(InscriptionService inscriptionService) {
        this.inscriptionService = inscriptionService;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // En-tête
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("Gestion des Inscriptions");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        searchField = new StyledTextField("Rechercher...");
        searchField.setPreferredSize(new Dimension(220, Theme.FIELD_HEIGHT));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { dataTable.filter(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { dataTable.filter(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { dataTable.filter(searchField.getText()); }
        });

        header.add(title, BorderLayout.WEST);
        header.add(searchField, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Tableau
        dataTable = new DataTable(COLUMNS);
        add(dataTable, BorderLayout.CENTER);

        // Boutons d'action
        JPanel bottomActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomActions.setOpaque(false);

        StyledButton validerBtn = new StyledButton("Valider", StyledButton.Style.SUCCESS);
        validerBtn.addActionListener(e -> validerInscription());

        StyledButton refuserBtn = new StyledButton("Refuser", StyledButton.Style.DANGER);
        refuserBtn.addActionListener(e -> refuserInscription());

        StyledButton supprimerBtn = new StyledButton("Supprimer", StyledButton.Style.DANGER);
        supprimerBtn.addActionListener(e -> supprimerInscription());

        StyledButton refreshBtn = new StyledButton("Actualiser", StyledButton.Style.OUTLINE);
        refreshBtn.addActionListener(e -> chargerDonnees());

        bottomActions.add(validerBtn);
        bottomActions.add(refuserBtn);
        bottomActions.add(supprimerBtn);
        bottomActions.add(refreshBtn);
        add(bottomActions, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(this::chargerDonnees);
    }

    public void chargerDonnees() {
        inscriptions = inscriptionService.listerToutesInscriptions();
        dataTable.clearRows();
        for (Inscription i : inscriptions) {
            dataTable.addRow(new Object[]{
                    inscriptionService.getNomMembre(i.getMembreId()),
                    inscriptionService.getNomActivite(i.getActiviteId()),
                    i.getDateInscription() != null ? i.getDateInscription().format(DT_FMT) : "",
                    i.getStatut().getLibelle()
            });
        }
    }

    private Inscription getInscriptionSelectionnee() {
        int modelRow = dataTable.getSelectedModelRow();
        if (modelRow < 0 || modelRow >= inscriptions.size()) return null;
        return inscriptions.get(modelRow);
    }

    private void validerInscription() {
        Inscription selected = getInscriptionSelectionnee();
        if (selected == null) {
            DialogUtils.erreur(this, "Veuillez sélectionner une inscription.");
            return;
        }
        try {
            inscriptionService.validerInscription(selected.getId());
            chargerDonnees();
            DialogUtils.succes(this, "Inscription validée avec succès.");
        } catch (IllegalArgumentException ex) {
            DialogUtils.erreur(this, ex.getMessage());
        }
    }

    private void refuserInscription() {
        Inscription selected = getInscriptionSelectionnee();
        if (selected == null) {
            DialogUtils.erreur(this, "Veuillez sélectionner une inscription.");
            return;
        }
        try {
            inscriptionService.refuserInscription(selected.getId());
            chargerDonnees();
            DialogUtils.succes(this, "Inscription refusée.");
        } catch (IllegalArgumentException ex) {
            DialogUtils.erreur(this, ex.getMessage());
        }
    }

    private void supprimerInscription() {
        Inscription selected = getInscriptionSelectionnee();
        if (selected == null) {
            DialogUtils.erreur(this, "Veuillez sélectionner une inscription.");
            return;
        }
        if (DialogUtils.confirmer(this, "Voulez-vous vraiment supprimer cette inscription ?",
                "Confirmer")) {
            try {
                inscriptionService.annulerInscription(selected.getId());
                chargerDonnees();
                DialogUtils.succes(this, "Inscription supprimée.");
            } catch (IllegalArgumentException ex) {
                DialogUtils.erreur(this, ex.getMessage());
            }
        }
    }
}
