package csa.ui;

import csa.App;
import csa.db.Database;

import javax.swing.*;
import java.awt.*;

// Vihaan - form for new students to apply
public class ApplyScreen extends JPanel {

    App app;

    JTextField nameField;
    JTextField sidField;
    JTextField emailField;
    JPasswordField pwField;
    JPasswordField confirmField;

    public ApplyScreen(App app) {
        this.app = app;
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        setup();
    }

    private void setup() {
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        topBar.setBackground(Helpers.NAVY);
        JLabel title = new JLabel("Apply for Co-op");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        topBar.add(title);
        add(topBar, BorderLayout.NORTH);

        JPanel middle = new JPanel(new GridBagLayout());
        middle.setBackground(Helpers.LIGHT);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xCCCCCC)),
            BorderFactory.createEmptyBorder(24, 28, 24, 28)
        ));

        nameField    = new JTextField(14);
        sidField     = new JTextField(14);
        emailField   = new JTextField(14);
        pwField      = new JPasswordField(14);
        confirmField = new JPasswordField(14);

        form.add(new JLabel("Full Name *"));
        form.add(nameField);
        form.add(new JLabel("Student ID (9 digits) *"));
        form.add(sidField);
        form.add(new JLabel("Email *"));
        form.add(emailField);
        form.add(new JLabel("Password *"));
        form.add(pwField);
        form.add(new JLabel("Confirm Password *"));
        form.add(confirmField);

        JButton backBtn   = new JButton("Back to Login");
        JButton submitBtn = new JButton("Submit Application");
        backBtn.addActionListener(e -> app.goTo(App.SCREEN_LOGIN));
        submitBtn.addActionListener(e -> submit());
        form.add(backBtn);
        form.add(submitBtn);

        middle.add(form);
        add(middle, BorderLayout.CENTER);
    }

    private void submit() {
        String name  = nameField.getText().trim();
        String sid   = sidField.getText().trim();
        String email = emailField.getText().trim();
        String pw    = new String(pwField.getPassword());
        String cpw   = new String(confirmField.getPassword());

        if (name.isEmpty() || sid.isEmpty() || email.isEmpty() || pw.isEmpty()) {
            Helpers.error(this, "Please fill in all fields.");
            return;
        }
        if (!Helpers.isValidStudentId(sid)) {
            Helpers.error(this, "Student ID must be exactly 9 digits.");
            return;
        }
        if (!Helpers.isValidEmail(email)) {
            Helpers.error(this, "Please enter a valid email.");
            return;
        }
        if (pw.length() < 6) {
            Helpers.error(this, "Password must be at least 6 characters.");
            return;
        }
        if (!pw.equals(cpw)) {
            Helpers.error(this, "Passwords don't match.");
            return;
        }

        try {
            Database db = Database.get();

            if (db.studentIdExists(sid)) {
                Helpers.error(this, "A student with that ID already exists.");
                return;
            }
            if (db.emailExists(email)) {
                Helpers.error(this, "That email is already registered.");
                return;
            }

            db.addStudent(name, sid, email, pw);
            db.submitApplication(sid, name, email);

            JOptionPane.showMessageDialog(this,
                "Application submitted!\n\n" +
                "Status: Pending Provisional Review\n" +
                "You can log in once the coordinator reviews your file.");

            nameField.setText("");
            sidField.setText("");
            emailField.setText("");
            pwField.setText("");
            confirmField.setText("");

            app.goTo(App.SCREEN_LOGIN);

        } catch (Exception ex) {
            Helpers.error(this, "Error: " + ex.getMessage());
        }
    }
}
