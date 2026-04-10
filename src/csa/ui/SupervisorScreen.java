package csa.ui;

import csa.App;
import csa.db.Database;
import csa.model.User;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.*;

// Kian - supervisor page for submitting evaluations
public class SupervisorScreen extends JPanel {

    App app;
    User user;

    JLabel welcomeLabel;
    JTextField sidField;
    JTextField termField;
    JTextArea commentsArea;
    JSlider ratingSlider;
    JLabel ratingLabel;
    File pickedPdf;
    JLabel pdfLabel;

    public SupervisorScreen(App app) {
        this.app = app;
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        setup();
    }

    public void setUser(User u) {
        user = u;
        if (welcomeLabel != null)
            welcomeLabel.setText("Welcome, " + u.getName());
    }

    private void setup() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Helpers.NAVY);
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        JLabel title = new JLabel("Supervisor Portal");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        topBar.add(title, BorderLayout.WEST);
        JButton signOut = new JButton("Sign Out");
        signOut.addActionListener(e -> app.goTo(App.SCREEN_LOGIN));
        topBar.add(signOut, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        body.setBackground(Color.WHITE);

        welcomeLabel = new JLabel("Welcome");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        welcomeLabel.setAlignmentX(LEFT_ALIGNMENT);
        body.add(welcomeLabel);
        body.add(Box.createVerticalStrut(18));

        // online form section
        JLabel formTitle = new JLabel("Submit Evaluation — Online Form");
        formTitle.setFont(new Font("Arial", Font.BOLD, 13));
        formTitle.setAlignmentX(LEFT_ALIGNMENT);
        body.add(formTitle);
        body.add(Box.createVerticalStrut(10));

        body.add(lbl("Student ID (9 digits):"));
        sidField = new JTextField(16);
        sidField.setMaximumSize(new Dimension(240, 28));
        sidField.setAlignmentX(LEFT_ALIGNMENT);
        body.add(sidField);
        body.add(Box.createVerticalStrut(8));

        body.add(lbl("Work Term (e.g. Winter 2026):"));
        termField = new JTextField(16);
        termField.setMaximumSize(new Dimension(240, 28));
        termField.setAlignmentX(LEFT_ALIGNMENT);
        body.add(termField);
        body.add(Box.createVerticalStrut(8));

        body.add(lbl("Rating (1 = Poor, 5 = Excellent):"));
        ratingSlider = new JSlider(1, 5, 3);
        ratingSlider.setMajorTickSpacing(1);
        ratingSlider.setPaintTicks(true);
        ratingSlider.setPaintLabels(true);
        ratingSlider.setSnapToTicks(true);
        ratingSlider.setMaximumSize(new Dimension(240, 50));
        ratingSlider.setAlignmentX(LEFT_ALIGNMENT);
        ratingLabel = new JLabel("3");
        ratingLabel.setAlignmentX(LEFT_ALIGNMENT);
        ratingSlider.addChangeListener(e ->
            ratingLabel.setText(String.valueOf(ratingSlider.getValue())));
        body.add(ratingSlider);
        body.add(ratingLabel);
        body.add(Box.createVerticalStrut(8));

        body.add(lbl("Comments:"));
        commentsArea = new JTextArea(4, 24);
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        commentsArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JScrollPane cs = new JScrollPane(commentsArea);
        cs.setMaximumSize(new Dimension(440, 90));
        cs.setAlignmentX(LEFT_ALIGNMENT);
        body.add(cs);
        body.add(Box.createVerticalStrut(12));

        JButton submitFormBtn = new JButton("Submit Online Evaluation");
        submitFormBtn.setAlignmentX(LEFT_ALIGNMENT);
        submitFormBtn.addActionListener(e -> submitForm());
        body.add(submitFormBtn);
        body.add(Box.createVerticalStrut(20));

        // divider
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(480, 2));
        sep.setAlignmentX(LEFT_ALIGNMENT);
        body.add(sep);
        body.add(Box.createVerticalStrut(16));

        // pdf section
        JLabel pdfTitle = new JLabel("Submit Evaluation — PDF Upload");
        pdfTitle.setFont(new Font("Arial", Font.BOLD, 13));
        pdfTitle.setAlignmentX(LEFT_ALIGNMENT);
        body.add(pdfTitle);
        body.add(Box.createVerticalStrut(6));

        JLabel pdfNote = new JLabel("Upload a scanned copy of the paper evaluation form.");
        pdfNote.setForeground(Color.GRAY);
        pdfNote.setFont(new Font("Arial", Font.PLAIN, 12));
        pdfNote.setAlignmentX(LEFT_ALIGNMENT);
        body.add(pdfNote);
        body.add(Box.createVerticalStrut(8));

        JButton pickBtn = new JButton("Choose PDF...");
        pickBtn.setAlignmentX(LEFT_ALIGNMENT);
        pickBtn.addActionListener(e -> pickFile());
        body.add(pickBtn);
        body.add(Box.createVerticalStrut(4));

        pdfLabel = new JLabel("No file chosen.");
        pdfLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        pdfLabel.setForeground(Color.GRAY);
        pdfLabel.setAlignmentX(LEFT_ALIGNMENT);
        body.add(pdfLabel);
        body.add(Box.createVerticalStrut(10));

        JButton submitPdfBtn = new JButton("Submit PDF Evaluation");
        submitPdfBtn.setAlignmentX(LEFT_ALIGNMENT);
        submitPdfBtn.addActionListener(e -> submitPdf());
        body.add(submitPdfBtn);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.PLAIN, 13));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private void submitForm() {
        String sid      = sidField.getText().trim();
        String term     = termField.getText().trim();
        String comments = commentsArea.getText().trim();
        int rating      = ratingSlider.getValue();

        if (!Helpers.isValidStudentId(sid)) {
            Helpers.error(this, "Student ID must be 9 digits.");
            return;
        }
        if (term.isEmpty()) {
            Helpers.error(this, "Work term is required.");
            return;
        }
        if (comments.isEmpty()) {
            Helpers.error(this, "Please add some comments.");
            return;
        }

        try {
            String status = Database.get().getStatus(sid);
            if (!"Final Accepted".equals(status)) {
                Helpers.error(this, "No Final Accepted student found with ID: " + sid);
                return;
            }
            Database.get().saveEvalForm(sid, user.getEmail(), term, comments, rating);
            JOptionPane.showMessageDialog(this, "Evaluation submitted!");
            sidField.setText("");
            termField.setText("");
            commentsArea.setText("");
            ratingSlider.setValue(3);
        } catch (Exception ex) {
            Helpers.error(this, "Error: " + ex.getMessage());
        }
    }

    private void pickFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            pickedPdf = fc.getSelectedFile();
            pdfLabel.setText(pickedPdf.getName());
        }
    }

    private void submitPdf() {
        if (pickedPdf == null) {
            Helpers.error(this, "Please choose a PDF file first.");
            return;
        }

        String sid = JOptionPane.showInputDialog(this, "Student ID this evaluation is for:");
        if (sid == null || sid.trim().isEmpty()) return;
        sid = sid.trim();

        if (!Helpers.isValidStudentId(sid)) {
            Helpers.error(this, "Student ID must be 9 digits.");
            return;
        }

        String term = JOptionPane.showInputDialog(this, "Work term (e.g. Winter 2026):");
        if (term == null || term.trim().isEmpty()) return;

        try {
            String status = Database.get().getStatus(sid);
            if (!"Final Accepted".equals(status)) {
                Helpers.error(this, "No Final Accepted student found with ID: " + sid);
                return;
            }

            Files.createDirectories(Paths.get("uploads"));
            Path dest = Paths.get("uploads", sid + "_eval.pdf");
            Files.copy(pickedPdf.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            Database.get().saveEvalPdf(sid, user.getEmail(), term.trim(), dest.toString());
            JOptionPane.showMessageDialog(this, "PDF evaluation submitted!");
            pickedPdf = null;
            pdfLabel.setText("No file chosen.");
        } catch (Exception ex) {
            Helpers.error(this, "Error: " + ex.getMessage());
        }
    }
}
