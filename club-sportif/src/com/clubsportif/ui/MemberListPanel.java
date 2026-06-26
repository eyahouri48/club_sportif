package com.clubsportif.ui;

import com.clubsportif.model.Membre;
import com.clubsportif.service.MembreService;
import com.clubsportif.ui.components.*;
import com.clubsportif.util.PdfCardGenerator;
import com.clubsportif.util.PhotoUtils;
import com.clubsportif.util.Theme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel d'affichage et gestion des membres — avec miniature photo en début de ligne.
 */
public class MemberListPanel extends JPanel {

    private final MembreService membreService;
    private final DataTable dataTable;
    private final StyledTextField searchField;
    private List<Membre> membres;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] COLUMNS = {
            "", "Nom", "Prénom", "Login", "Email", "Téléphone", "Date naiss.", "BMI"
    };

    public MemberListPanel(MembreService membreService) {
        this.membreService = membreService;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Header
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("Gestion des Membres");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        searchField = new StyledTextField("Rechercher un membre...");
        searchField.setPreferredSize(new Dimension(220, Theme.FIELD_HEIGHT));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { dataTable.filter(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { dataTable.filter(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { dataTable.filter(searchField.getText()); }
        });

        StyledButton addBtn = new StyledButton("+ Ajouter", StyledButton.Style.PRIMARY);
        addBtn.addActionListener(e -> ouvrirFormulaireAjout());

        actions.add(searchField);
        actions.add(addBtn);
        header.add(title, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        dataTable = new DataTable(COLUMNS);
        configurerColonnePhoto();
        add(dataTable, BorderLayout.CENTER);

        JPanel bottomActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomActions.setOpaque(false);

        StyledButton editBtn = new StyledButton("Modifier", StyledButton.Style.OUTLINE);
        editBtn.addActionListener(e -> ouvrirFormulaireModification());

        StyledButton deleteBtn = new StyledButton("Supprimer", StyledButton.Style.DANGER);
        deleteBtn.addActionListener(e -> supprimerMembre());

        StyledButton ficheBtn = new StyledButton("Fiche Sanitaire", StyledButton.Style.OUTLINE);
        ficheBtn.addActionListener(e -> ouvrirFicheSanitaire());

        StyledButton pdfBtn = new StyledButton("Carte PDF", StyledButton.Style.OUTLINE);
        pdfBtn.addActionListener(e -> genererCartePDF());

        StyledButton refreshBtn = new StyledButton("Actualiser", StyledButton.Style.OUTLINE);
        refreshBtn.addActionListener(e -> chargerDonnees());

        bottomActions.add(editBtn);
        bottomActions.add(deleteBtn);
        bottomActions.add(ficheBtn);
        bottomActions.add(pdfBtn);
        bottomActions.add(refreshBtn);
        add(bottomActions, BorderLayout.SOUTH);

        // Defer data loading to avoid white screen
        SwingUtilities.invokeLater(this::chargerDonnees);
    }

    private void configurerColonnePhoto() {
        JTable table = dataTable.getTable();
        TableColumn photoCol = table.getColumnModel().getColumn(0);
        photoCol.setMaxWidth(50);
        photoCol.setMinWidth(50);
        photoCol.setPreferredWidth(50);
        photoCol.setCellRenderer(new PhotoCellRenderer());
    }

    public void chargerDonnees() {
        membres = membreService.listerMembres();
        dataTable.clearRows();
        for (Membre m : membres) {
            double bmi = m.calculerBMI();
            String bmiStr = bmi > 0 ? String.format("%.1f", bmi) : "—";
            dataTable.addRow(new Object[]{
                    m, // photo column uses the Membre object
                    m.getNom(),
                    m.getPrenom(),
                    m.getLogin(),
                    m.getEmail(),
                    m.getTelephone(),
                    m.getDateNaissance() != null ? m.getDateNaissance().format(DATE_FMT) : "",
                    bmiStr
            });
        }
    }

    /**
     * Renderer for the photo thumbnail column.
     */
    private static class PhotoCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel();
            label.setHorizontalAlignment(CENTER);
            label.setOpaque(true);
            label.setBackground(isSelected ? Theme.PRIMARY_LIGHT :
                    (row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));

            if (value instanceof Membre m) {
                label.setIcon(PhotoUtils.createAvatarLabel(m.getPhoto(), 32).getIcon());
            }
            return label;
        }
    }

    private Membre getMembreSelectionne() {
        int modelRow = dataTable.getSelectedModelRow();
        if (modelRow < 0 || modelRow >= membres.size()) return null;
        return membres.get(modelRow);
    }

    private void ouvrirFormulaireAjout() {
        MemberFormDialog dialog = new MemberFormDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), membreService, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) chargerDonnees();
    }

    private void ouvrirFormulaireModification() {
        Membre selected = getMembreSelectionne();
        if (selected == null) {
            DialogUtils.erreur(this, "Veuillez sélectionner un membre à modifier.");
            return;
        }
        MemberFormDialog dialog = new MemberFormDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), membreService, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) chargerDonnees();
    }

    private void ouvrirFicheSanitaire() {
        Membre selected = getMembreSelectionne();
        if (selected == null) {
            DialogUtils.erreur(this, "Veuillez sélectionner un membre.");
            return;
        }
        FicheSanitaireDialog dlg = new FicheSanitaireDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), membreService, selected);
        dlg.setVisible(true);
        chargerDonnees();
    }

    private void genererCartePDF() {
        Membre selected = getMembreSelectionne();
        if (selected == null) {
            DialogUtils.erreur(this, "Veuillez sélectionner un membre pour générer sa carte.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Enregistrer la carte membre");
        chooser.setSelectedFile(new java.io.File("carte_" + selected.getLogin() + ".pdf"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF", "pdf"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf"))
            file = new java.io.File(file.getAbsolutePath() + ".pdf");

        try {
            PdfCardGenerator.genererCarte(selected, file);
            DialogUtils.succes(this, "Carte générée : " + file.getAbsolutePath());
        } catch (Exception ex) {
            DialogUtils.erreur(this, "Erreur lors de la génération PDF : " + ex.getMessage());
        }
    }

    private void supprimerMembre() {
        Membre selected = getMembreSelectionne();
        if (selected == null) {
            DialogUtils.erreur(this, "Veuillez sélectionner un membre à supprimer.");
            return;
        }
        if (DialogUtils.confirmer(this,
                "Voulez-vous vraiment supprimer " + selected.getNomComplet() + " ?",
                "Confirmer la suppression")) {
            try {
                membreService.supprimerMembre(selected.getId());
                chargerDonnees();
                DialogUtils.succes(this, "Membre supprimé avec succès.");
            } catch (IllegalArgumentException ex) {
                DialogUtils.erreur(this, ex.getMessage());
            }
        }
    }
}
