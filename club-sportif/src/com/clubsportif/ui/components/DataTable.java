package com.clubsportif.ui.components;

import com.clubsportif.util.Theme;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Table de données stylisée.
 */
public class DataTable extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;
    private final List<Object[]> allRows = new ArrayList<>();
    private final String[] columns;

    public DataTable(String[] columns) {
        this.columns = columns;
        setLayout(new BorderLayout());
        setOpaque(false);

        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Object.class : String.class;
            }
        };

        table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(Theme.PRIMARY_LIGHT);
                    c.setForeground(Theme.PRIMARY_DARK);
                } else if (row % 2 == 0) {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Theme.TEXT_PRIMARY);
                } else {
                    c.setBackground(new Color(248, 250, 255));
                    c.setForeground(Theme.TEXT_PRIMARY);
                }
                if (c instanceof JLabel lbl) lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        };

        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(44);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(Theme.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                lbl.setBackground(Theme.PRIMARY);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                lbl.setHorizontalAlignment(LEFT);
                lbl.setOpaque(true);
                return lbl;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);
        add(sp, BorderLayout.CENTER);
    }

    public void clearRows() {
        allRows.clear();
        model.setRowCount(0);
    }

    public void addRow(Object[] row) {
        allRows.add(row);
        model.addRow(row);
    }

    public void setData(Object[][] data) {
        allRows.clear();
        model.setRowCount(0);
        for (Object[] row : data) {
            allRows.add(row);
            model.addRow(row);
        }
    }

    public void filter(String query) {
        model.setRowCount(0);
        String q = query.toLowerCase().trim();
        for (Object[] row : allRows) {
            if (q.isEmpty()) { model.addRow(row); continue; }
            for (Object cell : row) {
                if (cell != null && cell.toString().toLowerCase().contains(q)) {
                    model.addRow(row);
                    break;
                }
            }
        }
    }

    /** Retourne la ligne sélectionnée dans le modèle (pas la vue filtrée). */
    public int getSelectedModelRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return -1;
        return table.convertRowIndexToModel(viewRow);
    }

    public int getSelectedRow() { return table.getSelectedRow(); }

    public JTable getTable() { return table; }

    public DefaultTableModel getModel() { return model; }

    public void configureColumn(int index, int width) {
        if (index >= table.getColumnCount()) return;
        TableColumn col = table.getColumnModel().getColumn(index);
        col.setPreferredWidth(width);
        if (width <= 0) { col.setMinWidth(0); col.setMaxWidth(0); }
    }
}
