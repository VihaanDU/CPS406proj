package csa.ui;

import csa.App;
import csa.db.Database;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.util.ArrayList;

// Ryan + Devesh - coordinator dashboard
public class CoordinatorScreen extends JPanel {

    App app;

    DefaultTableModel appsModel;
    JTable appsTable;

    DefaultTableModel missingModel;
    JTable missingTable;

    JLabel totalLabel;
    JLabel pendingLabel;

    public CoordinatorScreen(App app) {
        this.app = app;
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        setup();
    }

    private void setup() {
        // header bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Helpers.NAVY);
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("Coordinator Dashboard");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        topBar.add(title, BorderLayout.WEST);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtons.setBackground(Helpers.NAVY);
        JButton refreshBtn = new JButton("Refresh");
        JButton signOutBtn = new JButton("Sign Out");
        refreshBtn.addActionListener(e -> reload());
        signOutBtn.addActionListener(e -> app.goTo(App.SCREEN_LOGIN));
        rightButtons.add(refreshBtn);
        rightButtons.add(signOutBtn);
        topBar.add(rightButtons, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // tabbed content
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Applications", buildAppsTab());
        tabs.addTab("Missing Submissions", buildMissingTab());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildAppsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Helpers.LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // summary row
        JPanel summary = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
        summary.setBackground(Helpers.LIGHT);
        totalLabel   = new JLabel("Total: 0");
        pendingLabel = new JLabel("Pending: 0");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 13));
        pendingLabel.setFont(new Font("Arial", Font.BOLD, 13));
        pendingLabel.setForeground(new Color(0x8B6914));
        summary.add(totalLabel);
        summary.add(pendingLabel);
        panel.add(summary, BorderLayout.NORTH);

