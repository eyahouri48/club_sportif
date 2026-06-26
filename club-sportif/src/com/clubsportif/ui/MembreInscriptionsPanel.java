package com.clubsportif.ui;

import com.clubsportif.model.Inscription;
import com.clubsportif.model.StatutInscription;
import com.clubsportif.service.InscriptionService;
import com.clubsportif.ui.components.*;
import com.clubsportif.util.Session;
import com.clubsportif.util.Theme;
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel affichant les inscriptions du membre connecté.
 * Le membre peut annuler une inscription depuis cet écran.
 * Responsable : Dev B
 */
public class MembreInscriptionsPanel extends JPanel {

    private final InscriptionService inscriptionService;
    private final DataTable dataTable;
    private List<Inscription> inscriptions;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] COLUMNS = {
            "Activité", "Date d'inscription", "Statut"
    };

    public MembreInscriptionsPanel(InscriptionService inscriptionService) {
        this.inscriptionService = inscriptionService;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // En-tête
        JLabel title = new JLabel("Mes Inscriptions");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        add(title, BorderLayout.NORTH);

        // Tableau
        dataTable = new DataTable(COLUMNS);
        add(dataTable, BorderLayout.CENTER);

        // Boutons
        JPanel bottomActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomActions.setOpaque(false);

        StyledButton annulerBtn = new StyledButton("Annuler", StyledButton.Style.DANGER);
        annulerBtn.addActionListener(e -> annulerInscription());

        StyledButton refreshBtn = new StyledButton("Actualiser", StyledButton.Style.OUTLINE);
        refreshBtn.addActionListener(e -> chargerDonnees());

        bottomActions.add(annulerBtn);
        bottomActions.add(refreshBtn);
        add(bottomActions, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(this::chargerDonnees);
    }

    public void chargerDonnees() {
        String membreId = Session.getInstance().getMembreConnecte().getId();
        inscriptions = inscriptionService.getInscriptionsMembre(membreId);
        dataTable.clearRows();
        for (Inscription i : inscriptions) {
            dataTable.addRow(new Object[]{
                    inscriptionService.getNomActivite(i.getActiviteId()),
                    i.getDateInscription() != null ? i.getDateInscription().format(DT_FMT) : "",
                    i.getStatut().getLibelle()
            });
        }
    }

    private void annulerInscription() {
        int modelRow = dataTable.getSelectedModelRow();
        if (modelRow < 0 || modelRow >= inscriptions.size()) {
            DialogUtils.erreur(this, "Veuillez sélectionner une inscription à annuler.");
            return;
        }

        Inscription selected = inscriptions.get(modelRow);

        if (DialogUtils.confirmer(this,
                "Voulez-vous vraiment annuler cette inscription ?",
                "Confirmer l'annulation")) {
            try {
                inscriptionService.annulerInscription(selected.getId());
                chargerDonnees();
                DialogUtils.succes(this, "Inscription annulée avec succès.");
            } catch (IllegalArgumentException ex) {
                DialogUtils.erreur(this, ex.getMessage());
            }
        }
    }
}
