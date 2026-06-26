package com.clubsportif.ui;

import com.clubsportif.service.MembreService;
import com.clubsportif.ui.components.*;
import com.clubsportif.util.Theme;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {

    private final MembreService membreService;
    private final String membreId;
    private final boolean requireOld;

    private StyledPasswordField oldPassField;
    private StyledPasswordField newPassField;
    private StyledPasswordField confirmField;

    public ChangePasswordDialog(JFrame parent, MembreService membreService,
                                String membreId, boolean requireOld) {
        super(parent, "Changer le mot de passe", true);
        this.membreService = membreService;
        this.membreId      = membreId;
        this.requireOld    = requireOld;

        setSize(420, requireOld ? 340 : 280);
        setLocationRelativeTo(parent);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Theme.BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Changer le mot de passe");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        FormBuilder fb = new FormBuilder();
        oldPassField = new StyledPasswordField("Mot de passe actuel");
        fb.addField("Actuel *", oldPassField);
        newPassField = new StyledPasswordField("Min. 4 caractères");
        confirmField = new StyledPasswordField("Répéter le mot de passe");
        fb.addField("Nouveau *", newPassField);
        fb.addField("Confirmer *", confirmField);
        panel.add(fb.build(), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        StyledButton cancel = new StyledButton("Annuler", StyledButton.Style.OUTLINE);
        cancel.addActionListener(e -> dispose());
        StyledButton save = new StyledButton("Enregistrer", StyledButton.Style.PRIMARY);
        save.addActionListener(e -> sauvegarder());
        buttons.add(cancel);
        buttons.add(save);
        panel.add(buttons, BorderLayout.SOUTH);

        setContentPane(panel);
    }

    private void sauvegarder() {
        String oldPass = new String(oldPassField.getPassword()).trim();
        String newPass = new String(newPassField.getPassword()).trim();
        String confirm = new String(confirmField.getPassword()).trim();

        if (!newPass.equals(confirm)) {
            DialogUtils.erreur(this, "Les mots de passe ne correspondent pas.");
            return;
        }
        try {
            membreService.changerMotDePasse(membreId, oldPass, newPass);
            DialogUtils.succes(this, "Mot de passe modifié avec succès.");
            dispose();
        } catch (IllegalArgumentException ex) {
            DialogUtils.erreur(this, ex.getMessage());
        }
    }
}
