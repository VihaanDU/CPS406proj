package csa.ui;

import csa.App;
import csa.db.Database;
import csa.model.User;

import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;

// Yasir - login page
public class LoginScreen extends JPanel {

    App app;

    JTextField emailBox;
    JPasswordField passwordBox;
    JLabel errorMsg;

    public LoginScreen(App app) {
        this.app = app;
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        setup();
    }

    private void setup() {
        // top bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 14));
        topBar.setBackground(Helpers.NAVY);
        JLabel title = new JLabel("Co-op Support Application");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        topBar.add(title);
        add(topBar, BorderLayout.NORTH);

        // login form in the middle
        JPanel middle = new JPanel(new GridBagLayout());
        middle.setBackground(Helpers.LIGHT);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(28, 36, 28, 36)
        ));

        JLabel heading = new JLabel("Sign In");
        heading.setFont(new Font("Arial", Font.BOLD, 16));
        heading.setAlignmentX(LEFT_ALIGNMENT);
        form.add(heading);
        form.add(Box.createVerticalStrut(18));

        form.add(makeLabel("Email"));
        form.add(Box.createVerticalStrut(4));
        emailBox = new JTextField(20);
        emailBox.setMaximumSize(new Dimension(290, 30));
        emailBox.setAlignmentX(LEFT_ALIGNMENT);
        form.add(emailBox);
        form.add(Box.createVerticalStrut(12));

        form.add(makeLabel("Password"));
        form.add(Box.createVerticalStrut(4));
        passwordBox = new JPasswordField(20);
        passwordBox.setMaximumSize(new Dimension(290, 30));
        passwordBox.setAlignmentX(LEFT_ALIGNMENT);
        form.add(passwordBox);
        form.add(Box.createVerticalStrut(8));

        errorMsg = new JLabel(" ");
        errorMsg.setForeground(Color.RED);
        errorMsg.setFont(new Font("Arial", Font.PLAIN, 12));
        errorMsg.setAlignmentX(LEFT_ALIGNMENT);
        form.add(errorMsg);
        form.add(Box.createVerticalStrut(10));

        JButton loginBtn = new JButton("Sign In");
        loginBtn.setAlignmentX(LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> tryLogin());
        form.add(loginBtn);

        form.add(Box.createVerticalStrut(14));
        form.add(new JSeparator());
        form.add(Box.createVerticalStrut(12));

        JLabel newHere = new JLabel("New student?");
        newHere.setFont(new Font("Arial", Font.PLAIN, 12));
        newHere.setForeground(Color.GRAY);
        newHere.setAlignmentX(LEFT_ALIGNMENT);
        form.add(newHere);
        form.add(Box.createVerticalStrut(6));

        JButton applyBtn = new JButton("Apply for Co-op");
        applyBtn.setAlignmentX(LEFT_ALIGNMENT);
        applyBtn.addActionListener(e -> app.goTo(App.SCREEN_APPLY));
        form.add(applyBtn);

        form.add(Box.createVerticalStrut(8));

        JButton supBtn = new JButton("Create Supervisor Account");
        supBtn.setAlignmentX(LEFT_ALIGNMENT);
        supBtn.addActionListener(e -> supervisorSignup());
        form.add(supBtn);

        form.add(Box.createVerticalStrut(14));
        JLabel hint = new JLabel("Demo login: coordinator@tmu.ca / admin123");
        hint.setFont(new Font("Arial", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);
        hint.setAlignmentX(LEFT_ALIGNMENT);
        form.add(hint);

        middle.add(form);
        add(middle, BorderLayout.CENTER);

        passwordBox.addActionListener(e -> tryLogin());
        emailBox.addActionListener(e -> passwordBox.requestFocus());
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.PLAIN, 13));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private void tryLogin() {
        String email = emailBox.getText().trim();
        String pw = new String(passwordBox.getPassword());

        if (email.isEmpty() || pw.isEmpty()) {
            errorMsg.setText("Please fill in both fields.");
            return;
        }

        try {
            ResultSet rs = Database.get().login(email, pw);
            if (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("student_id"),
                    rs.getString("email"),
                    rs.getString("role")
                );
                errorMsg.setText(" ");
                emailBox.setText("");
                passwordBox.setText("");
                app.loginAs(user);
            } else {
                errorMsg.setText("Wrong email or password.");
            }
        } catch (Exception ex) {
            errorMsg.setText("Something went wrong.");
            ex.printStackTrace();
        }
    }

    private void supervisorSignup() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Create Supervisor Account");
        dialog.setSize(340, 260);
        dialog.setLocationRelativeTo(this);
        dialog.setModal(true);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JTextField nameField  = new JTextField(16);
        JTextField emailField = new JTextField(16);
        JPasswordField pwField = new JPasswordField(16);

        String[] labels = {"Name", "Email", "Password"};
        JComponent[] fields = {nameField, emailField, pwField};

        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i]);
            lbl.setAlignmentX(LEFT_ALIGNMENT);
            p.add(lbl);
            p.add(Box.createVerticalStrut(3));
            fields[i].setMaximumSize(new Dimension(280, 28));
            fields[i].setAlignmentX(LEFT_ALIGNMENT);
            p.add(fields[i]);
            p.add(Box.createVerticalStrut(10));
        }

        JButton createBtn = new JButton("Create Account");
        createBtn.setAlignmentX(LEFT_ALIGNMENT);
        createBtn.addActionListener(e -> {
            String name  = nameField.getText().trim();
            String email = emailField.getText().trim();
            String pw    = new String(pwField.getPassword());

            if (name.isEmpty() || email.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.");
                return;
            }
            if (!Helpers.isValidEmail(email)) {
                JOptionPane.showMessageDialog(dialog, "Invalid email.");
                return;
            }
            try {
                if (Database.get().emailExists(email)) {
                    JOptionPane.showMessageDialog(dialog, "Email already registered.");
                    return;
                }
                Database.get().addSupervisor(name, email, pw);
                JOptionPane.showMessageDialog(dialog, "Account created! You can now log in.");
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });
        p.add(createBtn);

        dialog.add(p);
        dialog.setVisible(true);
    }
}
