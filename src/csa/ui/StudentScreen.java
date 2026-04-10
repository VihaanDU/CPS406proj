package csa.ui;

import csa.App;
import csa.db.Database;
import csa.model.User;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.*;
import java.sql.ResultSet;

// Yasir - student home page after logging in
public class StudentScreen extends JPanel {

    App app;
    User user;

    JLabel nameLabel;
    JLabel statusLabel;
    JLabel notesLabel;
    JPanel actionArea;

    public StudentScreen(App app) {
        this.app = app;
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        setup();
    }

    public void setUser(User u) {
        user = u;
    }

    private void setup() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Helpers.NAVY);
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        JLabel title = new JLabel("Student Portal");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        topBar.add(title, BorderLayout.WEST);
        JButton signOut = new JButton("Sign Out");
        signOut.addActionListener(e -> app.goTo(App.SCREEN_LOGIN));
        topBar.add(signOut, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        nameLabel = new JLabel("Welcome");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        body.add(nameLabel);
        body.add(Box.createVerticalStrut(16));

        JLabel statusHeading = new JLabel("Application Status");
        statusHeading.setFont(new Font("Arial", Font.BOLD, 13));
        statusHeading.setAlignmentX(LEFT_ALIGNMENT);
        body.add(statusHeading);
        body.add(Box.createVerticalStrut(6));

        statusLabel = new JLabel("Loading...");
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        body.add(statusLabel);
        body.add(Box.createVerticalStrut(4));

        notesLabel = new JLabel("");
        notesLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        notesLabel.setForeground(Color.GRAY);
        notesLabel.setAlignmentX(LEFT_ALIGNMENT);
        body.add(notesLabel);
        body.add(Box.createVerticalStrut(20));

        actionArea = new JPanel();
        actionArea.setLayout(new BoxLayout(actionArea, BoxLayout.Y_AXIS));
        actionArea.setBackground(Color.WHITE);
        actionArea.setAlignmentX(LEFT_ALIGNMENT);
        body.add(actionArea);

        add(body, BorderLayout.CENTER);
    }

    public void reload() {
        if (user == null) return;

        nameLabel.setText("Welcome, " + user.getName() + "  (ID: " + user.getStudentId() + ")");

        try {
            ResultSet rs = Database.get().getApplication(user.getStudentId());
            if (!rs.next()) {
                statusLabel.setText("No application found.");
                return;
            }

            String status = rs.getString("status");
            String notes  = rs.getString("notes");

            statusLabel.setText("Status: " + status);

            if (status.contains("Accepted")) {
                statusLabel.setForeground(new Color(0x1E8449));
            } else if (status.contains("Rejected")) {
                statusLabel.setForeground(Color.RED);
            } else {
                statusLabel.setForeground(Color.BLACK);
            }

            notesLabel.setText(notes != null && !notes.isEmpty() ? "Note: " + notes : "");

            actionArea.removeAll();

            if (status.equals("Final Accepted")) {
                boolean hasReport = Database.get().hasReport(user.getStudentId());

                if (hasReport) {
                    JLabel done = new JLabel("Report submitted.");
                    done.setForeground(new Color(0x1E8449));
                    done.setAlignmentX(LEFT_ALIGNMENT);
                    actionArea.add(done);
                    actionArea.add(Box.createVerticalStrut(8));
                    JButton again = new JButton("Re-upload Report");
                    again.setAlignmentX(LEFT_ALIGNMENT);
                    again.addActionListener(e -> uploadReport());
                    actionArea.add(again);
                } else {
                    JLabel msg = new JLabel("Please upload your work term report (PDF only).");
                    msg.setAlignmentX(LEFT_ALIGNMENT);
                    actionArea.add(msg);
                    actionArea.add(Box.createVerticalStrut(8));
                    JButton uploadBtn = new JButton("Upload Report (PDF)");
                    uploadBtn.setAlignmentX(LEFT_ALIGNMENT);
                    uploadBtn.addActionListener(e -> uploadReport());
                    actionArea.add(uploadBtn);
                }
            } else {
                JLabel locked = new JLabel("Report upload is only available after Final Acceptance.");
                locked.setForeground(Color.GRAY);
                locked.setAlignmentX(LEFT_ALIGNMENT);
                actionArea.add(locked);
            }

            actionArea.revalidate();
            actionArea.repaint();

        } catch (Exception ex) {
            statusLabel.setText("Error loading data.");
            ex.printStackTrace();
        }
    }

    private void uploadReport() {
        String term = JOptionPane.showInputDialog(this, "Work term (e.g. Winter 2026):");
        if (term == null || term.trim().isEmpty()) return;

        String deadline = JOptionPane.showInputDialog(this, "Deadline (YYYY-MM-DD):");
        if (deadline == null || deadline.trim().isEmpty()) return;

        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("PDF files", "pdf"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            Helpers.error(this, "Only PDF files are accepted.");
            return;
        }

        try {
            Files.createDirectories(Paths.get("uploads"));
            Path dest = Paths.get("uploads", user.getStudentId() + "_report.pdf");
            Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            Database.get().saveReport(user.getStudentId(), dest.toString(), term.trim(), deadline.trim());
            JOptionPane.showMessageDialog(this, "Report uploaded successfully!");
            reload();
        } catch (Exception ex) {
            Helpers.error(this, "Upload failed: " + ex.getMessage());
        }
    }
}