        // applications table
        String[] cols = {"Student ID", "Name", "Email", "Status", "Notes", "Date Applied"};
        appsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        appsTable = new JTable(appsModel);
        appsTable.setRowHeight(24);
        appsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appsTable.getTableHeader().setBackground(new Color(0x2C3E50));
        appsTable.getTableHeader().setForeground(Color.WHITE);
        appsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // colour the status column to make it easier to read
        appsTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String s = val == null ? "" : val.toString();
                if (s.equals("Final Accepted"))
                    setForeground(new Color(0x1E8449));
                else if (s.equals("Provisional Accepted"))
                    setForeground(new Color(0x1A5276));
                else if (s.contains("Rejected"))
                    setForeground(Color.RED);
                else
                    setForeground(new Color(0x8B6914));
                return this;
            }
        });

        panel.add(new JScrollPane(appsTable), BorderLayout.CENTER);

        // action buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        btnRow.setBackground(Helpers.LIGHT);

        JButton provAccept  = new JButton("Provisional Accept");
        JButton provReject  = new JButton("Provisional Reject");
        JButton finalAccept = new JButton("Final Accept");
        JButton finalReject = new JButton("Final Reject");

        provAccept.addActionListener(e  -> decideProvisional("Provisional Accepted"));
        provReject.addActionListener(e  -> decideProvisional("Provisional Rejected"));
        finalAccept.addActionListener(e -> decideFinal("Final Accepted"));
        finalReject.addActionListener(e -> decideFinal("Final Rejected"));

        btnRow.add(provAccept);
        btnRow.add(provReject);
        btnRow.add(Box.createHorizontalStrut(10));
        btnRow.add(finalAccept);
        btnRow.add(finalReject);
        panel.add(btnRow, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildMissingTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Helpers.LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel info = new JLabel("Final Accepted students who haven't submitted their report or evaluation:");
        info.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(info, BorderLayout.NORTH);

        String[] cols = {"Student ID", "Name", "Email", "Report?", "Evaluation?"};
        missingModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        missingTable = new JTable(missingModel);
        missingTable.setRowHeight(24);
        missingTable.getTableHeader().setBackground(new Color(0x2C3E50));
        missingTable.getTableHeader().setForeground(Color.WHITE);
        missingTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        panel.add(new JScrollPane(missingTable), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        btnRow.setBackground(Helpers.LIGHT);
        JButton remindBtn = new JButton("Send Reminder Alert");
        remindBtn.addActionListener(e -> sendReminders());
        btnRow.add(remindBtn);
        panel.add(btnRow, BorderLayout.SOUTH);

        return panel;
    }

    public void reload() {
        loadApps();
        loadMissing();
        revalidate();
        repaint();
    }

    private void loadApps() {
        appsModel.setRowCount(0);
        int total = 0, pending = 0;

        try {
            ResultSet rs = Database.get().getAllApplications();
            while (rs.next()) {
                appsModel.addRow(new Object[]{
                    rs.getString("student_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("status"),
                    rs.getString("notes"),
                    rs.getString("applied_at")
                });
                total++;
                if (rs.getString("status").startsWith("Pending")) pending++;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        appsModel.fireTableDataChanged();
        totalLabel.setText("Total: " + total);
        pendingLabel.setText("Pending: " + pending);
    }

    private void loadMissing() {
        missingModel.setRowCount(0);
        try {
            ResultSet rs = Database.get().getFinalStudentsWithStatus();
            while (rs.next()) {
                missingModel.addRow(new Object[]{
                    rs.getString("student_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("has_report"),
                    rs.getString("has_eval")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        missingModel.fireTableDataChanged();
    }

    private void decideProvisional(String newStatus) {
        int row = appsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student first.");
            return;
        }

        String sid     = (String) appsModel.getValueAt(row, 0);
        String name    = (String) appsModel.getValueAt(row, 1);
        String current = (String) appsModel.getValueAt(row, 3);

        if (current != null && current.startsWith("Final")) {
            JOptionPane.showMessageDialog(this,
                "Can't change this — a final decision has already been made.");
            return;
        }

        String notes = JOptionPane.showInputDialog(this,
            "Optional notes for " + name + ":");
        if (notes == null) return;

        try {
            Database.get().updateStatus(sid, newStatus, notes);
            reload();
        } catch (Exception ex) {
            Helpers.error(this, "Error: " + ex.getMessage());
        }
    }

    private void decideFinal(String newStatus) {
        int row = appsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student first.");
            return;
        }

        String sid     = (String) appsModel.getValueAt(row, 0);
        String name    = (String) appsModel.getValueAt(row, 1);
        String current = (String) appsModel.getValueAt(row, 3);

        if (!"Provisional Accepted".equals(current)) {
            JOptionPane.showMessageDialog(this,
                "Student must be Provisionally Accepted first.\nCurrent status: " + current);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Set " + name + " to '" + newStatus + "'?");
        if (confirm != JOptionPane.YES_OPTION) return;

        String notes = JOptionPane.showInputDialog(this, "Notes (optional):");
        if (notes == null) return;

        try {
            Database.get().updateStatus(sid, newStatus, notes);
            reload();
        } catch (Exception ex) {
            Helpers.error(this, "Error: " + ex.getMessage());
        }
    }

    private void sendReminders() {
        ArrayList<String> missing = new ArrayList<>();

        for (int i = 0; i < missingModel.getRowCount(); i++) {
            String report = (String) missingModel.getValueAt(i, 3);
            String eval   = (String) missingModel.getValueAt(i, 4);
            if ("No".equals(report) || "No".equals(eval)) {
                missing.add(missingModel.getValueAt(i, 1) +
                    " (" + missingModel.getValueAt(i, 2) + ")" +
                    "  —  Report: " + report + ", Eval: " + eval);
            }
        }

        if (missing.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All students are up to date.");
            return;
        }

        StringBuilder msg = new StringBuilder("Reminder sent to:\n\n");
        for (String s : missing) msg.append("• ").append(s).append("\n");
        JOptionPane.showMessageDialog(this, msg.toString(), "Reminders Sent",
            JOptionPane.INFORMATION_MESSAGE);
    }
}
